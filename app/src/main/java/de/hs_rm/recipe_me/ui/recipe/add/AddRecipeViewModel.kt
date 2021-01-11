package de.hs_rm.recipe_me.ui.recipe.add

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.hs_rm.recipe_me.declaration.notifyObserver
import de.hs_rm.recipe_me.model.SaveAction
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

    val saveAction = MutableLiveData<SaveAction>()

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
        _ingredients.value = mutableListOf()
        _cookingSteps.value = mutableListOf()
        saveAction.value = SaveAction.ADD
    }

    /**
     * Initialize recipe if not already done
     */
    fun initRecipe() {
        if (_recipe.value == null) {
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
    fun addIngredient(name: String, quantity: String, unit: IngredientUnit): Boolean {
        var quantityDouble = 0.0

        if (name != "") {
            if (quantity != "") {
                quantityDouble = quantity.replace(",", ".").toDouble()
            }
            _ingredients.value?.add(Ingredient(name, quantityDouble, unit))
            _ingredients.notifyObserver()
            return true
        }
        return false
    }

    /**
     * Add cooking step to ViewModel scope
     * @param text Text of cooking step (won't get added without it)
     * @param time Time as String
     * @param timeUnit TimeUnit
     * @return true if cooking step could be added
     */
    fun addCookingStep(text: String, time: String, timeUnit: TimeUnit): Boolean {
        val cookingStep = getCookingStep(text, time, timeUnit)
        if (cookingStep != null) {
            _cookingSteps.value?.add(cookingStep)
            _cookingSteps.notifyObserver()
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
    fun updateCookingStep(text: String, time: String, timeUnit: TimeUnit): Boolean {
        val cookingStep = getCookingStep(text, time, timeUnit)
        if (cookingStep != null) {
            _cookingSteps.value?.set(updatingCookingStepIndex, cookingStep)
            _cookingSteps.notifyObserver()
            saveAction.value = SaveAction.ADD
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
    private fun getCookingStep(text: String, time: String, timeUnit: TimeUnit): CookingStep? {
        var timeInt = 0
        if (text != "") {
            if (time != "") {
                timeInt = time.toInt()
            }
            return CookingStep(text, timeInt, timeUnit)
        }
        return null
    }

    /**
     * Switch internal states to update cooking steps instead of adding
     * @param position Index of [CookingStep] in [CookingStepListAdapter] to be updated
     */
    fun prepareCookingStepUpdate(position: Int) {
        updatingCookingStepIndex = position
        saveAction.value = SaveAction.UPDATE
    }

    /**
     * Persist entities to repository. Clears ViewModel content afterwards
     */
    fun persistEntities() {
        viewModelScope.launch {
            _recipe.value?.let { repository.insert(it) }
            _ingredients.value?.let { repository.insert(it) }
            _cookingSteps.value?.let { repository.insert(it) }
        }

        _recipe.value = Recipe(RecipeCategory.values()[0])
        _ingredients.value = mutableListOf()
        _cookingSteps.value = mutableListOf()
    }

}
