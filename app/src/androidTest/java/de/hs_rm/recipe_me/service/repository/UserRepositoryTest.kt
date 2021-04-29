package de.hs_rm.recipe_me.service.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import de.hs_rm.recipe_me.persistence.AppDatabase
import de.hs_rm.recipe_me.persistence.dao.UserDao
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import test_shared.TestDataProvider
import test_shared.declaration.getOrAwaitValue

class UserRepositoryTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    lateinit var dao: UserDao
    lateinit var repository: UserRepository

    @Before
    fun beforeEach() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val db = Room.inMemoryDatabaseBuilder(appContext, AppDatabase::class.java).build()
        dao = db.userDao()
        repository = UserRepository(dao)
    }

    /**
     * Test inserting new user
     */
    @Test
    fun testInsertUser() {
        val nullUser = repository.getUser().getOrAwaitValue()
        assertThat(nullUser).isNull()

        val username = TestDataProvider.getRandomString(6)
        runBlocking {
            repository.insertOrUpdate(username)
        }

        val user = repository.getUser().getOrAwaitValue()
        assertThat(user).isNotNull()
        assertThat(user!!.name).isEqualTo(username)
    }

    /**
     * Test updating existing user
     */
    @Test
    fun testUpdateUser() {
        runBlocking {
            repository.insertOrUpdate("oldName")
        }

        val username = TestDataProvider.getRandomString(6)
        runBlocking {
            repository.insertOrUpdate(username)
        }

        val user = repository.getUser().getOrAwaitValue()
        assertThat(user).isNotNull()
        assertThat(user!!.name).isEqualTo(username)
    }
}
