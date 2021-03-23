package de.hs_rm.recipe_me.ui.recipe.add

import android.content.Context
import android.text.Editable
import android.widget.EditText
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import de.hs_rm.recipe_me.Constants
import de.hs_rm.recipe_me.TestDataProvider
import de.hs_rm.recipe_me.declaration.getOrAwaitValue
import de.hs_rm.recipe_me.model.recipe.*
import de.hs_rm.recipe_me.persistence.AppDatabase
import de.hs_rm.recipe_me.persistence.dao.RecipeDao
import de.hs_rm.recipe_me.service.repository.RecipeImageRepository
import de.hs_rm.recipe_me.service.repository.RecipeRepository
import kotlinx.coroutines.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject
import javax.inject.Named

@HiltAndroidTest
class AddRecipeViewModelTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Inject
    @Named(Constants.TEST_NAME)
    lateinit var db: AppDatabase

    @Inject
    @Named(Constants.TEST_NAME)
    lateinit var recipeRepository: RecipeRepository

    @Inject
    @Named(Constants.TEST_NAME)
    lateinit var recipeImageRepository: RecipeImageRepository

    private lateinit var viewModel: AddRecipeViewModel

    private lateinit var appContext: Context

    /**
     * Build inMemory database and create ViewModel
     */
    @Before
    fun beforeEach() {
        hiltRule.inject()
        assertEquals(AppDatabase.Environment.TEST.dbName, db.openHelper.databaseName)

        appContext = InstrumentationRegistry.getInstrumentation().targetContext
        db.clearAllTables()
        viewModel = AddRecipeViewModel(recipeRepository, recipeImageRepository)
        viewModel.setCategory(RecipeCategory.MAIN_DISHES)
        GlobalScope.launch(Dispatchers.Main) {
            delay(1000) // TODO notwendig?
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
        assertTrue(viewModel.setRecipeAttributes("n", "1", RecipeCategory.SNACKS))
    }

    /**
     * Add Ingredients with valid values, check return values and amount of ingredients.
     */
    @Test
    fun addIngredientSuccessful() {
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
     * Test that if an ingredients gets removed in viewModel scope, it wont be assigned to a
     * CookingStep in viewModel scope anymore
     */
    @Test
    fun deleteAssignedIngredientSuccessful() {
        viewModel.addIngredient(getEditable("Delete"), getEditable("1.0"), IngredientUnit.NONE)
        viewModel.addIngredient(getEditable("Keep"), getEditable("1.0"), IngredientUnit.NONE)

        val deleteIngredient = viewModel.ingredients.value!![0]
        val keepIngredient = viewModel.ingredients.value!![1]

        viewModel.addCookingStepWithIngredients(
            getEditable("Text"),
            getEditable("0"),
            TimeUnit.SECOND,
            mutableListOf(deleteIngredient, keepIngredient)
        )

        val ingredientsBefore = viewModel.cookingStepsWithIngredients.value?.get(0)!!.ingredients
        val beforeCountList = ArrayList<Ingredient>(ingredientsBefore)
        assertEquals(2, beforeCountList.size)

        viewModel.ingredients.value!!.remove(deleteIngredient)
        Thread.sleep(1000)
        viewModel.prepareCookingStepUpdate(0)

        val ingredientsAfter = viewModel.cookingStepsWithIngredients.value?.get(0)!!.ingredients
        val afterCountList = ArrayList<Ingredient>(ingredientsAfter)
        assertEquals(1, afterCountList.size)
    }

    /**
     * Add CookingStepsWithIngredients with valid values, check return values and amount of cooking steps.
     */
    @Test
    fun addCookingStepSuccessful() {
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
        insertTestData(2, 3)
        val position = 1
        val text = ""
        val time = 0
        val unit = TimeUnit.MINUTE
        val ingredient = viewModel.ingredients.getOrAwaitValue()[0]
        val ingredients = mutableListOf(ingredient)

        viewModel.prepareCookingStepUpdate(position)

        val countBefore = viewModel.cookingStepsWithIngredients.getOrAwaitValue().size

        val success = viewModel.updateCookingStepWithIngredients(
            getEditable(text),
            getEditable(time.toString()),
            unit,
            ingredients
        )
        assertFalse(success)

        val countAfter = viewModel.cookingStepsWithIngredients.getOrAwaitValue().size

        assertEquals(countBefore, countAfter)

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
    }

    /**
     * Test scenario:
     * Recipe with 3 Ingredients as been persisted. All 3 Ingredients are assigned to a CookingStep.
     * One Ingredient gets removed, one updated, one unchanged; User moves to CookingStep Fragment to edit the CookingStep.
     * Check if the ingredients assigned to the CookingStep are up-to-date
     */
    @Test
    fun updateAssignedIngredientsSuccessful() {
        val numberOfChildren = 3
        val cookingStepIndex = 0
        val updateIndex = 0

        insertTestData(numberOfChildren, numberOfChildren)

        viewModel.prepareCookingStepUpdate(cookingStepIndex)

        // Assign all 3 ingredients to CookingStep
        val ingredient0 = viewModel.ingredients.value!![0]
        val ingredient1 = viewModel.ingredients.value!![1]
        val ingredient2 = viewModel.ingredients.value!![2]
        val assignedIngredients = mutableListOf(ingredient0, ingredient1, ingredient2)

        viewModel.updateCookingStepWithIngredients(
            getEditable("CookingStepName"),
            getEditable("0"),
            TimeUnit.SECOND,
            assignedIngredients
        )

        // Persist recipe
        val id = viewModel.persistEntities().getOrAwaitValue()

        GlobalScope.launch(Dispatchers.Main) {
            delay(1000)
            viewModel.initRecipe(id)
        }

        viewModel.recipe.getOrAwaitValue()
        viewModel.ingredients.getOrAwaitValue()
        viewModel.cookingStepsWithIngredients.getOrAwaitValue()

        val newName = "New Name"
        val newQuantity = 6.0
        val newIngredientUnit = IngredientUnit.DASH

        // Update ingredient at index 0
        viewModel.prepareIngredientUpdate(updateIndex)
        viewModel.updateIngredient(
            getEditable(newName),
            getEditable(newQuantity.toString()),
            newIngredientUnit
        )

        //Remove ingredient at index 2
        viewModel.ingredients.value!!.removeAt(2)

        // This happens when CookingStepDialog gets opened - Assigned ingredients will be refreshed
        viewModel.prepareCookingStepUpdate(cookingStepIndex)

        val cookingStepWithIngredients =
            viewModel.cookingStepsWithIngredients.value!![cookingStepIndex]

        assertEquals(2, cookingStepWithIngredients.ingredients.size)

        val updatedAssignedIngredient = cookingStepWithIngredients.ingredients[updateIndex]
        assertEquals(newName, updatedAssignedIngredient.name)
        assertEquals(newQuantity, updatedAssignedIngredient.quantity, 0.0)
        assertEquals(newIngredientUnit, updatedAssignedIngredient.unit)

        val unchangedIngredient = cookingStepWithIngredients.ingredients[1]
        assertTrue(unchangedIngredient == ingredient1)
    }

    /**
     * Test persisting of entities and count them
     * https://stackoverflow.com/questions/51810330/testing-livedata-transformations
     */
    @Test
    fun persistEntitiesSuccessful() {
        val numberOfChildren = 3

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
//    @Test
    fun updateEntitiesSuccessful() {
        val numberOfChildren = 2

        val name = "New Name"
        val servings = "6"
        val category = RecipeCategory.BREAKFAST

        insertTestData(numberOfChildren, numberOfChildren)

        val recipeId = viewModel.persistEntities().getOrAwaitValue(10)

        // Test if test insertion succeeded (May be deleted)
        val recipeWithRelations2 =
            recipeRepository.getRecipeWithRelationsById(recipeId).getOrAwaitValue(10)
        assertEquals(numberOfChildren, recipeWithRelations2.ingredients.size)
        assertEquals(numberOfChildren, recipeWithRelations2.cookingStepsWithIngredients.size)

        GlobalScope.launch(Dispatchers.Main) {
            delay(1000)
            viewModel.initRecipe(1)
        }

        viewModel.recipe.getOrAwaitValue()
        viewModel.ingredients.getOrAwaitValue()
        viewModel.cookingStepsWithIngredients.getOrAwaitValue()

        // change name, servings and category
        viewModel.setRecipeAttributes(name, servings, category)

        // add ingredient
        viewModel.addIngredient(
            getEditable("New Ingredient"),
            getEditable("1"),
            IngredientUnit.NONE
        )
        // delete ingredient
        viewModel.ingredients.value!!.removeAt(1)
        // edit ingredient
        viewModel.prepareIngredientUpdate(0)
        viewModel.updateIngredient(
            getEditable("Updated Ingredient"),
            getEditable("2"),
            IngredientUnit.CAN
        )

        // add step
        viewModel.addCookingStepWithIngredients(
            getEditable("New Step"),
            getEditable(""),
            TimeUnit.SECOND,
            mutableListOf()
        )
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
        assertEquals(
            Ingredient("Updated Ingredient", 2.0, IngredientUnit.CAN),
            recipeWithRelations.ingredients[0]
        )
        assertEquals(
            Ingredient("New Ingredient", 1.0, IngredientUnit.NONE),
            recipeWithRelations.ingredients[1]
        )

        assertEquals(3, recipeWithRelations.cookingStepsWithIngredients.size)
        assertEquals(
            CookingStep("New Step", 0, TimeUnit.SECOND),
            recipeWithRelations.cookingStepsWithIngredients[2].cookingStep
        )
    }

    /**
     * Test validating name
     */
    @Test
    fun validateNameSuccessful() {
        assertEquals(0, viewModel.validateName(getEditable("n")))
    }

    /**
     * Test validating name
     */
    @Test
    fun validateNameUnsuccessful() {
        assertNotEquals(0, viewModel.validateName(getEditable("")))
    }

    /**
     * Test validating servings
     */
    @Test
    fun validateServingsSuccessful() {
        assertEquals(0, viewModel.validateServings(getEditable("1")))
    }

    /**
     * Test validating servings
     */
    @Test
    fun validateServingsUnsuccessful() {
        assertNotEquals(0, viewModel.validateServings(getEditable("0")))
        assertNotEquals(0, viewModel.validateServings(getEditable("")))
    }

    /**
     * Test if variables get reset on initRecipe()
     */
    @Test
    fun clearValuesSuccessful() {
        insertTestData(1, 1)
        viewModel.setRecipeAttributes("Name", "1", RecipeCategory.BREAKFAST)
        viewModel.initRecipe(Recipe.DEFAULT_ID)

        assertNotNull(viewModel.ingredients.value)
        assertEquals(0, viewModel.ingredients.value!!.size)

        assertNotNull(viewModel.cookingStepsWithIngredients)
        assertEquals(0, viewModel.cookingStepsWithIngredients.value!!.size)

        assertEquals("", viewModel.recipe.value!!.name)
        // servings is private and can't be tested
        assertEquals(RecipeCategory.MAIN_DISHES, viewModel.category.value)

        // recipeToUpdate is private and can't be tested here
    }

    /**
     * Test if a recipe can be created successful after another recipe has been updated before
     */
//    @Test
    fun updateAndCreateRecipeSuccessful() {
//        insertTestData(1, 1)
        val id = viewModel.persistEntities().getOrAwaitValue()
        assertEquals(1, recipeRepository.getRecipeTotal().getOrAwaitValue())
        // recipe saved

        GlobalScope.launch(Dispatchers.Main) {
            delay(1000)
            viewModel.initRecipe(id)
        }

        viewModel.recipe.getOrAwaitValue()
        viewModel.ingredients.getOrAwaitValue()
        viewModel.cookingStepsWithIngredients.getOrAwaitValue()

        viewModel.persistEntities().getOrAwaitValue(20)
        assertEquals(1, recipeRepository.getRecipeTotal().getOrAwaitValue())
        // recipe updated

        viewModel.setCategory(RecipeCategory.MAIN_DISHES)
        GlobalScope.launch(Dispatchers.Main) {
            delay(1000)
            viewModel.initRecipe(Recipe.DEFAULT_ID)
        }

        viewModel.recipe.getOrAwaitValue()
        viewModel.ingredients.getOrAwaitValue()
        viewModel.cookingStepsWithIngredients.getOrAwaitValue()

//        insertTestData(1, 1)
        viewModel.recipe.getOrAwaitValue()
        viewModel.persistEntities().getOrAwaitValue()
        assertEquals(2, recipeRepository.getRecipeTotal().getOrAwaitValue())
        // second recipe saved
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
                    getEditable("Inserted name $j"),
                    getEditable("$j"),
                    IngredientUnit.PINCH
                )
            }
            for (j in 1..cookingSteps) {
                viewModel.addCookingStepWithIngredients(
                    getEditable("Inserted text $j"),
                    getEditable("$j"),
                    TimeUnit.HOUR,
                    mutableListOf()
                )
            }
        }
    }

}
