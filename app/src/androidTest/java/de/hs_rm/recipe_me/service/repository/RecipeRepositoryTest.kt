package de.hs_rm.recipe_me.service.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import de.hs_rm.recipe_me.model.recipe.*
import de.hs_rm.recipe_me.model.relation.CookingStepIngredientCrossRef
import de.hs_rm.recipe_me.model.relation.CookingStepWithIngredients
import de.hs_rm.recipe_me.persistence.AppDatabase
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import test_shared.declaration.getOrAwaitValue

class RecipeRepositoryTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    lateinit var repository: RecipeRepository

    @Before
    fun beforeEach() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val db = Room.inMemoryDatabaseBuilder(appContext, AppDatabase::class.java).build()
        val dao = db.recipeDao()
        repository = RecipeRepository(dao)
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
        var id: Long

        runBlocking {
            id = repository.insert(Recipe(recipeName, servings, category))
            repository.insert(Ingredient(id, "Ingredient1", 2.0, IngredientUnit.GRAM))
        }

        val recipe = repository.getRecipeWithRelationsById(id).getOrAwaitValue()

        assertThat(recipe.ingredients.size).isEqualTo(1)
        assertThat(recipe.ingredients[0].name).isEqualTo("Ingredient1")
        assertThat(recipe.ingredients[0].quantity).isEqualTo(2.0)
        assertThat(recipe.ingredients[0].unit).isEqualTo(IngredientUnit.GRAM)
    }

    /**
     * Tests if added [CookingStep] has expected  text, time and time unit
     */
    @Test
    fun addCookingStep() {
        val recipeName = "TestRecipe"
        val servings = 2
        val category = RecipeCategory.SNACKS
        var id: Long

        runBlocking {
            id = repository.insert(Recipe(recipeName, servings, category))
            repository.insert(CookingStep(id, "cook", 20, TimeUnit.SECOND))
        }

        val recipe = repository.getRecipeWithRelationsById(id).getOrAwaitValue()

        assertThat(recipe.cookingStepsWithIngredients.size).isEqualTo(1)
        assertThat(recipe.cookingStepsWithIngredients[0].cookingStep.text).isEqualTo("cook")
        assertThat(recipe.cookingStepsWithIngredients[0].cookingStep.time).isEqualTo(20)
        assertThat(recipe.cookingStepsWithIngredients[0].cookingStep.timeUnit).isEqualTo(TimeUnit.SECOND)
    }

    @Test
    fun addRecipeWithRelationsSucceed() {
        val recipeName = "TestRecipe"
        val servings = 2
        val category = RecipeCategory.SNACKS

        var recipes = repository.getRecipes().getOrAwaitValue()
        val sizeBefore = recipes.size

        var id: Long

        runBlocking {
            id = repository.insert(Recipe(recipeName, servings, category))
            repository.insert(Ingredient(id, "Ingredient1", 2.0, IngredientUnit.GRAM))
            repository.insert(Ingredient(id, "Ingredient2", 0.0, IngredientUnit.NONE))
            repository.insert(CookingStep(id, "cook", 20, TimeUnit.SECOND))
            repository.insert(CookingStep(id, "serve", 0, TimeUnit.SECOND))
            repository.insert(CookingStep(id, "put to dishwasher", 1, TimeUnit.HOUR))
        }

        val recipe = repository.getRecipeWithRelationsById(id).getOrAwaitValue()

        recipes = repository.getRecipes().getOrAwaitValue()
        val sizeAfter = recipes.size

        assertThat(sizeAfter).isEqualTo(sizeBefore + 1)
        assertThat(recipe.recipe.name).isEqualTo(recipeName)
        assertThat(recipe.recipe.servings).isEqualTo(servings)
        assertThat(recipe.recipe.category).isEqualTo(category)

        assertThat(recipe.ingredients.size).isEqualTo(2)
        assertThat(recipe.cookingStepsWithIngredients.size).isEqualTo(3)
    }

    @Test
    fun deleteRecipeWithRelationsSucceed() {
        var recipes = repository.getRecipes().getOrAwaitValue()
        val sizeBefore = recipes.size

        // https://proandroiddev.com/testing-kotlin-coroutines-d904738b846d
        runBlocking { repository.deleteRecipeAndRelations(recipes[0].recipe) }

        recipes = repository.getRecipes().getOrAwaitValue()
        val sizeAfter = recipes.size

        assertThat(sizeAfter).isEqualTo(sizeBefore - 1)
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

        var recipeId: Long

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

        assertThat(recipe.cookingStepsWithIngredients.size).isEqualTo(1)
        assertThat(recipe.cookingStepsWithIngredients[0].cookingStep).isEqualTo(cookingStep)

        assertThat(recipe.cookingStepsWithIngredients[0].ingredients.size).isEqualTo(2)
        assertThat(recipe.cookingStepsWithIngredients[0].ingredients[0]).isEqualTo(ingredient1)
        assertThat(recipe.cookingStepsWithIngredients[0].ingredients[1]).isEqualTo(ingredient2)
    }

    /**
     * Test if ingredients and cooking steps get deleted successfully
     */
    @Test
    fun deleteIngredientsAndCookingStepsSuccessful() {
        var recipeId: Long

        runBlocking {
            recipeId = repository.insert(Recipe())

            repository.insert(CookingStep(recipeId, "", 0, TimeUnit.SECOND))
            repository.insert(CookingStep(recipeId, "", 0, TimeUnit.SECOND))

            repository.insert(Ingredient(recipeId, "", 0.0, IngredientUnit.NONE))
            repository.insert(Ingredient(recipeId, "", 0.0, IngredientUnit.NONE))
            repository.insert(Ingredient(recipeId, "", 0.0, IngredientUnit.NONE))
        }

        val recipe = repository.getRecipeWithRelationsById(recipeId).getOrAwaitValue()
        assertThat(recipe.cookingStepsWithIngredients.size).isEqualTo(2)
        assertThat(recipe.ingredients.size).isEqualTo(3)

        runBlocking { repository.deleteIngredientsAndCookingSteps(recipeId) }

        val recipeAfter = repository.getRecipeWithRelationsById(recipeId).getOrAwaitValue()
        assertThat(recipeAfter.cookingStepsWithIngredients.size).isEqualTo(0)
        assertThat(recipeAfter.ingredients.size).isEqualTo(0)
    }

    /////

    private fun insertTestRecipe() {
        runBlocking {
            repository.insert(Recipe("recipeName", 3, RecipeCategory.SNACKS))
        }
    }
}
