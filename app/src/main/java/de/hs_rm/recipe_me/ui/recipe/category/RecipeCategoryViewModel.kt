package de.hs_rm.recipe_me.ui.recipe.category

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.hs_rm.recipe_me.model.recipe.Recipe
import de.hs_rm.recipe_me.model.recipe.RecipeCategory
import de.hs_rm.recipe_me.service.repository.RecipeImageRepository
import de.hs_rm.recipe_me.service.repository.RecipeRepository
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

/**
 * ViewModel for [RecipeCategoryFragment]
 */
@HiltViewModel
class RecipeCategoryViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository,
    private val imageRepository: RecipeImageRepository
) : ViewModel() {

    lateinit var category: RecipeCategory

    /**
     * Returns LiveData with list of recipes that match the given [RecipeCategory]
     */
    fun getRecipesByCategory(recipeCategory: RecipeCategory): LiveData<List<Recipe>> {
        return recipeRepository.getRecipesByCategory(recipeCategory)
    }

    /**
     * Returns file of recipeImage that can be loaded into view via Glide.
     * If no image is available, null will be returned
     */
    fun getRecipeImageFile(recipeId: Long): File? {
        return imageRepository.getRecipeImageFile(recipeId)
    }

    /**
     * Delete recipe and its belonging Ingredients and CookingSteps
     */
    fun deleteRecipeAndRelations(recipe: Recipe) {
        imageRepository.deleteRecipeImage(recipe.id)
        viewModelScope.launch {
            recipeRepository.deleteRecipeAndRelations(recipe)
        }
    }

}
