package de.hs_rm.recipe_me.service

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import de.hs_rm.recipe_me.declaration.getOrAwaitValue
import de.hs_rm.recipe_me.model.recipe.*
import de.hs_rm.recipe_me.model.relation.CookingStepIngredientCrossRef
import de.hs_rm.recipe_me.model.relation.CookingStepWithIngredients
import de.hs_rm.recipe_me.persistence.AppDatabase
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.Executors

@RunWith(AndroidJUnit4::class)
class RecipeRepositoryTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    lateinit var repository: RecipeRepository

    private lateinit var appContext: Context

    @Before
    fun init() {
        appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val db = Room.inMemoryDatabaseBuilder(appContext, AppDatabase::class.java)
            .setTransactionExecutor(Executors.newSingleThreadExecutor())
            .build()

        val recipeDao = db.recipeDao()
        repository = RecipeRepository(recipeDao)

        insertTestRecipe()
    }

    /**
     * Tests if added [Ingredient] has expected name, quantity and unit
     */
    @Test
    fun addIngredient() {
        val recipeName = "TestRecipe"
        val servings = 2
        val category = RecipeCategory.SNACKS
        val imageUri = "uri"
        var id = -1L

        runBlocking {
            id = repository.insert(Recipe(recipeName, servings, category, imageUri))
            repository.insert(Ingredient(id, "Ingredient1", 2.0, IngredientUnit.GRAM))
        }

        val recipe = repository.getRecipeWithRelationsById(id).getOrAwaitValue()

        assertEquals(recipe.ingredients.size, 1)
        assertEquals(
            recipe.ingredients[0].name,
            "Ingredient1"
        )
        assertEquals(
            recipe.ingredients[0].quantity,
            2.0, 0.0
        )
        assertEquals(
            recipe.ingredients[0].unit,
            IngredientUnit.GRAM
        )

    }

    /**
     * Tests if added [CookingStep] has expected  text, time and time unit
     */
    @Test
    fun addCookingStep() {
        val recipeName = "TestRecipe"
        val servings = 2
        val category = RecipeCategory.SNACKS
        val imageUri = "uri"
        var id = -1L

        runBlocking {
            id = repository.insert(Recipe(recipeName, servings, category, imageUri))
            repository.insert(CookingStep(id, "uri", "cook", 20, TimeUnit.SECOND))
        }

        val recipe = repository.getRecipeWithRelationsById(id).getOrAwaitValue()

        assertEquals(recipe.cookingStepsWithIngredients.size, 1)
        assertEquals(recipe.cookingStepsWithIngredients[0].cookingStep.text, "cook")
        assertEquals(recipe.cookingStepsWithIngredients[0].cookingStep.time, 20)
        assertEquals(recipe.cookingStepsWithIngredients[0].cookingStep.timeUnit, TimeUnit.SECOND)
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

        val recipe = repository.getRecipeWithRelationsById(id).getOrAwaitValue()

        recipes = repository.getRecipes().getOrAwaitValue()
        val sizeAfter = recipes.size

        assertEquals(sizeBefore + 1, sizeAfter)
        assertEquals(recipe.recipe.name, recipeName)
        assertEquals(recipe.recipe.servings, servings)
        assertEquals(recipe.recipe.category, category)
        assertEquals(recipe.recipe.imageUri, imageUri)

        assertEquals(recipe.ingredients.size, 2)
        assertEquals(recipe.cookingStepsWithIngredients.size, 3)
    }

    @Test
    fun deleteRecipeWithRelationsSucceed() {
        var recipes = repository.getRecipes().getOrAwaitValue()
        val sizeBefore = recipes.size

        // https://proandroiddev.com/testing-kotlin-coroutines-d904738b846d
        runBlocking { repository.deleteRecipeAndRelations(recipes[0].recipe) }

        recipes = repository.getRecipes().getOrAwaitValue()
        val sizeAfter = recipes.size

        assertEquals(sizeBefore - 1, sizeAfter)
    }

    /**
     * Test if after inserting CookingStep and belonging Ingredients a valid
     * [CookingStepWithIngredients] object can be requested from the repo
     */
    @Test
    fun insertCookingStepWithIngredientsSuccessful() {
        val ingredient1 = Ingredient("Ingredient1", 1.1, IngredientUnit.NONE)
        val ingredient2 = Ingredient("Ingredient2", 2.2, IngredientUnit.GRAM)
        val cookingStep = CookingStep("StepText", 2, TimeUnit.MINUTE)

        var recipeId = 0L

        runBlocking {
            recipeId = repository.insert(Recipe())

            cookingStep.recipeId = recipeId

            val stepId = repository.insert(cookingStep)

            val id1 = repository.insert(ingredient1)
            val id2 = repository.insert(ingredient2)

            repository.insert(CookingStepIngredientCrossRef(stepId, id1))
            repository.insert(CookingStepIngredientCrossRef(stepId, id2))
        }

        val recipe = repository.getRecipeWithRelationsById(recipeId).getOrAwaitValue()

        assertEquals(1, recipe.cookingStepsWithIngredients.size)
        assertEquals(cookingStep, recipe.cookingStepsWithIngredients[0].cookingStep)

        assertEquals(recipe.cookingStepsWithIngredients[0].ingredients.size, 2)
        assertEquals(recipe.cookingStepsWithIngredients[0].ingredients[0], ingredient1)
        assertEquals(recipe.cookingStepsWithIngredients[0].ingredients[1], ingredient2)
    }

    /**
     * Test if ingredients and cooking steps get deleted successfully
     */
    @Test
    fun deleteIngredientsAndCookingStepsSuccessful() {
        var recipeId = 0L

        runBlocking {
            recipeId = repository.insert(Recipe())

            repository.insert(CookingStep(recipeId, "", "", 0, TimeUnit.SECOND))
            repository.insert(CookingStep(recipeId, "", "", 0, TimeUnit.SECOND))

            repository.insert(Ingredient(recipeId, "", 0.0, IngredientUnit.NONE))
            repository.insert(Ingredient(recipeId, "", 0.0, IngredientUnit.NONE))
            repository.insert(Ingredient(recipeId, "", 0.0, IngredientUnit.NONE))
        }

        val recipe = repository.getRecipeWithRelationsById(recipeId).getOrAwaitValue()
        assertEquals(2, recipe.cookingStepsWithIngredients.size)
        assertEquals(3, recipe.ingredients.size)

        runBlocking { repository.deleteIngredientsAndCookingSteps(recipeId) }

        val recipeAfter = repository.getRecipeWithRelationsById(recipeId).getOrAwaitValue()
        assertEquals(0, recipeAfter.cookingStepsWithIngredients.size)
        assertEquals(0, recipeAfter.ingredients.size)
    }

    /////

    private fun insertTestRecipe() {
        runBlocking {
            repository.insert(Recipe("recipeName", 3, RecipeCategory.SNACKS, ""))
        }
    }

}
