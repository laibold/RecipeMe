package de.hs_rm.recipe_me.service

import androidx.lifecycle.LiveData
import de.hs_rm.recipe_me.model.recipe.CookingStep
import de.hs_rm.recipe_me.model.recipe.Ingredient
import de.hs_rm.recipe_me.model.recipe.Recipe
import de.hs_rm.recipe_me.model.recipe.RecipeCategory
import de.hs_rm.recipe_me.model.relation.CookingStepIngredientCrossRef
import de.hs_rm.recipe_me.model.relation.RecipeWithRelations
import de.hs_rm.recipe_me.persistence.RecipeDao
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Single Source of Truth for [Recipe]. Use it with Dependency Injection
 */
@Singleton
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

    suspend fun insert(cookingStepIngredientCrossRef: CookingStepIngredientCrossRef) {
        recipeDao.insert(cookingStepIngredientCrossRef)
    }

    /**
     * Get all recipes
     */
    fun getRecipes(): LiveData<List<RecipeWithRelations>> {
        return recipeDao.getRecipes()
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
     * Get a Recipe by its id
     */
    fun getRecipeByIdAsLiveData(id: Long): LiveData<Recipe> {
        return recipeDao.getRecipeByIdAsLiveData(id)
    }

    /**
     * Get total of recipes
     */
    fun getRecipeTotal(): LiveData<Int> {
        return recipeDao.getRecipeCountAsLiveData()
    }

    /**
     * Update [Recipe]
     * @return id of updated recipe
     */
    suspend fun update(recipe: Recipe) {
        recipeDao.update(recipe)
    }

    /**
     * Update [Ingredient]
     */
    suspend fun update(ingredient: Ingredient) {
        recipeDao.update(ingredient)
    }

    /**
     * Update [CookingStep]
     */
    suspend fun update(cookingStep: CookingStep) {
        recipeDao.update(cookingStep)
    }

    /**
     * Update List of CookingSteps
     */
    @JvmName("updateCookingSteps")
    suspend fun update(cookingSteps: List<CookingStep>) {
        for (cookingStep in cookingSteps) {
            update(cookingStep)
        }
    }

    /**
     * Delete given Ingredient and its relations from database
     */
    suspend fun deleteIngredient(ingredient: Ingredient) {
        deleteCookingStepIngredientCrossRefs(ingredientId = ingredient.ingredientId)
        recipeDao.deleteIngredient(ingredient)
    }

    /**
     * Delete given CookingStep and its relations from database
     */
    suspend fun deleteCookingStep(cookingStep: CookingStep) {
        deleteCookingStepIngredientCrossRefs(cookingStepId = cookingStep.cookingStepId)
        recipeDao.deleteCookingStep(cookingStep)
    }

    /**
     * Delete recipe and it's belonging Ingredients and CookingSteps
     */
    suspend fun deleteRecipeAndRelations(recipe: Recipe) {
        deleteIngredientsAndCookingSteps(recipe.id)
        recipeDao.delete(recipe)
    }

    /**
     * Delete belonging Ingredients and CookingSteps with given recipeId
     */
    suspend fun deleteIngredientsAndCookingSteps(recipeId: Long) {
        recipeDao.deleteIngredients(recipeId)
        recipeDao.deleteCookingSteps(recipeId)
    }

    /**
     * Delete all cross references relating to the given Ingredient or CookingStep ids
     */
    suspend fun deleteCookingStepIngredientCrossRefs(
        ingredientId: Long = Ingredient.DEFAULT_ID,
        cookingStepId: Long = CookingStep.DEFAULT_ID
    ) {
        recipeDao.deleteCookingStepIngredientCrossRefs(ingredientId, cookingStepId)
    }

}
