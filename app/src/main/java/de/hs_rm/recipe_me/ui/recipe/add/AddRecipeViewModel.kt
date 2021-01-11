package de.hs_rm.recipe_me.ui.recipe.add

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.hs_rm.recipe_me.declaration.notifyObserver
import de.hs_rm.recipe_me.model.recipe.*
import de.hs_rm.recipe_me.service.RecipeRepository

/**
 * Shared ViewModel for adding recipes.
 * Used in [AddRecipeFragment1], [AddRecipeFragment2] and [AddRecipeFragment3],
 */
class AddRecipeViewModel @ViewModelInject constructor(
    private val repository: RecipeRepository
) : ViewModel() {

    lateinit var recipeCategory: RecipeCategory

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
     * Add ingredient to ViewModel scope
     */
    fun addIngredient(name: String, quantity: Double, unit: IngredientUnit) {
        _ingredients.value?.add(Ingredient(name, quantity, unit))
        _ingredients.notifyObserver()
    }

    /**
     * Add cooking step to ViewModel scope
     */
    fun addCookingStep(text: String, time: Int, timeUnit: TimeUnit) {
        _cookingSteps.value?.add(CookingStep(text, time, timeUnit))
        _cookingSteps.notifyObserver()
    }

}
