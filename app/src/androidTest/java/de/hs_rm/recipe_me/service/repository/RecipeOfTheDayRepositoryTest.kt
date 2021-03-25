package de.hs_rm.recipe_me.service.repository

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import de.hs_rm.recipe_me.Constants
import de.hs_rm.recipe_me.TestDataProvider
import de.hs_rm.recipe_me.model.RecipeOfTheDay
import de.hs_rm.recipe_me.model.recipe.Recipe
import de.hs_rm.recipe_me.persistence.AppDatabase
import de.hs_rm.recipe_me.persistence.dao.RecipeDao
import de.hs_rm.recipe_me.persistence.dao.RecipeOfTheDayDao
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Named

@HiltAndroidTest
class RecipeOfTheDayRepositoryTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    @Named(Constants.TEST_NAME)
    lateinit var db: AppDatabase

    @Inject
    @Named(Constants.TEST_NAME)
    lateinit var rotdRepository: RecipeOfTheDayRepository

    @Inject
    @Named(Constants.TEST_NAME)
    lateinit var recipeRepository: RecipeRepository

    @Inject
    @Named(Constants.TEST_NAME)
    lateinit var rotdDao: RecipeOfTheDayDao

    @Inject
    @Named(Constants.TEST_NAME)
    lateinit var recipeDao: RecipeDao

    private lateinit var context: Context

    /**
     * Build inMemory database and create repositories with DAOs from database
     */
    @Before
    fun beforeEach() {
        hiltRule.inject()
        assertEquals(AppDatabase.Environment.TEST.dbName, db.openHelper.databaseName)

        context = InstrumentationRegistry.getInstrumentation().targetContext
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
        insertTestData(1)
        val rotd = updateAndGetRotd()

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
        insertTestData(1)
        var oldRotd: Recipe?
        var newRotd: Recipe?

        runBlocking {
            // Set current rotd's date to yesterday
            oldRotd = updateAndGetRotd()
            val oldRotdObject = rotdDao.getRecipeOfTheDay()

            oldRotdObject!!.date = LocalDate.now().minusDays(1)
            rotdDao.update(oldRotdObject)
        }
        runBlocking {
            newRotd = updateAndGetRotd()
            assertEquals(1, rotdDao.getCount())
        }

        assertNotNull(newRotd)
        assertEquals(oldRotd, newRotd)
    }

    /**
     * Test that when there are 2 recipes in the database, the rotd get's switched every time
     * the current rotd gets invalid (when its date doesn't match today's one).
     */
    @Test
    fun switchRotdSuccessful() {
        insertTestData(2)
        var oldRotd: Recipe?
        var newRotd: Recipe?

        for (i in 0..25) {
            // Set current rotd's date to yesterday
            runBlocking {
                oldRotd = updateAndGetRotd()

                val oldRotdObject = rotdDao.getRecipeOfTheDay()
                oldRotdObject!!.date = LocalDate.now().minusDays(1)
                rotdDao.update(oldRotdObject)
            }

            newRotd = updateAndGetRotd()

            assertNotNull(newRotd)
            assertNotEquals(oldRotd!!.id, newRotd!!.id)
            assertNotEquals(oldRotd, newRotd)
        }

        assertCurrentRotdDateAndCount()
    }

    /**
     * Tests that if the Recipe that is rotd gets deleted, the rotd object will also be deleted
     * and a new rotd will be created
     */
    @Test
    fun newRotdOnRecipeDeleted() {
        insertTestData(2)
        val oldRotd = updateAndGetRotd()
        var recipeTotal: Int

        runBlocking {
            recipeRepository.deleteRecipeAndRelations(oldRotd!!)

            assertEquals(0, rotdDao.getCount())
            recipeTotal = recipeDao.getRecipeCount()
        }

        assertEquals(1, recipeTotal)

        val newRotd = updateAndGetRotd()

        assertNotEquals(newRotd, oldRotd)
        assertCurrentRotdDateAndCount()
    }

    /**
     * Test that rotd doesn't change when date is invalid (doesn't match today's one),
     * but there is no other recipe in the database.
     */
    @Test
    fun rotdWithOneRecipeSuccessful() {
        insertTestData(1)
        var oldRotd: Recipe?
        var newRotd: Recipe?

        runBlocking {
            oldRotd = updateAndGetRotd()

            val oldRotdObject = rotdDao.getRecipeOfTheDay()
            oldRotdObject!!.date = LocalDate.now().minusDays(1)
            rotdDao.update(oldRotdObject)
        }

        runBlocking {
            newRotd = updateAndGetRotd()
            assertEquals(1, rotdDao.getCount())
        }

        assertNotNull(newRotd)
        assertEquals(oldRotd, newRotd)
    }

    /**
     * Try to create a rotd without recipes in database. There should be no error,
     * but dao must be empty
     */
    @Test
    fun rotdWithoutRecipesSuccessful() {
        runBlocking {
            assertEquals(0, recipeDao.getRecipeCount())

            val rotd = updateAndGetRotd()
            assertNull(rotd)
            assertEquals(0, rotdDao.getCount())
        }
    }

    /**
     * Updates recipe of the day and returns the new [Recipe]
     */
    private fun updateAndGetRotd(): Recipe? {
        var recipe: Recipe?
        runBlocking {
            rotdRepository.updateRecipeOfTheDay()
            val rotdId = rotdRepository.getRecipeOfTheDayId()
            recipe = recipeRepository.getRecipeById(rotdId)
        }
        return recipe
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
