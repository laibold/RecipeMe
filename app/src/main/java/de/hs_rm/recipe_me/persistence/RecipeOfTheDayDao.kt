package de.hs_rm.recipe_me.persistence

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import de.hs_rm.recipe_me.model.RecipeOfTheDay

/**
 * Data Access Object for recipe of the day.
 */
@Dao
interface RecipeOfTheDayDao {

    @Insert
    suspend fun insert(recipeOfTheDay: RecipeOfTheDay)

    @Delete
    suspend fun delete(recipeOfTheDay: RecipeOfTheDay)

    @Query("SELECT * FROM RecipeOfTheDay LIMIT 1")
    suspend fun getRecipeOfTheDay(): RecipeOfTheDay?

}
