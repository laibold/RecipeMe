package de.hs_rm.recipe_me.ui.recipe.add.cooking_step

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isPlatformPopup
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import de.hs_rm.recipe_me.R
import de.hs_rm.recipe_me.declaration.espresso.waitForView
import de.hs_rm.recipe_me.declaration.espresso.withListSize
import de.hs_rm.recipe_me.declaration.espresso.withSpinnerSize
import de.hs_rm.recipe_me.declaration.launchFragmentInHiltContainer
import de.hs_rm.recipe_me.declaration.toEditable
import de.hs_rm.recipe_me.di.Constants
import de.hs_rm.recipe_me.model.recipe.*
import de.hs_rm.recipe_me.model.relation.CookingStepIngredientCrossRef
import de.hs_rm.recipe_me.persistence.AppDatabase
import de.hs_rm.recipe_me.service.Formatter
import de.hs_rm.recipe_me.ui.recipe.add.AddRecipeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers.*
import org.junit.*
import org.mockito.Mockito.mock
import test_shared.TestDataProvider
import test_shared.declaration.getOrAwaitValue
import javax.inject.Inject
import javax.inject.Named

@HiltAndroidTest
class AddRecipeFragment3Test {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Inject
    @Named(Constants.TEST_NAME)
    lateinit var db: AppDatabase

    private lateinit var viewModel: AddRecipeViewModel
    private lateinit var context: Context
    private lateinit var navController: NavController

    private val ingredients = mutableListOf<Ingredient>()

    @Before
    fun beforeEach() {
        hiltRule.inject()
        assertThat(db.openHelper.databaseName).isEqualTo(AppDatabase.Environment.TEST.dbName)

        context = InstrumentationRegistry.getInstrumentation().targetContext
        db.clearAllTables()

        navController = mock(NavController::class.java)
        launchFragmentInHiltContainer<AddRecipeFragment3> {
            val viewModelTemp: AddRecipeViewModel by activityViewModels()
            viewModel = viewModelTemp
            viewModel.setCategory(RecipeCategory.MAIN_DISHES)
            viewModel.initRecipe(Recipe.DEFAULT_ID)
            Navigation.setViewNavController(requireView(), navController)
        }

    }

    @After
    fun cleanup() {
        ingredients.clear()
    }

    /**
     * Test that on empty list the empty state text is shown and that spinner contains all time units
     * Test also that all assigned ingredients are shown in the list of the dialog
     */
    @Test
    fun testElements() {
        val headlineText = context.getString(R.string.cooking_steps)
        onView(withId(R.id.headline)).check(matches(withText(headlineText)))
        onView(isRoot()).perform(waitForView(R.id.add_hint_text))
        val emptyStateText = context.getString(R.string.add_cooking_steps_text)
        onView(withId(R.id.add_hint_text)).check(matches(withText(emptyStateText)))

        onView(withId(R.id.cooking_step_list_view)).check(matches(withEffectiveVisibility(Visibility.GONE)))

        // check spinner
        val unitCount = TimeUnit.values().count()
        onView(withId(R.id.add_cooking_step_fab)).perform(click())
        onView(withId(R.id.cooking_step_time_spinner)).check(matches(withSpinnerSize(unitCount)))

        // check ingredients
        val placeholderText = context.getString(R.string.add_via_button)
        onView(withId(R.id.ingredients_text_view)).check(matches(withText(placeholderText)))
        // test inner "navigation"
        onView(withId(R.id.edit_ingredients_button)).perform(click())
        onView(withId(R.id.form_content))
            .check(matches(withEffectiveVisibility(Visibility.GONE)))
        onView(withId(R.id.ingredient_list_content))
            .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        onView(withId(R.id.ingredients_list_view)).check(matches(withListSize(ingredients.size)))

        // press cancel twice (first to "navigate" back, second to close dialog)
        onView(withId(R.id.cancel_button)).perform(click())
        onView(withId(R.id.cancel_button)).perform(click())
        onView(withId(R.id.dialog_layout)).check(doesNotExist())
    }

    /**
     * Test adding with empty and valid cooking step name
     */
    @Test
    fun testValidation() {
        // open dialog
        onView(withId(R.id.add_cooking_step_fab)).perform(click())

        // try adding cooking step with empty name and check quantity restrictions
        val errorText = context.getString(R.string.err_enter_description)
        onView(withId(R.id.cooking_step_field)).perform(replaceText(" "))
        onView(withId(R.id.cooking_step_time_field)).perform(typeText("a~40m;"))
        onView(withId(R.id.cooking_step_time_field)).check(matches(withText("40")))
        onView(withId(R.id.add_button)).perform(click())
        onView(withId(R.id.cooking_step_field)).check(matches(hasErrorText(errorText)))

        // add cooking step successful
        onView(withId(R.id.cooking_step_field)).perform(replaceText("Text"))
        onView(withId(R.id.add_button)).perform(click())
        onView(withId(R.id.dialog_layout)).check(doesNotExist())
        onView(withId(R.id.cooking_step_list_view)).check(matches(withListSize(1)))
    }

    /**
     *  Navigation on back button press
     */
    @Test
    fun testBackNavigation() {
        onView(withId(R.id.back_button)).perform(click())
        // can't verify navigation here because button calls onBackPressed()
    }

    /**
     *  Navigation on next button press
     */
    @Test
    fun testNextNavigation() {
        onView(withId(R.id.next_button)).perform(click())
        onView(withId(R.id.loading_spinner)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        // can't verify navigation here because we don't know the id provided to the direction
    }

    /**
     * Add cooking step and test its name in list
     */
    @Test
    fun testAddCookingStep() {
        addIngredientsToViewModel(3)

        val cookingStep = TestDataProvider.getRandomCookingStep()

        onView(withId(R.id.cooking_step_list_view)).check(matches(withListSize(0)))
        onView(withId(R.id.add_cooking_step_fab)).perform(click())
        onView(withId(R.id.dialog_layout)).check(matches(isDisplayed()))

        insertToCookingStepDialog(cookingStep, 2)
        onView(withId(R.id.add_button)).perform(click())

        onView(withId(R.id.cooking_step_list_view)).check(matches(withListSize(1)))
        onData(anything()).inAdapterView(withId(R.id.cooking_step_list_view)).atPosition(0)
            .onChildView(withId(R.id.cooking_step_text)).check(matches(withText(cookingStep.text)))
    }

    /**
     * Test that on edit button click the selected cooking step in loaded to dialog and that
     * editing updates the cooking step in the list
     */
    @Test
    fun testEditCookingStep() {
        addIngredientsToViewModel(4)

        val cookingStep = TestDataProvider.getRandomCookingStep()

        // add test cooking step
        onView(withId(R.id.cooking_step_list_view)).check(matches(withListSize(0)))
        onView(withId(R.id.add_cooking_step_fab)).perform(click())
        insertToCookingStepDialog(cookingStep, 1)
        onView(withId(R.id.add_button)).perform(click())
        onView(withId(R.id.cooking_step_list_view)).check(matches(withListSize(1)))

        // click edit and check form
        onData(anything()).inAdapterView(withId(R.id.cooking_step_list_view)).atPosition(0)
            .onChildView(withId(R.id.edit_button)).perform(click())
        val ingredientText = Formatter.formatIngredientList(context, ingredients.slice(0..0))
        checkCookingStepInDialog(cookingStep, ingredientText)

        // set new values – we call addCookingStep with 2 ingredients, which will deselect index 0 and select index 1
        val updatedCookingStep = TestDataProvider.getRandomCookingStep()
        insertToCookingStepDialog(updatedCookingStep, 2)
        onView(withId(R.id.add_button)).perform(click())
        val updatedIngredientText = Formatter.formatIngredientList(context, ingredients.slice(1..1))

        // check updated cooking step in list
        onView(withId(R.id.cooking_step_list_view)).check(matches(withListSize(1)))
        onData(anything()).inAdapterView(withId(R.id.cooking_step_list_view)).atPosition(0)
            .onChildView(withId(R.id.cooking_step_text))
            .check(matches(withText(updatedCookingStep.text)))

        // check updated cooking step in dialog
        onData(anything()).inAdapterView(withId(R.id.cooking_step_list_view)).atPosition(0)
            .onChildView(withId(R.id.edit_button)).perform(click())
        checkCookingStepInDialog(updatedCookingStep, updatedIngredientText)
    }

    /**
     * Test that deleting an cooking step succeeds and empty state text is shown again afterwards
     */
    @Test
    fun testDeleteCookingStep() {
        val cookingStep = TestDataProvider.getRandomCookingStep()

        // add test cooking step
        onView(withId(R.id.add_cooking_step_fab)).perform(click())
        insertToCookingStepDialog(cookingStep, 0)
        onView(withId(R.id.add_button)).perform(click())
        onView(withId(R.id.cooking_step_list_view)).check(matches(withListSize(1)))

        // remove and check
        onData(anything()).inAdapterView(withId(R.id.cooking_step_list_view)).atPosition(0)
            .onChildView(withId(R.id.remove_button)).perform(click())
        onView(withId(R.id.cooking_step_list_view)).check(matches(withListSize(0)))

        //empty state text should be shown again
        val emptyStateText = context.getString(R.string.add_cooking_steps_text)
        onView(withId(R.id.add_hint_text)).check(matches(withText(emptyStateText)))
    }

    @Test
    @Ignore
    fun testLoadPersistedCookingStep() {
        var rId: Long
        val cookingStep = TestDataProvider.getRandomCookingStep().apply { text = "step text" }
        val ingredient1 = Ingredient("name", 1.0, IngredientUnit.CUP)
        val ingredient2 = Ingredient("name", 1.0, IngredientUnit.CUP)
        val ingredient3 = Ingredient("ingredient", 2.0, IngredientUnit.CAN)

        runBlocking {
            // Insert Recipe with 3 Ingredients where first and third are assigned to single CookingStep
            rId = db.recipeDao().insert(TestDataProvider.getRandomRecipe())

            val ingredientId1 = db.recipeDao().insert(ingredient1.apply { recipeId = rId })
            db.recipeDao().insert(ingredient2.apply { recipeId = rId })
            val ingredientId3 = db.recipeDao().insert(ingredient3.apply { recipeId = rId })

            val stepId = db.recipeDao().insert(cookingStep.apply { recipeId = rId })

            db.recipeDao().insert(CookingStepIngredientCrossRef(stepId, ingredientId1))
            db.recipeDao().insert(CookingStepIngredientCrossRef(stepId, ingredientId3))
        }

        GlobalScope.launch(Dispatchers.Main) { viewModel.initRecipe(rId) }

        onView(withId(R.id.cooking_step_list_view)).check(matches(withListSize(1)))

        onData(anything()).inAdapterView(withId(R.id.cooking_step_list_view)).atPosition(0)
            .onChildView(withId(R.id.cooking_step_text)).check(matches(withText(cookingStep.text)))

        // click edit and check form
        onData(anything()).inAdapterView(withId(R.id.cooking_step_list_view)).atPosition(0)
            .onChildView(withId(R.id.edit_button)).perform(click())
        val ingredientText =
            Formatter.formatIngredientList(context, listOf(ingredient1, ingredient3))
        checkCookingStepInDialog(cookingStep, ingredientText)

        onView(withId(R.id.edit_ingredients_button)).perform(click())
        onData(anything()).inAdapterView(withId(R.id.ingredients_list_view)).atPosition(0)
            .onChildView(withId(R.id.item_checkbox)).check(matches(isChecked()))
        onData(anything()).inAdapterView(withId(R.id.ingredients_list_view)).atPosition(1)
            .onChildView(withId(R.id.item_checkbox)).check(matches(isNotChecked()))
        onData(anything()).inAdapterView(withId(R.id.ingredients_list_view)).atPosition(2)
            .onChildView(withId(R.id.item_checkbox)).check(matches(isChecked()))
    }

    /**
     * Test that checking ingredients in dialog list works as expected even if the ingredients have the same values
     */
    @Test
    @Ignore
    fun testAssignIngredients() {
        viewModel.addIngredient("name".toEditable(), (2.0).toEditable(), IngredientUnit.CAN)
        viewModel.addIngredient("name".toEditable(), (2.0).toEditable(), IngredientUnit.CAN)
        viewModel.addIngredient("ingredient".toEditable(), (0.0).toEditable(), IngredientUnit.CUP)

        val cookingStep = TestDataProvider.getRandomCookingStep()

        onView(withId(R.id.cooking_step_list_view)).check(matches(withListSize(0)))
        onView(withId(R.id.add_cooking_step_fab)).perform(click())
        onView(withId(R.id.dialog_layout)).check(matches(isDisplayed()))

        insertToCookingStepDialog(cookingStep, 0)

        onView(withId(R.id.edit_ingredients_button)).perform(click())

        // check first ingredient, only this one should be checked
        onData(anything()).inAdapterView(withId(R.id.ingredients_list_view)).atPosition(0)
            .onChildView(withId(R.id.item_checkbox)).perform(click())

        onData(anything()).inAdapterView(withId(R.id.ingredients_list_view)).atPosition(0)
            .onChildView(withId(R.id.item_checkbox)).check(matches(isChecked()))
        onData(anything()).inAdapterView(withId(R.id.ingredients_list_view)).atPosition(1)
            .onChildView(withId(R.id.item_checkbox)).check(matches(isNotChecked()))
        onData(anything()).inAdapterView(withId(R.id.ingredients_list_view)).atPosition(2)
            .onChildView(withId(R.id.item_checkbox)).check(matches(isNotChecked()))

        // check second ingredient, first and second should be checked
        onData(anything()).inAdapterView(withId(R.id.ingredients_list_view)).atPosition(1)
            .onChildView(withId(R.id.item_checkbox)).perform(click())

        onData(anything()).inAdapterView(withId(R.id.ingredients_list_view)).atPosition(0)
            .onChildView(withId(R.id.item_checkbox)).check(matches(isChecked()))
        onData(anything()).inAdapterView(withId(R.id.ingredients_list_view)).atPosition(1)
            .onChildView(withId(R.id.item_checkbox)).check(matches(isChecked()))
        onData(anything()).inAdapterView(withId(R.id.ingredients_list_view)).atPosition(2)
            .onChildView(withId(R.id.item_checkbox)).check(matches(isNotChecked()))

        // uncheck second ingredient again, only first one should be checked
        onData(anything()).inAdapterView(withId(R.id.ingredients_list_view)).atPosition(0)
            .onChildView(withId(R.id.item_checkbox)).check(matches(isChecked()))
        onData(anything()).inAdapterView(withId(R.id.ingredients_list_view)).atPosition(1)
            .onChildView(withId(R.id.item_checkbox)).check(matches(isNotChecked()))
        onData(anything()).inAdapterView(withId(R.id.ingredients_list_view)).atPosition(2)
            .onChildView(withId(R.id.item_checkbox)).check(matches(isNotChecked()))

        onView(withId(R.id.add_button)).perform(click())
        onView(withId(R.id.add_button)).perform(click())
    }

    /**
     * Assign 3 ingredients to cooking step and delete 1 afterwards.
     * The list in the dialog should be refreshed and the other ingredients should still be assigned
     */
    @Test
    fun testDeletedIngredient() {
        addIngredientsToViewModel(3)

        val cookingStep = TestDataProvider.getRandomCookingStep()
        onView(withId(R.id.add_cooking_step_fab)).perform(click())
        insertToCookingStepDialog(cookingStep, 3)
        onView(withId(R.id.add_button)).perform(click())

        viewModel.ingredients.getOrAwaitValue().removeAt(0)

        // check that cooking step no only has 2 assigned ingredients left
        val updatedIngredientText = Formatter.formatIngredientList(context, ingredients.slice(1..2))
        onData(anything()).inAdapterView(withId(R.id.cooking_step_list_view)).atPosition(0)
            .onChildView(withId(R.id.edit_button)).perform(click())
        checkCookingStepInDialog(cookingStep, updatedIngredientText)

        // check ingredient list (should contain 2 left ingredients that are both checked)
        onView(withId(R.id.edit_ingredients_button)).perform(click())
        onView(withId(R.id.ingredients_list_view)).check(matches(withListSize(2)))
        onData(anything()).inAdapterView(withId(R.id.ingredients_list_view)).atPosition(0)
            .onChildView(withId(R.id.item_checkbox)).check(matches(isChecked()))
        onData(anything()).inAdapterView(withId(R.id.ingredients_list_view)).atPosition(1)
            .onChildView(withId(R.id.item_checkbox)).check(matches(isChecked()))
    }

    /**
     * Update assigned ingredient and check if it's still assigned and no changes happen.
     */
    @Test
    fun testUpdatedIngredient() {
        addIngredientsToViewModel(3)

        val cookingStep = TestDataProvider.getRandomCookingStep()
        val updatedIngredient = TestDataProvider.getRandomIngredient()
        onView(withId(R.id.add_cooking_step_fab)).perform(click())
        insertToCookingStepDialog(cookingStep, 2)
        onView(withId(R.id.add_button)).perform(click())

        viewModel.prepareIngredientUpdate(0)
        viewModel.updateIngredient(
            updatedIngredient.name.toEditable(),
            updatedIngredient.quantity.toEditable(),
            updatedIngredient.unit
        )

        val expectedList = listOf(updatedIngredient, ingredients[1])
        val expectedIngredientText = Formatter.formatIngredientList(context, expectedList)

        onData(anything()).inAdapterView(withId(R.id.cooking_step_list_view)).atPosition(0)
            .onChildView(withId(R.id.edit_button)).perform(click())
        checkCookingStepInDialog(cookingStep, expectedIngredientText)

        // check ingredient list (first 2 ingredients should still be checked)
        onView(withId(R.id.edit_ingredients_button)).perform(click())
        onView(withId(R.id.ingredients_list_view)).check(matches(withListSize(3)))
        onData(anything()).inAdapterView(withId(R.id.ingredients_list_view)).atPosition(0)
            .onChildView(withId(R.id.item_checkbox)).check(matches(isChecked()))
        onData(anything()).inAdapterView(withId(R.id.ingredients_list_view)).atPosition(1)
            .onChildView(withId(R.id.item_checkbox)).check(matches(isChecked()))
    }

    ///

    private fun addIngredientsToViewModel(quantity: Int) {
        for (i in 1..quantity) {
            val ingredient = TestDataProvider.getRandomIngredient()
            ingredients.add(ingredient)
            viewModel.addIngredient(
                ingredient.name.toEditable(),
                ingredient.quantity.toEditable(),
                ingredient.unit
            )
        }
    }

    /**
     * Fill out cooking step dialog and assign number of ingredients (be aware of the maximum size)
     */
    private fun insertToCookingStepDialog(
        cookingStep: CookingStep,
        numberOfAssignedIngredients: Int
    ) {
        val unitName = cookingStep.timeUnit.getNumberString(context.resources, cookingStep.time)

        onView(withId(R.id.cooking_step_field)).perform(replaceText(cookingStep.text))
        onView(withId(R.id.cooking_step_time_field)).perform(replaceText(cookingStep.time.toString()))
        onView(withId(R.id.cooking_step_time_spinner)).perform(click())
        onData(allOf(`is`(instanceOf(String::class.java)), `is`(unitName)))
            .inRoot(isPlatformPopup()).perform(click())

        if (numberOfAssignedIngredients > 0) {
            // add ingredients
            onView(withId(R.id.edit_ingredients_button)).perform(click())
            for (i in 0 until numberOfAssignedIngredients) {
                onData(anything()).inAdapterView(withId(R.id.ingredients_list_view)).atPosition(i)
                    .onChildView(withId(R.id.item_checkbox)).perform(click())
            }
            onView(withId(R.id.add_button)).perform(click())
        }
    }

    /**
     * Check cooking step's text, time, time unit and text of ingredients
     * Press edit to to open dialog first
     */
    private fun checkCookingStepInDialog(cookingStep: CookingStep, ingredientText: String) {
        val timeUnitName = cookingStep.timeUnit.getNumberString(context.resources, cookingStep.time)

        onView(withId(R.id.cooking_step_field)).check(matches(withText(cookingStep.text)))
        onView(withId(R.id.cooking_step_time_field)).check(matches(withText(cookingStep.time.toString())))
        onView(withId(R.id.cooking_step_time_spinner))
            .check(matches(withSpinnerText(containsString(timeUnitName))))
        onView(withId(R.id.ingredients_text_view)).check(matches(withText(ingredientText)))
    }

}
