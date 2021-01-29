package de.hs_rm.recipe_me.service

import androidx.lifecycle.LiveData
import de.hs_rm.recipe_me.model.recipe.Recipe
import de.hs_rm.recipe_me.model.recipe.RecipeCategory
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
     * Insert List of [CookingStep]s to repository
     */
    @JvmName("insertCookingSteps")
    suspend fun insert(cookingSteps: MutableList<CookingStep>) {
        for (cookingStep in cookingSteps) {
            insert(cookingStep)
        }
    }

    /**
     * Insert [Ingredient] to repository
     * @return id of inserted Ingredient
     */
    suspend fun insert(ingredient: Ingredient): Long {
        return recipeDao.insert(ingredient)
    }

    /**
     * Insert List of [Ingredient]s to repository
     */
    @JvmName("insertIngredients")
    suspend fun insert(ingredients: MutableList<Ingredient>) {
        for (ingredient in ingredients) {
            insert(ingredient)
        }
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

    /**
     * Get a RecipeWithRelations by its id
     */
    fun getRecipeWithRelationsById(id: Long): LiveData<RecipeWithRelations> {
        return recipeDao.getRecipeWithRelationsById(id)
    }

    /**
     * Get a Recipe by its id
     */
    suspend fun getRecipeById(id: Long): Recipe {
        return recipeDao.getRecipeById(id)
    }

    /**
     * Get total of recipes
     */
    fun getRecipeTotal(): LiveData<Int> {
        return recipeDao.getRecipeCountAsLiveData()
    }

    /**
     * Delete recipe and it's belonging Ingredients and CookingSteps
     */
    suspend fun deleteRecipeAndRelations(recipe: Recipe) {
        recipeDao.deleteIngredients(recipe.id)
        recipeDao.deleteCookingSteps(recipe.id)
        recipeDao.delete(recipe)
    }

}
