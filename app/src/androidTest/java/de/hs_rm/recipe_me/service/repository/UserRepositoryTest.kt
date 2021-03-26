package de.hs_rm.recipe_me.service.repository

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.platform.app.InstrumentationRegistry
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import de.hs_rm.recipe_me.Constants
import de.hs_rm.recipe_me.TestDataProvider
import de.hs_rm.recipe_me.declaration.getOrAwaitValue
import de.hs_rm.recipe_me.persistence.AppDatabase
import de.hs_rm.recipe_me.persistence.dao.UserDao
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject
import javax.inject.Named

@HiltAndroidTest
class UserRepositoryTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Inject
    @Named(Constants.TEST_NAME)
    lateinit var dao: UserDao

    @Inject
    @Named(Constants.TEST_NAME)
    lateinit var db: AppDatabase

    @Inject
    @Named(Constants.TEST_NAME)
    lateinit var repository: UserRepository

    private lateinit var appContext: Context

    @Before
    fun beforeEach() {
        hiltRule.inject()
        assertEquals(AppDatabase.Environment.TEST.dbName, db.openHelper.databaseName)

        appContext = InstrumentationRegistry.getInstrumentation().targetContext
        db.clearAllTables()
    }

    /**
     * Test inserting new user
     */
    @Test
    fun testInsertUser() {
        val nullUser = repository.getUser().getOrAwaitValue()
        assertNull(nullUser)

        val userName = TestDataProvider.getRandomString(6)
        runBlocking {
            repository.insertOrUpdate(userName)
        }

        val user = repository.getUser().getOrAwaitValue()
        assertNotNull(user)
        assertEquals(userName, user!!.name)
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
        assertNotNull(user)
        assertEquals(username, user!!.name)
    }
}
