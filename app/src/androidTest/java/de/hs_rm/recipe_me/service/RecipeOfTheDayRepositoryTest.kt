package de.hs_rm.recipe_me.service

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import de.hs_rm.recipe_me.TestDataProvider
import de.hs_rm.recipe_me.declaration.getOrAwaitValue
import de.hs_rm.recipe_me.model.RecipeOfTheDay
import de.hs_rm.recipe_me.model.recipe.Recipe
import de.hs_rm.recipe_me.persistence.AppDatabase
import de.hs_rm.recipe_me.persistence.RecipeDao
import de.hs_rm.recipe_me.persistence.RecipeOfTheDayDao
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import javax.inject.Inject

/**
 * https://developer.android.com/training/dependency-injection/hilt-testing
 */
@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class RecipeOfTheDayRepositoryTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private lateinit var rotdRepository: RecipeOfTheDayRepository
    private lateinit var recipeRepository: RecipeRepository

    @Inject
    lateinit var recipeDao: RecipeDao

    @Inject
    lateinit var rotdDao: RecipeOfTheDayDao

    private lateinit var appContext: Context
    private lateinit var db: AppDatabase

    /**
     * Inject dependencies, build inMemory database and create repositories with DAOs from database
     */
    @Before
    fun init() {
        hiltRule.inject()
        appContext = InstrumentationRegistry.getInstrumentation().targetContext

        db = Room.inMemoryDatabaseBuilder(appContext, AppDatabase::class.java).build()
        db.clearAllTables()

        rotdRepository = RecipeOfTheDayRepository(rotdDao, recipeDao)
        recipeRepository = RecipeRepository(recipeDao)
    }

    /**
     * Clear db after tests
     */
    @After
    fun cleanup() {
        db.clearAllTables()
    }

    /**
     * Test date comparison in rotdRepository.
     * Rots with date from yesterday are principally not valid anymore.
     */
    @Test
    fun compareDatesSuccessful() {
        val yesterdayDate = LocalDate.now().minusDays(1)
        val yesterdaysRotd = RecipeOfTheDay(yesterdayDate, 0)

        var result = rotdRepository.rotdInvalid(yesterdaysRotd)
        assertTrue(result)

        val todaysDate = LocalDate.now()
        val todaysRotd = RecipeOfTheDay(todaysDate, 0)

        result = rotdRepository.rotdInvalid(todaysRotd)
        assertFalse(result)
    }

    /**
     * Test if rotd gets created when no rotd is existing.
     */
    @Test
    fun createRotdFromNullSuccessful() {
        runBlocking { recipeDao.clear() }
        insertTestData(1)
        var rotd: Recipe? = null

        runBlocking { rotd = rotdRepository.getRecipeOfTheDay().getOrAwaitValue() }

        assertNotNull(rotd)
        assertNotEquals(rotd!!.name, "")

        assertCurrentRotdDateAndCount()
    }

    /**
     * Test if rotd stays exactly the same when there is only 1 recipe in the database
     * even if the date of the recipe is invalid.
     */
    @Test
    fun createRotdWithOneRecipeSuccessful() {
        runBlocking { recipeDao.clear() }
        insertTestData(1)
        lateinit var oldRotd: Recipe
        lateinit var newRotd: Recipe

        runBlocking {
            // Set current rotd's date to yesterday
            oldRotd = rotdRepository.getRecipeOfTheDay().getOrAwaitValue()
            val oldRotdObject = rotdDao.getRecipeOfTheDay()
            oldRotdObject!!.date = LocalDate.now().minusDays(1)
            rotdDao.update(oldRotdObject)
        }
        runBlocking {
            newRotd = rotdRepository.getRecipeOfTheDay().getOrAwaitValue()
            assertEquals(1, rotdDao.getCount())
        }

        assertNotNull(newRotd)
        assertEquals(oldRotd, newRotd)
    }

    /**
     * Test that when there are 2 recipes in the database, the rotd get's switched every time
     * the current rotd gets invalid (when it's date doesn't match today's one).
     */
    @Test
    fun switchRotdSuccessful() {
        runBlocking { recipeDao.clear() }
        insertTestData(2)
        lateinit var oldRotd: Recipe
        lateinit var newRotd: Recipe

        for (i in 0..100) {
            // Set current rotd's date to yesterday
            runBlocking {
                oldRotd = rotdRepository.getRecipeOfTheDay().getOrAwaitValue()

                val oldRotdObject = rotdDao.getRecipeOfTheDay()
                oldRotdObject!!.date = LocalDate.now().minusDays(1)
                rotdDao.update(oldRotdObject)
            }

            runBlocking { newRotd = rotdRepository.getRecipeOfTheDay().getOrAwaitValue() }

            assertNotNull(newRotd)
            assertNotEquals(oldRotd.id, newRotd.id)
            assertNotEquals(oldRotd, newRotd)
        }

        assertCurrentRotdDateAndCount()
    }

    /**
     * Test that rotd doesn't change when date is invalid (doesn't match today's one),
     * but there is no other recipe in the database.
     */
    @Test
    fun rotdWithOneRecipeSuccessful() {
        runBlocking { recipeDao.clear() }
        insertTestData(1)
        lateinit var oldRotd: Recipe
        lateinit var newRotd: Recipe

        runBlocking {
            oldRotd = rotdRepository.getRecipeOfTheDay().getOrAwaitValue()

            val oldRotdObject = rotdDao.getRecipeOfTheDay()
            oldRotdObject!!.date = LocalDate.now().minusDays(1)
            rotdDao.update(oldRotdObject)
        }

        runBlocking {
            newRotd = rotdRepository.getRecipeOfTheDay().getOrAwaitValue()
            assertEquals(1, rotdDao.getCount())
        }

        assertNotNull(newRotd)
        assertEquals(oldRotd, newRotd)
    }

    /**
     * Assert that current rotd's date match today's date and that there is only 1 rotd in the database
     */
    private fun assertCurrentRotdDateAndCount() {
        runBlocking {
            val currentRotdObject = rotdDao.getRecipeOfTheDay()
            assertEquals(LocalDate.now(), currentRotdObject!!.date)

            assertEquals(1, rotdDao.getCount())
        }
    }

    /**
     * Insert as many random recipes as wanted. every recipe will have 2 ingredients and 2 cooking steps
     * @param amount amount of recipes to be inserted
     */
    private fun insertTestData(amount: Int) {
        for (i in 1..amount) {
            runBlocking {
                val id = recipeRepository.insert(TestDataProvider.getRandomRecipe())
                for (j in 0..2) {
                    recipeRepository.insert(TestDataProvider.getRandomIngredient(id))
                    recipeRepository.insert(TestDataProvider.getRandomCookingStep(id))
                }
            }
        }
    }

}
