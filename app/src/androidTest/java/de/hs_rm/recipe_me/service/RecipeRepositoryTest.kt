package de.hs_rm.recipe_me.service

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import de.hs_rm.recipe_me.declaration.getOrAwaitValue
import de.hs_rm.recipe_me.model.recipe.*
import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

/**
 * https://developer.android.com/training/dependency-injection/hilt-testing
 */
@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class RecipeRepositoryTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Inject
    lateinit var repository: RecipeRepository

    private lateinit var appContext: Context

    @Before
    fun init() {
        hiltRule.inject()
        appContext = InstrumentationRegistry.getInstrumentation().targetContext
        insertTestRecipe()
    }

    @Test
    fun testInjection() {
        assertNotNull(repository)
    }
    
    fun addIngredient() {
        // TODO
    }

    fun addCookingStep() {
        // TODO
    }

    @Test
    fun addRecipeWithRelationsSucceed() {
        val recipeName = "TestRecipe"
        val servings = 2
        val category = RecipeCategory.SNACKS
        val imageUri = "uri"

        var recipes = repository.getRecipes().getOrAwaitValue()
        val sizeBefore = recipes.size

        var id = -1L

        runBlocking {
            id = repository.insert(Recipe(recipeName, servings, category, imageUri))
            repository.insert(Ingredient(id, "Ingredient1", 2.0, IngredientUnit.GRAM))
            repository.insert(Ingredient(id, "Ingredient2", 0.0, IngredientUnit.NONE))
            repository.insert(CookingStep(id, "uri", "cook", 20, TimeUnit.SECOND))
            repository.insert(CookingStep(id, "uri", "serve", 0, TimeUnit.SECOND))
            repository.insert(CookingStep(id, "uri", "put to dishwasher", 1, TimeUnit.HOUR))
        }

        val recipe = repository.getRecipeById(id).getOrAwaitValue(2)

        recipes = repository.getRecipes().getOrAwaitValue()
        val sizeAfter = recipes.size

        assertEquals(sizeBefore + 1, sizeAfter)
        assertEquals(recipe.recipe.name, recipeName)
        assertEquals(recipe.recipe.servings, servings)
        assertEquals(recipe.recipe.category, category)
        assertEquals(recipe.recipe.imageUri, imageUri)

        assertEquals(recipe.ingredients.size, 2)
        assertEquals(recipe.cookingSteps.size, 3)
    }

    @Test
    fun deleteRecipeWithRelationsSucceed() {
        var recipes = repository.getRecipes().getOrAwaitValue()
        val sizeBefore = recipes.size

        // https://proandroiddev.com/testing-kotlin-coroutines-d904738b846d
        runBlocking { repository.deleteRecipeAndRelations(recipes[0].recipe) }

        recipes = repository.getRecipes().getOrAwaitValue()
        val sizeAfter = recipes.size

        assertEquals(sizeBefore, sizeAfter + 1)
    }

    private fun insertTestRecipe() {
        runBlocking {
            repository.insert(Recipe("recipeName", 3, RecipeCategory.SNACKS, ""))
        }
    }

}
