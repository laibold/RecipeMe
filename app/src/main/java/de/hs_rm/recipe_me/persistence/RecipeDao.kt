package de.hs_rm.recipe_me.persistence

import androidx.lifecycle.LiveData
import androidx.room.*
import de.hs_rm.recipe_me.model.recipe.CookingStep
import de.hs_rm.recipe_me.model.recipe.Ingredient
import de.hs_rm.recipe_me.model.recipe.Recipe
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

    @Query("DELETE FROM Recipe")
    suspend fun clear()

    @Transaction
    @Query("SELECT * FROM Recipe")
    fun getRecipes(): LiveData<List<RecipeWithRelations>>
}
