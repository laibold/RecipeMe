package de.hs_rm.recipe_me.service.repository

import androidx.lifecycle.LiveData
import de.hs_rm.recipe_me.model.user.User
import de.hs_rm.recipe_me.persistence.UserDao
import javax.inject.Inject

/**
 * Single Source of Truth for [User]. Use it with Dependency Injection
 */
class UserRepository @Inject constructor(
    private val userDao: UserDao
) {

    /**
     * Insert or update [User] with given name
     */
    suspend fun insertOrUpdate(name: String) {
        when (val user = userDao.getUser()) {
            null -> {
                userDao.insert(User(name))
            }
            else -> {
                user.name = name
                userDao.update(user)
            }
        }
    }

    /**
     * Clear [User]
     */
    suspend fun clear() {
        userDao.clear()
    }

    /**
     * @return current [User] as LiveData
     */
    fun getUser(): LiveData<User?> {
        return userDao.getUserAsLiveData()
    }

}
