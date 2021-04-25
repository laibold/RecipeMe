package de.hs_rm.recipe_me.ui.recipe.add

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import de.hs_rm.recipe_me.Constants
import test_shared.TestDataProvider
import test_shared.declaration.getOrAwaitValue
import de.hs_rm.recipe_me.declaration.toEditable
import de.hs_rm.recipe_me.model.recipe.*
import de.hs_rm.recipe_me.persistence.AppDatabase
import de.hs_rm.recipe_me.service.repository.RecipeImageRepository
import de.hs_rm.recipe_me.service.repository.RecipeRepository
import kotlinx.coroutines.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
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
        assertThat(db.openHelper.databaseName).isEqualTo(AppDatabase.Environment.TEST.dbName)

        appContext = InstrumentationRegistry.getInstrumentation().targetContext
        db.clearAllTables()
        viewModel = AddRecipeViewModel(recipeRepository, recipeImageRepository)
        viewModel.setCategory(RecipeCategory.MAIN_DISHES)
        viewModel.initRecipe(Recipe.DEFAULT_ID)

        viewModel.recipe.getOrAwaitValue()
        viewModel.ingredients.getOrAwaitValue()
        viewModel.cookingStepsWithIngredients.getOrAwaitValue()
    }

    /**
     * Try to set recipe name and servings with invalid values. Function should return false.
     */
    @Test
    fun setRecipeAttributesUnsuccessful() {
        assertThat(viewModel.setRecipeAttributes("", "", RecipeCategory.SNACKS))
            .isFalse()

        assertThat(viewModel.setRecipeAttributes("", "1", RecipeCategory.SNACKS))
            .isFalse()

        assertThat(viewModel.setRecipeAttributes("name", "", RecipeCategory.SNACKS))
            .isFalse()

        assertThat(viewModel.setRecipeAttributes("name", "0", RecipeCategory.SNACKS))
            .isFalse()
    }

    /**
     * Set recipe name and servings with valid values. Function should return true.
     */
    @Test
    fun setRecipeAttributesSuccessful() {
        assertThat(viewModel.setRecipeAttributes("n", "1", RecipeCategory.SNACKS))
            .isTrue()
    }

    /**
     * Add Ingredients with valid values, check return values and amount of ingredients.
     */
    @Test
    fun addIngredientSuccessful() {
        val name = "Valid Name".toEditable()
        val quantity1 = (1.5).toEditable()
        val quantity2 = "1,5".toEditable()
        val quantity3 = "".toEditable()
        val unit = IngredientUnit.NONE

        val countBefore = viewModel.ingredients.value?.size!!

        assertThat(viewModel.addIngredient(name, quantity1, unit)).isTrue()
        assertThat(viewModel.addIngredient(name, quantity2, unit)).isTrue()
        assertThat(viewModel.addIngredient(name, quantity3, unit)).isTrue()

        val countAfter = viewModel.ingredients.value?.size!!
        assertThat(countAfter).isEqualTo((countBefore + 3))
    }

    /**
     * Add Ingredients with valid name, check return value and amount of ingredients (shouldn't increase).
     */
    @Test
    fun addIngredientUnsuccessful() {
        val nameInvalid = "".toEditable()
        val quantityValid = (1.5).toEditable()
        val unit = IngredientUnit.NONE

        val countBefore = viewModel.ingredients.value?.size!!

        assertThat(viewModel.addIngredient(nameInvalid, quantityValid, unit)).isFalse()

        val countAfter = viewModel.ingredients.value?.size!!
        assertThat(countAfter).isEqualTo(countBefore)
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
            name.toEditable(),
            quantity.toEditable(),
            ingredientUnit
        )
        assertThat(success).isTrue()

        val countAfter = viewModel.ingredients.value?.size!!

        assertThat(viewModel.ingredients.value!![position].name).isEqualTo(name)
        assertThat(viewModel.ingredients.value!![position].quantity).isEqualTo(quantity)
        assertThat(viewModel.ingredients.value!![position].unit).isEqualTo(ingredientUnit)

        assertThat(countAfter).isEqualTo(countBefore)
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
            name.toEditable(),
            quantity.toEditable(),
            ingredientUnit
        )
        assertThat(success).isFalse()

        val countAfter = viewModel.ingredients.value?.size!!

        assertThat(viewModel.ingredients.value!![position].name).isNotEqualTo(name)
        assertThat(viewModel.ingredients.value!![position].quantity).isNotEqualTo(quantity)
        assertThat(viewModel.ingredients.value!![position].unit).isNotEqualTo(ingredientUnit)

        assertThat(countAfter).isEqualTo(countBefore)
    }

    /**
     * Test that if an ingredients gets removed in viewModel scope, it wont be assigned to a
     * CookingStep in viewModel scope anymore
     */
    @Test
    fun deleteAssignedIngredientSuccessful() {
        viewModel.addIngredient("Delete".toEditable(), (1.0).toEditable(), IngredientUnit.NONE)
        viewModel.addIngredient("Keep".toEditable(), (1.0).toEditable(), IngredientUnit.NONE)

        val deleteIngredient = viewModel.ingredients.value!![0]
        val keepIngredient = viewModel.ingredients.value!![1]

        viewModel.addCookingStepWithIngredients(
            "Text".toEditable(),
            (0).toEditable(),
            TimeUnit.SECOND,
            mutableListOf(deleteIngredient, keepIngredient)
        )

        val ingredientsBefore = viewModel.cookingStepsWithIngredients.value?.get(0)!!.ingredients
        val beforeCountList = ArrayList<Ingredient>(ingredientsBefore)
        assertThat(beforeCountList.size).isEqualTo(2)

        viewModel.ingredients.value!!.remove(deleteIngredient)
        Thread.sleep(500)
        viewModel.prepareCookingStepUpdate(0)

        val ingredientsAfter = viewModel.cookingStepsWithIngredients.value?.get(0)!!.ingredients
        val afterCountList = ArrayList<Ingredient>(ingredientsAfter)
        assertThat(afterCountList.size).isEqualTo(1)
    }

    /**
     * Add CookingStepsWithIngredients with valid values, check return values and amount of cooking steps.
     */
    @Test
    fun addCookingStepSuccessful() {
        val text = "Valid Text".toEditable()
        val time1 = (1).toEditable()
        val time2 = "".toEditable()
        val unit = TimeUnit.SECOND
        val ingredients = mutableListOf(TestDataProvider.getRandomIngredient(0))

        val countBefore = viewModel.cookingStepsWithIngredients.value?.size!!

        assertThat(viewModel.addCookingStepWithIngredients(text, time1, unit, ingredients)).isTrue()
        assertThat(viewModel.addCookingStepWithIngredients(text, time2, unit, ingredients)).isTrue()

        val countAfter = viewModel.cookingStepsWithIngredients.value?.size!!

        assertThat(countAfter).isEqualTo(countBefore + 2)
    }

    /**
     * Add CookingStepWithIngredients with invalid text, check return value and amount of CookingSteps (shouldn't increase).
     */
    @Test
    fun addCookingStepUnsuccessful() {
        val textInvalid = "".toEditable()
        val timeValid = (3).toEditable()
        val unit = TimeUnit.MINUTE
        val ingredients = mutableListOf(TestDataProvider.getRandomIngredient(0))

        val countBefore = viewModel.cookingStepsWithIngredients.value?.size!!

        assertThat(
            viewModel.addCookingStepWithIngredients(
                textInvalid,
                timeValid,
                unit,
                ingredients
            )
        ).isFalse()

        val countAfter = viewModel.cookingStepsWithIngredients.value?.size!!
        assertThat(countAfter).isEqualTo(countBefore)
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
            text.toEditable(),
            time.toEditable(),
            unit,
            ingredients
        )
        assertThat(success).isTrue()

        val countAfter = viewModel.cookingStepsWithIngredients.value?.size!!

        assertThat(viewModel.cookingStepsWithIngredients.value!![position].cookingStep.text)
            .isEqualTo(text)
        assertThat(viewModel.cookingStepsWithIngredients.value!![position].cookingStep.time)
            .isEqualTo(time)
        assertThat(viewModel.cookingStepsWithIngredients.value!![position].cookingStep.timeUnit)
            .isEqualTo(unit)
        assertThat(viewModel.cookingStepsWithIngredients.value!![position].ingredients.size)
            .isEqualTo(1)

        assertThat(countAfter).isEqualTo(countBefore)
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
            text.toEditable(),
            time.toEditable(),
            unit,
            ingredients
        )
        assertThat(success).isFalse()

        val countAfter = viewModel.cookingStepsWithIngredients.getOrAwaitValue().size

        assertThat(countAfter).isEqualTo(countBefore)

        assertThat(viewModel.cookingStepsWithIngredients.value!![position].cookingStep.text)
            .isNotEqualTo(text)
        assertThat(viewModel.cookingStepsWithIngredients.value!![position].cookingStep.time)
            .isNotEqualTo(time)
        assertThat(viewModel.cookingStepsWithIngredients.value!![position].cookingStep.timeUnit)
            .isNotEqualTo(unit)
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
            "CookingStepName".toEditable(),
            (0).toEditable(),
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
            newName.toEditable(),
            newQuantity.toEditable(),
            newIngredientUnit
        )

        //Remove ingredient at index 2
        viewModel.ingredients.value!!.removeAt(2)

        // This happens when CookingStepDialog gets opened - Assigned ingredients will be refreshed
        viewModel.prepareCookingStepUpdate(cookingStepIndex)

        val cookingStepWithIngredients =
            viewModel.cookingStepsWithIngredients.value!![cookingStepIndex]

        assertThat(cookingStepWithIngredients.ingredients.size).isEqualTo(2)

        val updatedAssignedIngredient = cookingStepWithIngredients.ingredients[updateIndex]
        assertThat(updatedAssignedIngredient.name).isEqualTo(newName)
        assertThat(updatedAssignedIngredient.quantity).isEqualTo(newQuantity)
        assertThat(updatedAssignedIngredient.unit).isEqualTo(newIngredientUnit)

        val unchangedIngredient = cookingStepWithIngredients.ingredients[1]
        assertThat(unchangedIngredient == ingredient1).isTrue()
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

        assertThat(recipe).isNotNull()

        val id = viewModel.persistEntities().getOrAwaitValue()

        assertThat(id).isNotEqualTo(0L)
        assertThat(recipeRepository.getRecipeTotal().getOrAwaitValue()).isEqualTo(1)

        val recipeWithRelations = recipeRepository.getRecipeWithRelationsById(id).getOrAwaitValue()

        assertThat(recipeWithRelations.ingredients.size).isEqualTo(numberOfChildren)
        assertThat(recipeWithRelations.cookingStepsWithIngredients.size).isEqualTo(numberOfChildren)
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
        assertThat(recipeWithRelations2.ingredients.size).isEqualTo(numberOfChildren)
        assertThat(recipeWithRelations2.cookingStepsWithIngredients.size).isEqualTo(numberOfChildren)

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
            "New Ingredient".toEditable(),
            (1).toEditable(),
            IngredientUnit.NONE
        )
        // delete ingredient
        viewModel.ingredients.value!!.removeAt(1)
        // edit ingredient
        viewModel.prepareIngredientUpdate(0)
        viewModel.updateIngredient(
            "Updated Ingredient".toEditable(),
            (2).toEditable(),
            IngredientUnit.CAN
        )

        // add step
        viewModel.addCookingStepWithIngredients(
            "New Step".toEditable(),
            "".toEditable(),
            TimeUnit.SECOND,
            mutableListOf()
        )
        // edit step
        // remove step
        // change (add/remove) ingredient in step

        val newRecipeId = viewModel.persistEntities().getOrAwaitValue()
        assertThat(newRecipeId).isEqualTo(Recipe.DEFAULT_ID)

        val recipeWithRelations =
            recipeRepository.getRecipeWithRelationsById(newRecipeId).getOrAwaitValue(10)

        assertThat(recipeWithRelations.recipe.name).isEqualTo(name)
        assertThat(recipeWithRelations.recipe.servings).isEqualTo(servings.toInt())
        assertThat(recipeWithRelations.recipe.category).isEqualTo(category)

        assertThat(recipeWithRelations.ingredients.size).isEqualTo(2)
        assertThat(recipeWithRelations.ingredients[0])
            .isEqualTo(Ingredient("Updated Ingredient", 2.0, IngredientUnit.CAN))
        assertThat(recipeWithRelations.ingredients[1])
            .isEqualTo(Ingredient("New Ingredient", 1.0, IngredientUnit.NONE))

        assertThat(recipeWithRelations.cookingStepsWithIngredients.size).isEqualTo(3)
        assertThat(recipeWithRelations.cookingStepsWithIngredients[2].cookingStep)
            .isEqualTo(CookingStep("New Step", 0, TimeUnit.SECOND))
    }

    /**
     * Test validating name
     */
    @Test
    fun validateNameSuccessful() {
        assertThat(viewModel.validateName("n".toEditable())).isEqualTo(0)
    }

    /**
     * Test validating name
     */
    @Test
    fun validateNameUnsuccessful() {
        assertThat(viewModel.validateName("".toEditable())).isNotEqualTo(0)
    }

    /**
     * Test validating servings
     */
    @Test
    fun validateServingsSuccessful() {
        assertThat(viewModel.validateServings((1).toEditable())).isEqualTo(0)
    }

    /**
     * Test validating servings
     */
    @Test
    fun validateServingsUnsuccessful() {
        assertThat(viewModel.validateServings((0).toEditable())).isNotEqualTo(0)
        assertThat(viewModel.validateServings("".toEditable())).isNotEqualTo(0)
    }

    /**
     * Test if variables get reset on initRecipe(). This needs to happen to prevent
     * a former recipe to be shown again, because ViewModel has activity scoped lifecycle.
     */
    @Test
    fun clearValuesSuccessful() {
        insertTestData(1, 1)
        viewModel.setRecipeAttributes("Name", "1", RecipeCategory.BREAKFAST)
        viewModel.initRecipe(Recipe.DEFAULT_ID)

        assertThat(viewModel.ingredients.value).isNotNull()
        assertThat(viewModel.ingredients.value!!.size).isEqualTo(0)

        assertThat(viewModel.cookingStepsWithIngredients).isNotNull()
        assertThat(viewModel.cookingStepsWithIngredients.value!!.size).isEqualTo(0)

        assertThat(viewModel.recipe.value!!.name).isEqualTo("")
        // servings is private and can't be tested
        assertThat(viewModel.category.value).isEqualTo(RecipeCategory.MAIN_DISHES)

        // recipeToUpdate is private and can't be tested here
    }

    /**
     * Test if a recipe can be created successful after another recipe has been updated before
     */
//    @Test
    fun updateAndCreateRecipeSuccessful() {
//        insertTestData(1, 1)
        val id = viewModel.persistEntities().getOrAwaitValue()
        assertThat(recipeRepository.getRecipeTotal().getOrAwaitValue()).isEqualTo(1)
        // recipe saved

        GlobalScope.launch(Dispatchers.Main) {
            delay(1000)
            viewModel.initRecipe(id)
        }

        viewModel.recipe.getOrAwaitValue()
        viewModel.ingredients.getOrAwaitValue()
        viewModel.cookingStepsWithIngredients.getOrAwaitValue()

        viewModel.persistEntities().getOrAwaitValue(20)
        assertThat(recipeRepository.getRecipeTotal().getOrAwaitValue()).isEqualTo(1)
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
        assertThat(recipeRepository.getRecipeTotal().getOrAwaitValue()).isEqualTo(2)
        // second recipe saved
    }

    /////////////////////////////////////////////////////

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
                    "Inserted name $j".toEditable(),
                    "$j".toEditable(),
                    IngredientUnit.PINCH
                )
            }
            for (j in 1..cookingSteps) {
                viewModel.addCookingStepWithIngredients(
                    "Inserted text $j".toEditable(),
                    "$j".toEditable(),
                    TimeUnit.HOUR,
                    mutableListOf()
                )
            }
        }
    }

}
