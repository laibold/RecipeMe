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

    @Before
    fun init() {
        hiltRule.inject()
        appContext = InstrumentationRegistry.getInstrumentation().targetContext

        db = Room.inMemoryDatabaseBuilder(appContext, AppDatabase::class.java).build()
        db.clearAllTables()

        rotdRepository = RecipeOfTheDayRepository(rotdDao, recipeDao)
        recipeRepository = RecipeRepository(recipeDao)
    }

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

    @Test
    fun createRotdFromNullSuccessful() {
        runBlocking { recipeDao.clear() }
        insertTestData(1)
        var rotd: Recipe? = null

        runBlocking { rotd = rotdRepository.getRecipeOfTheDay().getOrAwaitValue() }

        assertNotNull(rotd)
        assertNotEquals(rotd!!.name, "")

        runBlocking {
            val currentRotdObject = rotdDao.getRecipeOfTheDay()
            assertEquals(LocalDate.now(), currentRotdObject!!.date)
        }
    }

    @Test
    fun createRotdWithOneRecipeSuccessful() {
        runBlocking { recipeDao.clear() }
        insertTestData(1)
        lateinit var oldRotd: Recipe
        lateinit var newRotd: Recipe

        runBlocking { oldRotd = rotdRepository.getRecipeOfTheDay().getOrAwaitValue() }
        runBlocking { newRotd = rotdRepository.getRecipeOfTheDay().getOrAwaitValue() }

        assertNotNull(newRotd)
        assertEquals(oldRotd, newRotd)

        runBlocking {
            val currentRotdObject = rotdDao.getRecipeOfTheDay()
            assertEquals(LocalDate.now(), currentRotdObject!!.date)
        }
    }

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

        runBlocking {
            val currentRotdObject = rotdDao.getRecipeOfTheDay()
            assertEquals(LocalDate.now(), currentRotdObject!!.date)
        }
    }

    // TODO test no change when still valid

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