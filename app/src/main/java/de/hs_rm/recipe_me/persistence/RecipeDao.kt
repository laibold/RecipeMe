package de.hs_rm.recipe_me.persistence

import androidx.lifecycle.LiveData
import androidx.room.*
import de.hs_rm.recipe_me.model.recipe.CookingStep
import de.hs_rm.recipe_me.model.recipe.Ingredient
import de.hs_rm.recipe_me.model.recipe.Recipe
import de.hs_rm.recipe_me.model.recipe.RecipeCategory
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

    @Delete
    suspend fun delete(recipe: Recipe)

    @Delete
    suspend fun delete(ingredient: Ingredient)

    @Delete
    suspend fun delete(cookingStep: CookingStep)

    @Query("DELETE FROM Recipe")
    suspend fun clear()

    @Transaction
    @Query("SELECT * FROM Recipe")
    fun getRecipes(): LiveData<List<RecipeWithRelations>>

    @Query("SELECT * FROM Recipe WHERE category = :recipeCategory")
    fun getRecipesByCategory(recipeCategory: RecipeCategory): LiveData<List<Recipe>>
}
