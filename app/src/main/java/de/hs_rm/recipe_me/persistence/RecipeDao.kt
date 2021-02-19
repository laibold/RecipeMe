package de.hs_rm.recipe_me.persistence

import androidx.lifecycle.LiveData
import androidx.room.*
import de.hs_rm.recipe_me.model.recipe.CookingStep
import de.hs_rm.recipe_me.model.recipe.Ingredient
import de.hs_rm.recipe_me.model.recipe.Recipe
import de.hs_rm.recipe_me.model.recipe.RecipeCategory
import de.hs_rm.recipe_me.model.relation.CookingStepIngredientCrossRef
import de.hs_rm.recipe_me.model.relation.RecipeWithRelations

/**
 * Data Access Object for Recipes with their relations.
 * Insert [Recipe], [Ingredient] and [CookingStep] connected by foreign keys and request [RecipeWithRelations]
 */
@Dao
interface RecipeDao {

    @Insert
    suspend fun insert(recipe: Recipe): Long

    @Insert
    suspend fun insert(ingredient: Ingredient): Long

    @Insert
    suspend fun insert(cookingStep: CookingStep): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(cookingStepIngredientCrossRef: CookingStepIngredientCrossRef)

    @Update
    suspend fun update(recipe: Recipe)

    @Update
    suspend fun update(ingredient: Ingredient)

    @Update
    suspend fun update(cookingStep: CookingStep)

    @Delete
    suspend fun delete(recipe: Recipe)

    @Query("DELETE FROM Ingredient WHERE recipeId = :recipeId")
    suspend fun deleteIngredients(recipeId: Long)

    @Query("DELETE FROM CookingStep WHERE recipeId = :recipeId")
    suspend fun deleteCookingSteps(recipeId: Long)

    @Transaction
    @Query("SELECT * FROM Recipe")
    fun getRecipes(): LiveData<List<RecipeWithRelations>>

    @Query("SELECT * FROM Recipe WHERE category = :recipeCategory ORDER BY name COLLATE NOCASE ASC")
    fun getRecipesByCategory(recipeCategory: RecipeCategory): LiveData<List<Recipe>>

    @Transaction
    @Query("SELECT * FROM Recipe WHERE id = :id")
    fun getRecipeWithRelationsById(id: Long): LiveData<RecipeWithRelations>

    @Query("SELECT * FROM Recipe WHERE id = :id")
    suspend fun getRecipeById(id: Long): Recipe

    @Query("SELECT * FROM Recipe WHERE id = :id")
    fun getRecipeByIdAsLiveData(id: Long): LiveData<Recipe>

    @Query("SELECT COUNT(*) FROM Recipe")
    fun getRecipeCountAsLiveData(): LiveData<Int>

    @Query("SELECT COUNT(*) FROM Recipe")
    suspend fun getRecipeCount(): Int

    @Query("SELECT * FROM Recipe LIMIT 1 OFFSET :offset")
    suspend fun getRecipeByOffset(offset: Int): Recipe

    @Delete
    suspend fun deleteIngredient(ingredient: Ingredient)

    @Delete
    suspend fun deleteCookingStep(cookingStep: CookingStep)

    @Query("DELETE FROM CookingStepIngredientCrossRef WHERE ingredientId = :ingredientId OR cookingStepId = :cookingStepId")
    suspend fun deleteCookingStepIngredientCrossRefs(
        ingredientId: Long = Ingredient.DEFAULT_ID,
        cookingStepId: Long = CookingStep.DEFAULT_ID
    )

    @Query("DELETE FROM CookingStepIngredientCrossRef" +
            " WHERE ingredientId" +
            " IN (SELECT ingredientId FROM Ingredient WHERE recipeId = :recipeId)" +
            " OR cookingStepId" +
            " IN (SELECT cookingStepId FROM CookingStep WHERE recipeId = :recipeId)")
    suspend fun deleteCookingStepIngredientCrossRefsByRecipeId(recipeId: Long)

}
