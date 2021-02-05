package de.hs_rm.recipe_me.persistence

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import de.hs_rm.recipe_me.model.user.User

/**
 * Data Access Object for user.
 */
@Dao
interface UserDao {

    @Insert
    suspend fun insert(user: User): Long

    @Update
    suspend fun update(user: User)

    @Query("DELETE FROM User")
    suspend fun clear()

    @Query("SELECT * FROM User LIMIT 1")
    suspend fun getUser(): User?

}