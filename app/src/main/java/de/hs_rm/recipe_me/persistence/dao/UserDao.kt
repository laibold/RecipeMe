package de.hs_rm.recipe_me.persistence.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import de.hs_rm.recipe_me.model.user.User

/**
 * Data Access Object for [User].
 */
@Dao
interface UserDao {

    @Insert
    suspend fun insert(user: User)

    @Update
    suspend fun update(user: User)

    @Query("DELETE FROM User")
    suspend fun clear()

    @Query("SELECT * FROM User LIMIT 1")
    fun getUserAsLiveData(): LiveData<User?>

    @Query("SELECT * FROM User LIMIT 1")
    suspend fun getUser(): User?

}
