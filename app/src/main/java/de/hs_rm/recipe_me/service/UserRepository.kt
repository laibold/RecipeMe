package de.hs_rm.recipe_me.service

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
     * Insert or update given [User]
     */
    suspend fun insertOrUpdate(user: User) {
        when (userDao.getUser()) {
            null -> {
                userDao.insert(user)
            }
            else -> {
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
     * @return id of the current [User], -1 when there is none
     */
    suspend fun getUser(): Long {
        val user = userDao.getUser()
        return user?.id ?: -1
    }


}