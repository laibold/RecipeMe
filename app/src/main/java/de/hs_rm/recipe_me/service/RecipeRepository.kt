package de.hs_rm.recipe_me.service

import androidx.lifecycle.LiveData
import de.hs_rm.recipe_me.model.recipe.Recipe
import de.hs_rm.recipe_me.model.recipe.RecipeCategory
import de.hs_rm.recipe_me.model.relation.RecipeWithRelations
import de.hs_rm.recipe_me.persistence.RecipeDao
import javax.inject.Inject

/**
 * Single Source of Truth for [Recipe]. Use it with Dependency Injection
 */
class RecipeRepository @Inject constructor(
    private val recipeDao: RecipeDao
) {

    /**
     * Get all recipes
     */
    fun getRecipes(): LiveData<List<RecipeWithRelations>> {
        return recipeDao.getRecipes()
    }

    /**
     * Clear recipes
     */
    suspend fun clearRecipes() {
        recipeDao.clear()
    }

    /**
     * Get recipes by category
     */
    fun getRecipesByCategory(recipeCategory: RecipeCategory): LiveData<List<Recipe>> {
        return recipeDao.getRecipesByCategory(recipeCategory)
    }

    suspend fun deleteRecipeWithRelations(recipe: RecipeWithRelations) {
        recipeDao.delete(recipe.recipe)
        for (ingredient in recipe.ingredients) {
            recipeDao.delete(ingredient)
        }
        for (cookingStep in recipe.cookingSteps) {
            recipeDao.delete(cookingStep)
        }
    }

}
