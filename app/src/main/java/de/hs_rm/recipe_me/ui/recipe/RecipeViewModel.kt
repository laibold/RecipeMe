package de.hs_rm.recipe_me.ui.recipe

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.hs_rm.recipe_me.model.recipe.Recipe
import de.hs_rm.recipe_me.model.recipe.RecipeCategory
import de.hs_rm.recipe_me.service.RecipeRepository
import kotlinx.coroutines.launch

/**
 * ViewModel for [RecipeHomeFragment]
 * TODO rename to RecipeCategoryViewModel
 */
class RecipeViewModel @ViewModelInject constructor(
    private val repository: RecipeRepository
) : ViewModel() {

    private val recipes = repository.getRecipes()
    lateinit var category: RecipeCategory

    /**
     * Clear database
     */
    fun clear() {
        viewModelScope.launch {
            repository.clearRecipes()
        }
    }

    /**
     * Returns LiveData with list of recipes that match the given [RecipeCategory]
     */
    fun getRecipesByCategory(recipeCategory: RecipeCategory): LiveData<List<Recipe>> {
        return repository.getRecipesByCategory(recipeCategory)
    }

    /**
     * Delete recipe and it's belonging Ingredients and CookingSteps
     */
    fun deleteRecipeAndRelations(recipe: Recipe) {
        viewModelScope.launch {
            repository.deleteRecipeAndRelations(recipe)
        }
    }

}
