package de.hs_rm.recipe_me.ui.recipe.add

import android.graphics.Bitmap
import android.net.Uri
import android.text.Editable
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import de.hs_rm.recipe_me.R
import de.hs_rm.recipe_me.declaration.addToValue
import de.hs_rm.recipe_me.declaration.setValueAt
import de.hs_rm.recipe_me.model.recipe.*
import de.hs_rm.recipe_me.model.relation.CookingStepIngredientCrossRef
import de.hs_rm.recipe_me.model.relation.CookingStepWithIngredients
import de.hs_rm.recipe_me.model.relation.RecipeWithRelations
import de.hs_rm.recipe_me.service.repository.RecipeImageRepository
import de.hs_rm.recipe_me.service.repository.RecipeRepository
import de.hs_rm.recipe_me.ui.recipe.add.cooking_step.AddCookingStepListAdapter
import de.hs_rm.recipe_me.ui.recipe.add.cooking_step.AddRecipeFragment3
import de.hs_rm.recipe_me.ui.recipe.add.ingredient.AddIngredientListAdapter
import de.hs_rm.recipe_me.ui.recipe.add.ingredient.AddRecipeFragment2
import de.hs_rm.recipe_me.ui.recipe.add.recipe_information.AddRecipeFragment1
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Shared ViewModel for adding recipes.
 * Used in [AddRecipeFragment1], [AddRecipeFragment2] and [AddRecipeFragment3],
 */
@HiltViewModel
class AddRecipeViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository,
    private val imageRepository: RecipeImageRepository,
) : ViewModel() {

    private var updatingCookingStepIndex = -1
    private var updatingIngredientIndex = -1

    private var recipeToUpdate: Recipe? = null
    private var oldIngredients: MutableList<Ingredient>? = null
    private var oldCookingSteps: MutableList<CookingStep>? = null

    private val _recipeCategory = MutableLiveData<RecipeCategory>()
    val category: LiveData<RecipeCategory>
        get() = _recipeCategory

    private val _recipe = MutableLiveData<Recipe?>()
    val recipe: LiveData<Recipe?>
        get() = _recipe

    private val _ingredients = MutableLiveData<MutableList<Ingredient>>()
    val ingredients: LiveData<MutableList<Ingredient>>
        get() = _ingredients

    private val _cookingStepsWithIngredients =
        MutableLiveData<MutableList<CookingStepWithIngredients>>()
    val cookingStepsWithIngredients: LiveData<MutableList<CookingStepWithIngredients>>
        get() = _cookingStepsWithIngredients

    private val _recipeImage = MutableLiveData<Bitmap?>()
    val recipeImage: LiveData<Bitmap?>
        get() = _recipeImage

    private lateinit var observer: Observer<RecipeWithRelations>

    /**
     * Clear variable values on initialization because ViewModel has Activity lifecycle scope
     */
    private fun clearValues() {
        _ingredients.value = mutableListOf()
        _cookingStepsWithIngredients.value = mutableListOf()
        _recipe.value = null
        _recipeImage.value = null
        recipeToUpdate = null
    }

    /**
     * Initialize recipe, Ingredients and cookingStepsWithIngredients.
     * Old Values will be cleared and if recipeId is not default, the related Recipe will be loaded.
     * This method should only be called when entering the add/edit recipe graph
     */
    fun initRecipe(recipeId: Long) {
        clearValues()

        if (recipeId != Recipe.DEFAULT_ID) {
            // recipeId has been committed, so this recipe should be edited and its values should be entered into the forms

            viewModelScope.launch {
                recipeToUpdate = recipeRepository.getRecipeById(recipeId)

                if (recipeToUpdate != null) {
                    setRecipeImage(recipeToUpdate!!)

                    val loadedRecipeWithRelations =
                        recipeRepository.getRecipeWithRelationsById(recipeId)

                    observer = Observer { recipeWithRelations ->
                        _recipe.postValue(recipeWithRelations.recipe)

                        oldIngredients = mutableListOf()
                        oldCookingSteps = mutableListOf()

                        for (ingredient in recipeWithRelations.ingredients) {
                            oldIngredients!!.add(ingredient)
                            _ingredients.addToValue(ingredient)
                        }

                        for (cookingStepWithIngredients in recipeWithRelations.cookingStepsWithIngredients) {
                            oldCookingSteps!!.add(cookingStepWithIngredients.cookingStep)
                            _cookingStepsWithIngredients.addToValue(
                                cookingStepWithIngredients
                            )
                        }
                        loadedRecipeWithRelations.removeObserver(observer)
                    }

                    loadedRecipeWithRelations.observeForever(observer)
                }
            }

        } else {
            // Add recipe
            _recipe.value = Recipe(_recipeCategory.value!!)
        }
    }

    /**
     * Set attributes to new [Recipe]
     * @param name Name of the Recipe (must not be empty)
     * @param servings Number of servings (must be greater than 0)
     * @param category Category of the Recipe
     * @return true if input was valid
     */
    fun setRecipeAttributes(name: String, servings: String, category: RecipeCategory): Boolean {
        if (name != "" && servings != "") {
            val servingsInt = servings.toInt()
            if (servingsInt > 0) {
                _recipe.value?.name = name
                _recipe.value?.servings = servingsInt
                _recipe.value?.category = category
                return true
            }
        }
        return false
    }

    fun setCategory(recipeCategory: RecipeCategory) {
        _recipeCategory.value = recipeCategory
    }

    /**
     * Add ingredient to ViewModel scope
     * @param name Name of ingredient (won't get added without it)
     * @param quantity Quantity as String
     * @param unit IngredientUnit
     * @return true if ingredient could be added
     */
    fun addIngredient(name: Editable, quantity: Editable, unit: IngredientUnit): Boolean {
        val ingredient = getIngredient(name, quantity, unit)

        if (ingredient != null) {
            _ingredients.addToValue(ingredient)
            return true
        }

        return false
    }

    /**
     * Update ingredient to ViewModel scope
     * @param name Editable with name of ingredient (won't get updated if it's blank)
     * @param quantity Quantity as Editable
     * @param ingredientUnit IngredientUnit
     * @return true if cooking step could be updated
     */
    fun updateIngredient(
        name: Editable,
        quantity: Editable,
        ingredientUnit: IngredientUnit,
    ): Boolean {
        val ingredientToUpdate = _ingredients.value!![updatingIngredientIndex]
        val ingredient =
            getIngredient(name, quantity, ingredientUnit, ingredientToUpdate.ingredientId)

        if (ingredient != null) {
            _ingredients.setValueAt(this.updatingIngredientIndex, ingredient)
            return true
        }

        return false
    }

    /**
     * Create ingredient from given parameters, return null if name is empty.
     * If ingredientId is provided, it will be inserted into the created Ingredient
     * @param name Name of ingredient (won't get created without it)
     * @param quantity Quantity as String
     * @param ingredientUnit IngredientUnit
     * @param ingredientId Id of ingredient if it already has one (on update)
     * @return true if cooking step could be created
     */
    private fun getIngredient(
        name: Editable,
        quantity: Editable,
        ingredientUnit: IngredientUnit,
        ingredientId: Long = Ingredient.DEFAULT_ID,
    ): Ingredient? {
        var quantityDouble = Ingredient.DEFAULT_QUANTITY

        if (name.isNotBlank()) {
            if (quantity.isNotBlank()) {
                quantityDouble = quantity.toString().replace(',', '.').toDouble()
            }
            val ingredient = Ingredient(name.toString().trim(), quantityDouble, ingredientUnit)
            if (ingredientId != Ingredient.DEFAULT_ID) {
                ingredient.ingredientId = ingredientId
            }
            return ingredient
        }
        return null
    }

    /**
     * Switch internal states to update ingredients instead of adding
     * @param position Index of [Ingredient] in [AddIngredientListAdapter] to be updated
     */
    fun prepareIngredientUpdate(position: Int) {
        updatingIngredientIndex = position
    }

    /**
     * Add [CookingStepWithIngredients] to ViewModel scope
     * @param text Text of cooking step (won't get added without it)
     * @param time Time as String
     * @param timeUnit TimeUnit
     * @param ingredients belonging Ingredients
     * @return true if cooking step could be added
     */
    fun addCookingStepWithIngredients(
        text: Editable,
        time: Editable,
        timeUnit: TimeUnit,
        ingredients: MutableList<Ingredient>,
    ): Boolean {
        val cookingStepWithIngredients =
            getCookingStepWithIngredients(text, time, timeUnit, ingredients)
        if (cookingStepWithIngredients != null) {
            _cookingStepsWithIngredients.addToValue(cookingStepWithIngredients)
            return true
        }
        return false
    }

    /**
     * Update [CookingStepWithIngredients] to ViewModel scope
     * @param text Text of cooking step (won't get updated without it)
     * @param time Time as String
     * @param timeUnit TimeUnit
     * @param ingredients belonging Ingredients
     * @return true if cooking step could be updated
     */
    fun updateCookingStepWithIngredients(
        text: Editable,
        time: Editable,
        timeUnit: TimeUnit,
        ingredients: MutableList<Ingredient>,
    ): Boolean {
        val objectToUpdate =
            _cookingStepsWithIngredients.value!![updatingCookingStepIndex].cookingStep
        val cookingStepWithIngredients =
            getCookingStepWithIngredients(
                text,
                time,
                timeUnit,
                ingredients,
                objectToUpdate.cookingStepId
            )

        if (cookingStepWithIngredients != null) {
            _cookingStepsWithIngredients.setValueAt(
                this.updatingCookingStepIndex,
                cookingStepWithIngredients
            )
            return true
        }
        return false
    }

    /**
     * Create cooking step from given parameters, return null if text is empty.
     * If cookingStepId is provided, it will be inserted into the created CookingStepWithIngredients
     * @param text Text of cooking step (won't get created without it)
     * @param time Time as String
     * @param timeUnit TimeUnit
     * @param cookingStepId Id of cookingStep if it already has one (on update)
     * @return true if cooking step could be created
     */
    private fun getCookingStepWithIngredients(
        text: Editable,
        time: Editable,
        timeUnit: TimeUnit,
        ingredients: MutableList<Ingredient>,
        cookingStepId: Long = CookingStep.DEFAULT_ID,
    ): CookingStepWithIngredients? {
        var timeInt = CookingStep.DEFAULT_TIME

        if (text.isNotBlank()) {
            if (time.isNotBlank()) {
                timeInt = time.toString().toInt()
            }
            val cookingStepWithIngredients = CookingStepWithIngredients(
                CookingStep(text.toString().trim(), timeInt, timeUnit),
                ingredients
            )
            if (cookingStepId != CookingStep.DEFAULT_ID) {
                cookingStepWithIngredients.cookingStep.cookingStepId = cookingStepId
            }
            return cookingStepWithIngredients
        }
        return null
    }

    /**
     * Switch internal states to update cooking steps instead of adding
     * @param position Index of [CookingStep] in [AddCookingStepListAdapter] to be updated
     */
    fun prepareCookingStepUpdate(position: Int) {
        updatingCookingStepIndex = position
    }

    /**
     * Validate recipe name (not empty)
     * @return 0 if valid, otherwise string id for error message
     */
    fun validateName(name: Editable): Int {
        if (name.isBlank()) {
            return R.string.err_enter_name
        }
        return 0
    }

    /**
     * Validate recipe servings (not empty and greater than 0)
     * @return 0 if valid, otherwise string id for error message
     */
    fun validateServings(servings: Editable): Int {
        if (servings.isBlank()) {
            return R.string.err_enter_servings
        }
        if (servings.toString().toInt() < 1) {
            return R.string.err_servings_greater_than_zero
        }
        return 0
    }

    /**
     * Validate ingredients (at least one)
     * @return true if valid
     */
    fun validateIngredients(): Boolean {
        return _ingredients.value!!.isNotEmpty()
    }

    /**
     * Persist entities to repository. Clears ViewModel content afterwards
     * @return LiveData that contains the id of the saved/updated recipe
     */
    fun persistEntities(): LiveData<Long> {
        return if (recipeToUpdate != null) {
            updateEntities()
        } else {
            saveNewEntities()
        }
    }

    /**
     * Update Recipe, Ingredients and CookingSteps with related Ingredients
     * @return LiveData that contains the id of the updated recipe
     */
    private fun updateEntities(): MutableLiveData<Long> {
        val recipeId = MutableLiveData<Long>()

        // Set recipe values
        _recipe.value?.let { r ->
            recipeToUpdate!!.name = r.name
            recipeToUpdate!!.servings = r.servings
            recipeToUpdate!!.category = r.category
        }

        viewModelScope.launch {
            // Lists to copy LiveData values to before inserting to prevent ConcurrentModificationException
            lateinit var ingredientList: List<Ingredient>
            lateinit var cookingStepList: List<CookingStepWithIngredients>

            recipeRepository.update(recipeToUpdate!!)

            _ingredients.value?.let { ingredients ->
                for (ingredient in ingredients) {
                    ingredient.recipeId = recipeToUpdate!!.id
                }
                ingredientList = ArrayList<Ingredient>(ingredients)
            }

            _cookingStepsWithIngredients.value?.let { list ->
                for (cookingStepWithIngredients in list) {
                    cookingStepWithIngredients.cookingStep.recipeId = recipeToUpdate!!.id
                }
                cookingStepList = ArrayList<CookingStepWithIngredients>(list)
            }

            // insert, update and delete ingredients
            for (ingredient in ingredientList) {
                if (ingredient.ingredientId != Ingredient.DEFAULT_ID) {
                    // Items that have already been in the database
                    recipeRepository.update(ingredient)
                } else {
                    // New items
                    ingredient.ingredientId = recipeRepository.insert(ingredient)
                }
            }
            // Delete ingredients that are in old but not in new list
            for (ingredient in oldIngredients!!) {
                if (!ingredientList.contains(ingredient)) {
                    recipeRepository.deleteIngredient(ingredient)
                }
            }

            // insert, update and delete cookingStepsWithIngredients
            for (cookingStepWithIngredients in cookingStepList) {
                val cookingStep = cookingStepWithIngredients.cookingStep

                if (cookingStep.cookingStepId != CookingStep.DEFAULT_ID) {
                    // Items that have already been in the database
                    recipeRepository.update(cookingStep)
                } else {
                    // New items
                    cookingStep.cookingStepId = recipeRepository.insert(cookingStep)
                }

                // Delete relations to ingredients and re-insert them. This might be the fastest way
                recipeRepository.deleteCookingStepIngredientCrossRefs(cookingStepId = cookingStep.cookingStepId)
                insertCookingStepIngredientCrossRefs(
                    cookingStepWithIngredients.ingredients,
                    cookingStep.cookingStepId
                )
            }

            // Delete CookingSteps that are in old but not in new list
            for (cookingStep in oldCookingSteps!!) {
                if (!cookingStepList.map { it.cookingStep }.contains(cookingStep)) {
                    recipeRepository.deleteCookingStep(cookingStep)
                }
            }

            saveImage(recipeToUpdate!!.id)
            recipeId.postValue(recipeToUpdate!!.id)
        }
        return recipeId
    }

    /**
     * Save Recipe, Ingredients and CookingSteps with related Ingredients to database
     * @return LiveData that contains the id of the saved/updated recipe
     */
    private fun saveNewEntities(): MutableLiveData<Long> {
        val recipeId = MutableLiveData<Long>()

        viewModelScope.launch {
            _recipe.value?.let { recipe ->
                // Insert recipe
                val id = recipeRepository.insert(recipe)

                // Assign recipe id to ingredients and cooking steps
                for (ingredient in _ingredients.value!!) {
                    ingredient.recipeId = id
                }
                for (cookingStepWithIngredients in _cookingStepsWithIngredients.value!!) {
                    cookingStepWithIngredients.cookingStep.recipeId = id
                }

                // Insert ingredients
                _ingredients.value?.let { ingredients ->
                    for (ingredient in ingredients) {
                        // Assign id from inserted entity back to object to create cross reference
                        ingredient.ingredientId = recipeRepository.insert(ingredient)
                    }
                }

                // Insert cooking steps and create references to the belonging ingredients
                _cookingStepsWithIngredients.value?.let { list ->
                    for (cookingStepWithIngredients in list) {
                        val cId = recipeRepository.insert(cookingStepWithIngredients.cookingStep)
                        insertCookingStepIngredientCrossRefs(
                            cookingStepWithIngredients.ingredients,
                            cId
                        )
                    }
                }

                saveImage(id)
                recipeId.postValue(id)
            }
        }
        return recipeId
    }

    /**
     * Insert a cross reference entry for every given ingredient and cooking step.
     * Use this after the ingredients have been inserted. They need to have their auto-generated id
     */
    private suspend fun insertCookingStepIngredientCrossRefs(
        ingredients: List<Ingredient>,
        cookingStepId: Long,
    ) {
        // The ingredients are references to already inserted ones, so they
        // now have an id and we can create a cross reference to the CookingSteps
        // that have just been inserted
        for (ingredient in ingredients) {
            recipeRepository.insert(
                CookingStepIngredientCrossRef(
                    cookingStepId,
                    ingredient.ingredientId
                )
            )
        }
    }

    /**
     * Save image to recipe folder with recipe id.
     * An existing image will be replaced.
     */
    private fun saveImage(recipeId: Long) {
        _recipeImage.value?.let {
            imageRepository.saveRecipeImage(it, recipeId)
        }
    }

    /**
     * Load picture from given uri and save it to viewModel scope
     */
    fun setRecipeImage(uri: Uri, width: Int, height: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            _recipeImage.postValue(
                imageRepository.getImageFromUri(uri, width, height)
            )
        }
    }

    /**
     * Load picture from given uri and save it to viewModel scope
     */
    private fun setRecipeImage(recipe: Recipe) {
        CoroutineScope(Dispatchers.IO).launch {
            _recipeImage.postValue(
                imageRepository.getRecipeImage(recipe)
            )
        }
    }

}
