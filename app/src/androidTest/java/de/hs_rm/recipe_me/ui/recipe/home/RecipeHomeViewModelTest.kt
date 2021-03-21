package de.hs_rm.recipe_me.ui.recipe.home

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import de.hs_rm.recipe_me.TestDataProvider
import de.hs_rm.recipe_me.declaration.getOrAwaitValue
import de.hs_rm.recipe_me.model.recipe.RecipeCategory
import de.hs_rm.recipe_me.persistence.AppDatabase
import de.hs_rm.recipe_me.persistence.dao.RecipeDao
import de.hs_rm.recipe_me.persistence.dao.RecipeOfTheDayDao
import de.hs_rm.recipe_me.service.repository.RecipeImageRepository
import de.hs_rm.recipe_me.service.repository.RecipeOfTheDayRepository
import de.hs_rm.recipe_me.service.repository.RecipeRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.Executors

@RunWith(AndroidJUnit4::class)
class RecipeHomeViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var db: AppDatabase
    private lateinit var recipeDao: RecipeDao
    private lateinit var recipeOfTheDayDao: RecipeOfTheDayDao
    private lateinit var recipeRepository: RecipeRepository
    private lateinit var recipeImageRepository: RecipeImageRepository
    private lateinit var recipeOfTheDayRepository: RecipeOfTheDayRepository
    private lateinit var viewModel: RecipeHomeViewModel

    private lateinit var appContext: Context

    /**
     * Build inMemory database and create ViewModel
     */
    @Before
    fun init() {
        appContext = InstrumentationRegistry.getInstrumentation().targetContext

        db = Room.inMemoryDatabaseBuilder(appContext, AppDatabase::class.java)
            .setTransactionExecutor(Executors.newSingleThreadExecutor())
            .build()

        recipeDao = db.recipeDao()
        recipeOfTheDayDao = db.recipeOfTheDayDao()
        recipeOfTheDayRepository = RecipeOfTheDayRepository(recipeOfTheDayDao, recipeDao)
        recipeRepository = RecipeRepository(recipeDao)
        recipeImageRepository = RecipeImageRepository(appContext)
        viewModel =
            RecipeHomeViewModel(recipeOfTheDayRepository, recipeRepository, recipeImageRepository)

        insertTestData()
    }

    /**
     * Test if recipeOfTheDay gets loaded
     */
    @Test
    fun loadRecipeOfTheDay() {
        viewModel.loadRecipeOfTheDay()
        assertNotNull(viewModel.recipeOfTheDay.getOrAwaitValue())
    }

    ///////

    /**
     * Insert recipes with different categories to dao
     */
    private fun insertTestData() {
        val recipes = listOf(
            TestDataProvider.getRandomRecipe().apply { category = RecipeCategory.MAIN_DISHES },
            TestDataProvider.getRandomRecipe().apply { category = RecipeCategory.BREAKFAST },
            TestDataProvider.getRandomRecipe().apply { category = RecipeCategory.SALADS }
        )

        for (recipe in recipes) {
            runBlocking { recipeDao.insert(recipe) }
        }
    }
}
