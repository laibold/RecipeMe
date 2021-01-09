package de.hs_rm.recipe_me.service

import androidx.lifecycle.LiveData
import de.hs_rm.recipe_me.model.recipe.*
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
     * Insert [Recipe] to repository
     * @return id of inserted Recipe
     */
    suspend fun insert(recipe: Recipe): Long {
        return recipeDao.insert(recipe)
    }

    /**
     * Insert [CookingStep] to repository
     * @return id of inserted CookingStep
     */
    suspend fun insert(cookingStep: CookingStep): Long {
        return recipeDao.insert(cookingStep)
    }

    /**
     * Insert [Ingredient] to repository
     * @return id of inserted Ingredient
     */
    suspend fun insert(ingredient: Ingredient): Long {
        return recipeDao.insert(ingredient)
    }

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

}
