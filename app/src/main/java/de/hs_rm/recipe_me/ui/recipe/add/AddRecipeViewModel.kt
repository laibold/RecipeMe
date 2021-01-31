package de.hs_rm.recipe_me.ui.recipe.add

import android.text.Editable
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.hs_rm.recipe_me.R
import de.hs_rm.recipe_me.declaration.addToValue
import de.hs_rm.recipe_me.declaration.setValueAt
import de.hs_rm.recipe_me.model.recipe.*
import de.hs_rm.recipe_me.service.RecipeRepository
import kotlinx.coroutines.launch

/**
 * Shared ViewModel for adding recipes.
 * Used in [AddRecipeFragment1], [AddRecipeFragment2] and [AddRecipeFragment3],
 */
class AddRecipeViewModel @ViewModelInject constructor(
    private val repository: RecipeRepository
) : ViewModel() {

    lateinit var recipeCategory: RecipeCategory
    private var updatingCookingStepIndex = -1
    private var updatingIngredientIndex = -1

    private val _recipe = MutableLiveData<Recipe>()
    val recipe: LiveData<Recipe>
        get() = _recipe

    private val _ingredients = MutableLiveData<MutableList<Ingredient>>()
    val ingredients: LiveData<MutableList<Ingredient>>
        get() = _ingredients

    private val _cookingSteps = MutableLiveData<MutableList<CookingStep>>()
    val cookingSteps: LiveData<MutableList<CookingStep>>
        get() = _cookingSteps

    init {
        _ingredients.postValue(mutableListOf())
        _cookingSteps.postValue(mutableListOf())
    }

    /**
     * Initialize recipe if not already done
     */
    fun initRecipe() {
        if (_recipe.value == null) {
            // don't use postValue() here
            _recipe.value = Recipe(recipeCategory)
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
        ingredientUnit: IngredientUnit
    ): Boolean {
        val ingredient = getIngredient(name, quantity, ingredientUnit)

        if (ingredient != null) {
            _ingredients.setValueAt(this.updatingIngredientIndex, ingredient)
            return true
        }

        return false
    }

    /**
     * Create ingredient from given parameters, return null if name is empty
     * @param name Name of ingredient (won't get created without it)
     * @param quantity Quantity as String
     * @param ingredientUnit IngredientUnit
     * @return true if cooking step could be created
     */
    private fun getIngredient(
        name: Editable, quantity: Editable, ingredientUnit: IngredientUnit
    ): Ingredient? {
        var quantityDouble = Ingredient.DEFAULT_QUANTITY

        if (name.isNotBlank()) {
            if (quantity.isNotBlank()) {
                quantityDouble = quantity.toString().replace(',', '.').toDouble()
            }
            return Ingredient(name.toString().trim(), quantityDouble, ingredientUnit)
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
     * Add cooking step to ViewModel scope
     * @param text Text of cooking step (won't get added without it)
     * @param time Time as String
     * @param timeUnit TimeUnit
     * @return true if cooking step could be added
     */
    fun addCookingStep(text: Editable, time: Editable, timeUnit: TimeUnit): Boolean {
        val cookingStep = getCookingStep(text, time, timeUnit)
        if (cookingStep != null) {
            _cookingSteps.addToValue(cookingStep)
            return true
        }
        return false
    }

    /**
     * Update cooking step to ViewModel scope
     * @param text Text of cooking step (won't get updated without it)
     * @param time Time as String
     * @param timeUnit TimeUnit
     * @return true if cooking step could be updated
     */
    fun updateCookingStep(text: Editable, time: Editable, timeUnit: TimeUnit): Boolean {
        val cookingStep = getCookingStep(text, time, timeUnit)
        if (cookingStep != null) {
            _cookingSteps.setValueAt(this.updatingCookingStepIndex, cookingStep)
            return true
        }
        return false
    }

    /**
     * Create cooking step from given parameters, return null if text is empty
     * @param text Text of cooking step (won't get created without it)
     * @param time Time as String
     * @param timeUnit TimeUnit
     * @return true if cooking step could be created
     */
    private fun getCookingStep(text: Editable, time: Editable, timeUnit: TimeUnit): CookingStep? {
        var timeInt = CookingStep.DEFAULT_TIME
        if (text.isNotBlank()) {
            if (time.isNotBlank()) {
                timeInt = time.toString().toInt()
            }
            return CookingStep(text.toString().trim(), timeInt, timeUnit)
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
     * Persist entities to repository. Clears ViewModel content afterwards
     * @return LiveData that contains the id of the generated recipe
     */
    fun persistEntities(): LiveData<Long> {
        val recipeId = MutableLiveData<Long>()
        viewModelScope.launch {
            _recipe.value?.let { r ->
                val id = repository.insert(r)

                for (ingredient in _ingredients.value!!) {
                    ingredient.recipeId = id
                }
                for (cookingStep in _cookingSteps.value!!) {
                    cookingStep.recipeId = id
                }

                _ingredients.value?.let { i -> repository.insert(i) }
                _cookingSteps.value?.let { c -> repository.insert(c) }

                _ingredients.postValue(mutableListOf())
                _cookingSteps.postValue(mutableListOf())
                _recipe.postValue(Recipe(RecipeCategory.values()[0]))
                recipeId.postValue(id)
            }
        }
        return recipeId
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
     * Validate cooking steps (at least one)
     * @return true if valid
     */
    fun validateCookingSteps(): Boolean {
        return _cookingSteps.value!!.isNotEmpty()
    }

}
