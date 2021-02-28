package de.hs_rm.recipe_me.persistence.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
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

    @Query("SELECT * FROM RecipeOfTheDay LIMIT 1")
    suspend fun getRecipeOfTheDay(): RecipeOfTheDay?

    @Query("SELECT COUNT(*) FROM RecipeOfTheDay")
    suspend fun getCount(): Int
}
