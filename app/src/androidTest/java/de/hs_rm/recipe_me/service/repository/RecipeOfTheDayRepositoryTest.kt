package de.hs_rm.recipe_me.service.repository

import android.content.Context
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import de.hs_rm.recipe_me.TestDataProvider
import de.hs_rm.recipe_me.model.RecipeOfTheDay
import de.hs_rm.recipe_me.model.recipe.Recipe
import de.hs_rm.recipe_me.persistence.AppDatabase
import de.hs_rm.recipe_me.persistence.dao.RecipeDao
import de.hs_rm.recipe_me.persistence.dao.RecipeOfTheDayDao
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class RecipeOfTheDayRepositoryTest {

    private lateinit var db: AppDatabase
    private lateinit var rotdRepository: RecipeOfTheDayRepository
    private lateinit var recipeRepository: RecipeRepository
    private lateinit var rotdDao: RecipeOfTheDayDao
    private lateinit var recipeDao: RecipeDao

    private lateinit var appContext: Context

    /**
     * Build inMemory database and create repositories with DAOs from database
     */
    @Before
    fun init() {
        appContext = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(appContext, AppDatabase::class.java).build()
        recipeDao = db.recipeDao()
        rotdDao = db.recipeOfTheDayDao()

        rotdRepository = RecipeOfTheDayRepository(rotdDao, recipeDao)
        recipeRepository = RecipeRepository(recipeDao)
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
        db.clearAllTables()
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
        db.clearAllTables()
        insertTestData(1)
        var oldRotd: Recipe? = null
        var newRotd: Recipe? = null

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
        db.clearAllTables()
        insertTestData(2)
        var oldRotd: Recipe? = null
        var newRotd: Recipe?

        for (i in 0..100) {
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
        db.clearAllTables()
        insertTestData(2)
        val oldRotd = updateAndGetRotd()
        var recipeTotal = -1

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
        db.clearAllTables()
        insertTestData(1)
        var oldRotd: Recipe? = null
        var newRotd: Recipe? = null

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
    fun rotdWithoutRecipesSuccessful() {
        db.clearAllTables()
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
        var recipe: Recipe? = null
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
