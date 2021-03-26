package de.hs_rm.recipe_me.ui.recipe.category

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
import de.hs_rm.recipe_me.service.repository.RecipeRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject
import javax.inject.Named

@HiltAndroidTest
class RecipeCategoryViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    @Named(Constants.TEST_NAME)
    lateinit var db: AppDatabase

    @Inject
    @Named(Constants.TEST_NAME)
    lateinit var recipeRepository: RecipeRepository

    @Inject
    @Named(Constants.TEST_NAME)
    lateinit var recipeImageRepository: RecipeImageRepository

    @Inject
    @Named(Constants.TEST_NAME)
    lateinit var recipeDao: RecipeDao

    private lateinit var viewModel: RecipeCategoryViewModel

    private lateinit var appContext: Context

    /**
     * Build inMemory database and create ViewModel
     */
    @Before
    fun beforeEach() {
        hiltRule.inject()
        assertEquals(AppDatabase.Environment.TEST.dbName, db.openHelper.databaseName)

        viewModel = RecipeCategoryViewModel(recipeRepository, recipeImageRepository)
        appContext = InstrumentationRegistry.getInstrumentation().targetContext
        db.clearAllTables()
        insertTestData()
    }

    /**
     * Test that getRecipesByCategory returns only recipes with given category
     */
    @Test
    fun getRecipesByCategory() {
        val category = RecipeCategory.MAIN_DISHES
        val recipes = viewModel.getRecipesByCategory(category).getOrAwaitValue()

        assertEquals(3, recipes.size)

        for (recipe in recipes) {
            assertEquals(category, recipe.category)
        }
    }

    /**
     * Test that a recipe gets deleted successfully
     */
    @Test
    fun deleteRecipe() {
        val category = RecipeCategory.MAIN_DISHES
        val recipesBefore = viewModel.getRecipesByCategory(category).getOrAwaitValue()
        val countBefore = recipesBefore.size

        val recipeToDelete = recipesBefore[0]
        viewModel.deleteRecipeAndRelations(recipeToDelete)

        Thread.sleep(100)

        val recipesAfter = viewModel.getRecipesByCategory(category).getOrAwaitValue()
        val countAfter = recipesAfter.size

        assertFalse(recipesAfter.contains(recipeToDelete))
        assertEquals(countBefore, countAfter + 1)
    }

    ///////

    /**
     * Insert recipes with different categories to dao
     */
    private fun insertTestData() {
        val recipes = listOf(
            TestDataProvider.getRandomRecipe().apply { category = RecipeCategory.MAIN_DISHES },
            TestDataProvider.getRandomRecipe().apply { category = RecipeCategory.MAIN_DISHES },
            TestDataProvider.getRandomRecipe().apply { category = RecipeCategory.MAIN_DISHES },
            TestDataProvider.getRandomRecipe().apply { category = RecipeCategory.BREAKFAST },
            TestDataProvider.getRandomRecipe().apply { category = RecipeCategory.SNACKS },
            TestDataProvider.getRandomRecipe().apply { category = RecipeCategory.SALADS },
            TestDataProvider.getRandomRecipe().apply { category = RecipeCategory.SALADS },
            TestDataProvider.getRandomRecipe().apply { category = RecipeCategory.DESSERTS },
            TestDataProvider.getRandomRecipe().apply { category = RecipeCategory.DESSERTS }
        )

        for (recipe in recipes) {
            runBlocking { recipeDao.insert(recipe) }
        }
    }
}
