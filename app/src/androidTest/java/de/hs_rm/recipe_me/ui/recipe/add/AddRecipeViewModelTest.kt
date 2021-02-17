package de.hs_rm.recipe_me.ui.recipe.add

import android.app.Application
import android.content.Context
import android.text.Editable
import android.widget.EditText
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import de.hs_rm.recipe_me.TestDataProvider
import de.hs_rm.recipe_me.declaration.getOrAwaitValue
import de.hs_rm.recipe_me.model.recipe.*
import de.hs_rm.recipe_me.persistence.AppDatabase
import de.hs_rm.recipe_me.persistence.RecipeDao
import de.hs_rm.recipe_me.service.RecipeRepository
import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.Executors

@RunWith(AndroidJUnit4::class)
class AddRecipeViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var db: AppDatabase
    private lateinit var recipeRepository: RecipeRepository
    private lateinit var recipeDao: RecipeDao
    private lateinit var viewModel: AddRecipeViewModel

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
        recipeRepository = RecipeRepository(recipeDao)
    }

    /**
     * Clear all database tables and re-initialize ViewModel and it's recipe
     */
    private fun beforeEach() {
        db.clearAllTables()
        viewModel =
            AddRecipeViewModel(recipeRepository, appContext.applicationContext as Application)
        viewModel.setCategory(RecipeCategory.MAIN_DISHES)
        GlobalScope.launch(Dispatchers.Main) {
            delay(1000)
            viewModel.initRecipe(Recipe.DEFAULT_ID)
        }

        viewModel.recipe.getOrAwaitValue()
        viewModel.ingredients.getOrAwaitValue()
        viewModel.cookingStepsWithIngredients.getOrAwaitValue()
    }

    /**
     * Try to set recipe name and servings with invalid values. Function should return false.
     */
    @Test
    fun setRecipeAttributesUnsuccessful() {
        beforeEach()
        assertFalse(viewModel.setRecipeAttributes("", "", RecipeCategory.SNACKS))
        assertFalse(viewModel.setRecipeAttributes("", "1", RecipeCategory.SNACKS))
        assertFalse(viewModel.setRecipeAttributes("name", "", RecipeCategory.SNACKS))
        assertFalse(viewModel.setRecipeAttributes("name", "0", RecipeCategory.SNACKS))
    }

    /**
     * Set recipe name and servings with valid values. Function should return true.
     */
    @Test
    fun setRecipeAttributesSuccessful() {
        beforeEach()
        assertTrue(viewModel.setRecipeAttributes("n", "1", RecipeCategory.SNACKS))
    }

    /**
     * Add Ingredients with valid values, check return values and amount of ingredients.
     */
    @Test
    fun addIngredientSuccessful() {
        beforeEach()
        val name = getEditable("Valid Name")
        val quantity1 = getEditable("1.5")
        val quantity2 = getEditable("1,5")
        val quantity3 = getEditable("")
        val unit = IngredientUnit.NONE

        val countBefore = viewModel.ingredients.value?.size!!

        assertTrue(viewModel.addIngredient(name, quantity1, unit))
        assertTrue(viewModel.addIngredient(name, quantity2, unit))
        assertTrue(viewModel.addIngredient(name, quantity3, unit))

        val countAfter = viewModel.ingredients.value?.size!!

        assertEquals((countBefore + 3), countAfter)
    }

    /**
     * Add Ingredients with valid name, check return value and amount of ingredients (shouldn't increase).
     */
    @Test
    fun addIngredientUnsuccessful() {
        beforeEach()
        val nameInvalid = getEditable("")
        val quantityValid = getEditable("1.5")
        val unit = IngredientUnit.NONE

        val countBefore = viewModel.ingredients.value?.size!!

        assertFalse(viewModel.addIngredient(nameInvalid, quantityValid, unit))

        val countAfter = viewModel.ingredients.value?.size!!

        assertEquals(countBefore, countAfter)
    }

    /**
     * Update ingredient with valid values and check them as well as the amount of ingredients
     */
    @Test
    fun updateIngredientSuccessful() {
        beforeEach()
        insertTestData(3, 0)
        val position = 1
        val name = "new name"
        val quantity = 1.5
        val ingredientUnit = IngredientUnit.CLOVE

        viewModel.prepareIngredientUpdate(position)

        val countBefore = viewModel.ingredients.value?.size!!

        val success = viewModel.updateIngredient(
            getEditable(name),
            getEditable(quantity.toString()),
            ingredientUnit
        )
        assertTrue(success)

        val countAfter = viewModel.ingredients.value?.size!!

        assertEquals(name, viewModel.ingredients.value!![position].name)
        assertEquals(quantity, viewModel.ingredients.value!![position].quantity, 0.0)
        assertEquals(ingredientUnit, viewModel.ingredients.value!![position].unit)

        assertEquals(countBefore, countAfter)
    }

    /**
     * Update ingredient with invalid values and check them as well as the amount of ingredients (shouldn't change)
     */
    @Test
    fun updateIngredientUnsuccessful() {
        beforeEach()
        insertTestData(3, 0)
        val position = 1
        val name = ""
        val quantity = 1.5
        val ingredientUnit = IngredientUnit.CLOVE

        viewModel.prepareIngredientUpdate(position)

        val countBefore = viewModel.ingredients.value?.size!!

        val success = viewModel.updateIngredient(
            getEditable(name),
            getEditable(quantity.toString()),
            ingredientUnit
        )
        assertFalse(success)

        val countAfter = viewModel.ingredients.value?.size!!

        assertNotEquals(name, viewModel.ingredients.value!![position].name)
        assertNotEquals(quantity, viewModel.ingredients.value!![position].quantity, 0.0)
        assertNotEquals(ingredientUnit, viewModel.ingredients.value!![position].unit)

        assertEquals(countBefore, countAfter)
    }

    /**
     * Add CookingStepsWithIngredients with valid values, check return values and amount of cooking steps.
     */
    @Test
    fun addCookingStepSuccessful() {
        beforeEach()
        val text = getEditable("Valid Text")
        val time1 = getEditable("1")
        val time2 = getEditable("")
        val unit = TimeUnit.SECOND
        val ingredients = mutableListOf(TestDataProvider.getRandomIngredient(0))

        val countBefore = viewModel.cookingStepsWithIngredients.value?.size!!

        assertTrue(viewModel.addCookingStepWithIngredients(text, time1, unit, ingredients))
        assertTrue(viewModel.addCookingStepWithIngredients(text, time2, unit, ingredients))

        val countAfter = viewModel.cookingStepsWithIngredients.value?.size!!

        assertEquals((countBefore + 2), countAfter)
    }

    /**
     * Add CookingStepWithIngredients with invalid text, check return value and amount of CookingSteps (shouldn't increase).
     */
    @Test
    fun addCookingStepUnsuccessful() {
        beforeEach()
        val textInvalid = getEditable("")
        val timeValid = getEditable("3")
        val unit = TimeUnit.MINUTE
        val ingredients = mutableListOf(TestDataProvider.getRandomIngredient(0))

        val countBefore = viewModel.cookingStepsWithIngredients.value?.size!!

        assertFalse(
            viewModel.addCookingStepWithIngredients(
                textInvalid,
                timeValid,
                unit,
                ingredients
            )
        )

        val countAfter = viewModel.cookingStepsWithIngredients.value?.size!!

        assertEquals(countBefore, countAfter)
    }

    /**
     * Update CookingStep with valid values and check them as well as the amount of CookingSteps
     */
    @Test
    fun updateCookingStepSuccessful() {
        beforeEach()
        insertTestData(0, 3)
        val position = 1
        val text = "new text"
        val time = 50
        val unit = TimeUnit.MINUTE
        val ingredients = mutableListOf(TestDataProvider.getRandomIngredient(0))

        viewModel.prepareCookingStepUpdate(position)

        val countBefore = viewModel.cookingStepsWithIngredients.value?.size!!

        val success = viewModel.updateCookingStepWithIngredients(
            getEditable(text),
            getEditable(time.toString()),
            unit,
            ingredients
        )
        assertTrue(success)

        val countAfter = viewModel.cookingStepsWithIngredients.value?.size!!

        assertEquals(text, viewModel.cookingStepsWithIngredients.value!![position].cookingStep.text)
        assertEquals(time, viewModel.cookingStepsWithIngredients.value!![position].cookingStep.time)
        assertEquals(
            unit,
            viewModel.cookingStepsWithIngredients.value!![position].cookingStep.timeUnit
        )
        assertEquals(1, viewModel.cookingStepsWithIngredients.value!![position].ingredients.size)

        assertEquals(countBefore, countAfter)
    }

    /**
     * Update CookingStepWithIngredient with invalid values and check them as well as the amount of CookingStep (shouldn't change)
     */
    @Test
    fun updateCookingStepUnsuccessful() {
        beforeEach()
        insertTestData(0, 3)
        val position = 1
        val text = ""
        val time = 0
        val unit = TimeUnit.MINUTE
        val ingredient = Ingredient("", 0.0, IngredientUnit.PACK)
        val ingredients = mutableListOf(ingredient)

        viewModel.prepareCookingStepUpdate(position)

        val countBefore = viewModel.cookingStepsWithIngredients.value?.size!!

        val success = viewModel.updateCookingStepWithIngredients(
            getEditable(text),
            getEditable(time.toString()),
            unit,
            ingredients
        )
        assertFalse(success)

        val countAfter = viewModel.cookingStepsWithIngredients.value?.size!!

        assertNotEquals(
            text,
            viewModel.cookingStepsWithIngredients.value!![position].cookingStep.text
        )
        assertNotEquals(
            time,
            viewModel.cookingStepsWithIngredients.value!![position].cookingStep.time
        )
        assertNotEquals(
            unit,
            viewModel.cookingStepsWithIngredients.value!![position].cookingStep.timeUnit
        )
        assertNotEquals(
            ingredient,
            viewModel.cookingStepsWithIngredients.value!![position].ingredients[0]
        )

        assertEquals(countBefore, countAfter)
    }

    /**
     * Test persisting of entities and count them
     * https://stackoverflow.com/questions/51810330/testing-livedata-transformations
     */
    @Test
    fun persistEntitiesSuccessful() {
        val numberOfChildren = 3

        beforeEach()
        insertTestData(numberOfChildren, numberOfChildren)

        val recipe = viewModel.recipe.getOrAwaitValue()

        assertNotNull(recipe)

        val id = viewModel.persistEntities().getOrAwaitValue()

        assertNotEquals(0L, id)
        assertEquals(1, recipeRepository.getRecipeTotal().getOrAwaitValue())

        val recipeWithRelations = recipeRepository.getRecipeWithRelationsById(id).getOrAwaitValue()

        assertEquals(numberOfChildren, recipeWithRelations.ingredients.size)
        assertEquals(numberOfChildren, recipeWithRelations.cookingStepsWithIngredients.size)
    }

    /**
     * Test update of all entities
     */
    @Test
    fun updateEntitiesSuccessful() {
        val numberOfChildren = 2

        val name = "New Name"
        val servings = "6"
        val category = RecipeCategory.BREAKFAST

        beforeEach()
        insertTestData(numberOfChildren, numberOfChildren)

        val recipeId = viewModel.persistEntities().getOrAwaitValue(10)

        // Test if test insertion succeeded (May be deleted)
        val recipeWithRelations2 =
            recipeRepository.getRecipeWithRelationsById(recipeId).getOrAwaitValue(10)
        assertEquals(numberOfChildren, recipeWithRelations2.ingredients.size)
        assertEquals(numberOfChildren, recipeWithRelations2.cookingStepsWithIngredients.size)

        // Initialize ViewModel with id
        viewModel =
            AddRecipeViewModel(recipeRepository, appContext.applicationContext as Application)
        GlobalScope.launch(Dispatchers.Main) {
            delay(1000)
            viewModel.initRecipe(recipeId)
        }

        viewModel.recipe.getOrAwaitValue()
        viewModel.ingredients.getOrAwaitValue()
        viewModel.cookingStepsWithIngredients.getOrAwaitValue()

        // change name, servings and category
        viewModel.setRecipeAttributes(name, servings, category)

        // add ingredient
        viewModel.addIngredient(getEditable("New Ingredient"),
            getEditable("1"),
            IngredientUnit.NONE)
        // delete ingredient
        viewModel.ingredients.value!!.removeAt(1)
        // edit ingredient
        viewModel.prepareIngredientUpdate(0)
        viewModel.updateIngredient(getEditable("Updated Ingredient"),
            getEditable("2"),
            IngredientUnit.CAN)

        // add step
        viewModel.addCookingStepWithIngredients(getEditable("New Step"),
            getEditable(""),
            TimeUnit.SECOND,
            mutableListOf())
        // edit step
        // remove step
        // change (add/remove) ingredient in step

        val newRecipeId = viewModel.persistEntities().getOrAwaitValue()
        assertNotEquals(Recipe.DEFAULT_ID, newRecipeId)

        val recipeWithRelations =
            recipeRepository.getRecipeWithRelationsById(newRecipeId).getOrAwaitValue(10)

        assertEquals(name, recipeWithRelations.recipe.name)
        assertEquals(servings.toInt(), recipeWithRelations.recipe.servings)
        assertEquals(category, recipeWithRelations.recipe.category)

        assertEquals(2, recipeWithRelations.ingredients.size)
        assertEquals(Ingredient("Updated Ingredient", 2.0, IngredientUnit.CAN),
            recipeWithRelations.ingredients[0])
        assertEquals(Ingredient("New Ingredient", 1.0, IngredientUnit.NONE),
            recipeWithRelations.ingredients[1])

        assertEquals(3, recipeWithRelations.cookingStepsWithIngredients.size)
        assertEquals(CookingStep("New Step", 0, TimeUnit.SECOND),
            recipeWithRelations.cookingStepsWithIngredients[2].cookingStep)
    }

    /**
     * Test validating name
     */
    @Test
    fun validateNameSuccessful() {
        beforeEach()
        assertEquals(0, viewModel.validateName(getEditable("n")))
    }

    /**
     * Test validating name
     */
    @Test
    fun validateNameUnsuccessful() {
        beforeEach()
        assertNotEquals(0, viewModel.validateName(getEditable("")))
    }

    /**
     * Test validating servings
     */
    @Test
    fun validateServingsSuccessful() {
        beforeEach()
        assertEquals(0, viewModel.validateServings(getEditable("1")))
    }

    /**
     * Test validating servings
     */
    @Test
    fun validateServingsUnsuccessful() {
        beforeEach()
        assertNotEquals(0, viewModel.validateServings(getEditable("0")))
        assertNotEquals(0, viewModel.validateServings(getEditable("")))
    }

    /////////////////////////////////////////////////////

    /**
     * Mock Editable text from EditText
     */
    private fun getEditable(s: String): Editable {
        val editText = EditText(appContext)
        editText.setText(s)
        return editText.text
    }

    /**
     * Insert as many random ingredients and cooking steps to ViewModel as wanted.
     *
     * @param ingredients amount of ingredients to be inserted
     * @param cookingSteps amount of cookingSteps to be inserted
     */
    private fun insertTestData(ingredients: Int, cookingSteps: Int) {
        runBlocking {
            for (j in 1..ingredients) {
                viewModel.addIngredient(
                    getEditable("Inserted name"),
                    getEditable("99"),
                    IngredientUnit.PINCH
                )
            }
            for (j in 1..cookingSteps) {
                val ingredientList = mutableListOf(TestDataProvider.getRandomIngredient(0))
                viewModel.addCookingStepWithIngredients(
                    getEditable("Inserted text"),
                    getEditable("9"),
                    TimeUnit.HOUR,
                    ingredientList
                )
            }
        }
    }

}
