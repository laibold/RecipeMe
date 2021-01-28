package de.hs_rm.recipe_me.persistence

import androidx.room.*
import de.hs_rm.recipe_me.model.RecipeOfTheDay

/**
 * Data Access Object for recipe of the day.
 */
@Dao
interface RecipeOfTheDayDao {

    @Insert
    suspend fun insert(recipeOfTheDay: RecipeOfTheDay): Long

    @Update
    suspend fun update(recipeOfTheDay: RecipeOfTheDay)

    @Query("DELETE FROM RecipeOfTheDay")
    suspend fun clear()

    @Query("SELECT * FROM RecipeOfTheDay LIMIT 1")
    suspend fun getRecipeOfTheDay(): RecipeOfTheDay?

}
