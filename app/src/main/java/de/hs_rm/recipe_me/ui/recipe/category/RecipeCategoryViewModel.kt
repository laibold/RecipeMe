package de.hs_rm.recipe_me.ui.recipe.category

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.hs_rm.recipe_me.model.recipe.Recipe
import de.hs_rm.recipe_me.model.recipe.RecipeCategory
import de.hs_rm.recipe_me.service.repository.RecipeRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for [RecipeCategoryFragment]
 */
@HiltViewModel
class RecipeCategoryViewModel @Inject constructor(
    private val repository: RecipeRepository
) : ViewModel() {

    lateinit var category: RecipeCategory

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
