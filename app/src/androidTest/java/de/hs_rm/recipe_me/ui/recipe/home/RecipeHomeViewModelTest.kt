package de.hs_rm.recipe_me.ui.recipe.home

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.platform.app.InstrumentationRegistry
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import de.hs_rm.recipe_me.Constants
import de.hs_rm.recipe_me.TestDataProvider
import de.hs_rm.recipe_me.declaration.getOrAwaitValue
import de.hs_rm.recipe_me.model.recipe.RecipeCategory
import de.hs_rm.recipe_me.persistence.AppDatabase
import de.hs_rm.recipe_me.persistence.dao.RecipeDao
import de.hs_rm.recipe_me.service.repository.RecipeImageRepository
import de.hs_rm.recipe_me.service.repository.RecipeOfTheDayRepository
import de.hs_rm.recipe_me.service.repository.RecipeRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject
import javax.inject.Named

@HiltAndroidTest
class RecipeHomeViewModelTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Inject
    @Named(Constants.TEST_NAME)
    lateinit var db: AppDatabase

    @Inject
    @Named(Constants.TEST_NAME)
    lateinit var recipeDao: RecipeDao

    @Inject
    @Named(Constants.TEST_NAME)
    lateinit var recipeRepository: RecipeRepository

    @Inject
    @Named(Constants.TEST_NAME)
    lateinit var recipeImageRepository: RecipeImageRepository

    @Inject
    @Named(Constants.TEST_NAME)
    lateinit var recipeOfTheDayRepository: RecipeOfTheDayRepository

    private lateinit var viewModel: RecipeHomeViewModel

    private lateinit var appContext: Context

    /**
     * Build inMemory database and create ViewModel
     */
    @Before
    fun beforeEach() {
        hiltRule.inject()
        assertEquals(AppDatabase.Environment.TEST.dbName, db.openHelper.databaseName)

        viewModel =
            RecipeHomeViewModel(recipeOfTheDayRepository, recipeRepository, recipeImageRepository)
        appContext = InstrumentationRegistry.getInstrumentation().targetContext
        db.clearAllTables()
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
