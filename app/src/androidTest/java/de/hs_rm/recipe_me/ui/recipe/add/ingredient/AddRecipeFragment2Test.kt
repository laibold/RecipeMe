package de.hs_rm.recipe_me.ui.recipe.add.ingredient

import android.content.Context
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
import de.hs_rm.recipe_me.di.Constants
import de.hs_rm.recipe_me.model.recipe.Ingredient
import de.hs_rm.recipe_me.model.recipe.IngredientUnit
import de.hs_rm.recipe_me.model.recipe.Recipe
import de.hs_rm.recipe_me.model.recipe.RecipeCategory
import de.hs_rm.recipe_me.persistence.AppDatabase
import de.hs_rm.recipe_me.service.Formatter
import de.hs_rm.recipe_me.ui.recipe.add.AddRecipeViewModel
import org.hamcrest.Matchers.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.verify
import test_shared.TestDataProvider
import javax.inject.Inject
import javax.inject.Named

@HiltAndroidTest
class AddRecipeFragment2Test {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    @Named(Constants.TEST_NAME)
    lateinit var db: AppDatabase

    private lateinit var context: Context
    private lateinit var navController: NavController

    @Before
    fun beforeEach() {
        hiltRule.inject()
        assertThat(db.openHelper.databaseName).isEqualTo(AppDatabase.Environment.TEST.dbName)

        context = InstrumentationRegistry.getInstrumentation().targetContext
        db.clearAllTables()

        navController = Mockito.mock(NavController::class.java)
        launchFragmentInHiltContainer<AddRecipeFragment2> {
            val viewModel: AddRecipeViewModel by activityViewModels()
            viewModel.setCategory(RecipeCategory.MAIN_DISHES)
            viewModel.initRecipe(Recipe.DEFAULT_ID)
            Navigation.setViewNavController(requireView(), navController)
        }
    }

    /**
     * Test that on empty list the empty state text is shown and that spinner contains all units
     */
    @Test
    fun testElements() {
        val headlineText = context.getString(R.string.ingredients)
        onView(withId(R.id.headline)).check(matches(withText(headlineText)))
        onView(isRoot()).perform(waitForView(R.id.add_hint_text))
        val emptyStateText = context.getString(R.string.add_ingredients_text)
        onView(withId(R.id.add_hint_text)).check(matches(withText(emptyStateText)))

        onView(withId(R.id.ingredients_list_view)).check(matches(withEffectiveVisibility(Visibility.GONE)))

        // open dialog and check spinner
        val unitCount = IngredientUnit.values().count()
        onView(withId(R.id.add_ingredient_fab)).perform(click())
        onView(withId(R.id.ingredient_unit_spinner)).check(matches(withSpinnerSize(unitCount)))

        // Press cancel
        onView(withId(R.id.cancel_button)).perform(click())
        onView(withId(R.id.ingredient_dialog_layout)).check(doesNotExist())
    }

    /**
     * Test navigation on empty ingredient name and that navigation only succeeds
     * if at least one ingredient is added
     */
    @Test
    fun testValidation() {
        onView(withId(R.id.next_button)).perform(click())
        // no navigation without ingredients
        val headlineText = context.getString(R.string.ingredients)
        onView(withId(R.id.headline)).check(matches(withText(headlineText)))

        // open dialog
        onView(withId(R.id.add_ingredient_fab)).perform(click())

        // try adding ingredient with empty name and check quantity restrictions
        val errorText = context.getString(R.string.err_enter_name)
        onView(withId(R.id.ingredient_name_field)).perform(replaceText(" "))
        onView(withId(R.id.ingredient_quantity_field)).perform(typeText("a~4m;"))
        onView(withId(R.id.ingredient_quantity_field)).check(matches(withText("4")))
        onView(withId(R.id.add_button)).perform(click())
        onView(withId(R.id.ingredient_name_field)).check(matches(hasErrorText(errorText)))

        // add ingredient successful
        onView(withId(R.id.ingredient_name_field)).perform(replaceText("Name"))
        onView(withId(R.id.add_button)).perform(click())
        onView(withId(R.id.ingredient_dialog_layout)).check(doesNotExist())
        onView(withId(R.id.ingredients_list_view)).check(matches(withListSize(1)))

        // navigation should now be possible
        onView(withId(R.id.next_button)).perform(click())
        verify(navController).navigate(AddRecipeFragment2Directions.toAddRecipeFragment3())
    }

    /**
     * Add ingredient and test its formatted name and the list size
     */
    @Test
    fun testAddIngredient() {
        val ingredient = TestDataProvider.getRandomIngredient(minQuantity = 2.0)
        val formattedText = Formatter.formatIngredient(context, ingredient)

        onView(withId(R.id.ingredients_list_view)).check(matches(withListSize(0)))
        onView(withId(R.id.add_ingredient_fab)).perform(click())
        onView(withId(R.id.ingredient_dialog_layout)).check(matches(isDisplayed()))

        // fill out form
        addIngredient(ingredient)

        // test list size and formatting of ingredient
        onView(withId(R.id.ingredients_list_view)).check(matches(withListSize(1)))
        onData(anything()).inAdapterView(withId(R.id.ingredients_list_view)).atPosition(0)
            .onChildView(withId(R.id.ingredient_text_view)).check(matches(withText(formattedText)))
    }

    /**
     * Test that on edit button click the selected ingredient in loaded to dialog and that
     * editing updates the ingredient in the list
     */
    @Test
    fun testEditIngredient() {
        val ingredient = TestDataProvider.getRandomIngredient(minQuantity = 2.0)

        // add test ingredient
        onView(withId(R.id.ingredients_list_view)).check(matches(withListSize(0)))
        onView(withId(R.id.add_ingredient_fab)).perform(click())
        addIngredient(ingredient)

        onView(withId(R.id.ingredients_list_view)).check(matches(withListSize(1)))

        // click edit and check form
        onData(anything()).inAdapterView(withId(R.id.ingredients_list_view)).atPosition(0)
            .onChildView(withId(R.id.edit_button)).perform(click())
        val formattedQuantity = Formatter.formatIngredientQuantity(ingredient.quantity)
        val unitName = ingredient.unit.getNumberString(context.resources, ingredient.quantity)
        onView(withId(R.id.ingredient_quantity_field)).check(matches(withText(formattedQuantity)))
        onView(withId(R.id.ingredient_unit_spinner))
            .check(matches(withSpinnerText(containsString(unitName))))
        onView(withId(R.id.ingredient_name_field)).check(matches(withText(ingredient.name)))

        // set new values
        val updatedIngredient = TestDataProvider.getRandomIngredient(minQuantity = 2.0)
        val formattedText = Formatter.formatIngredient(context, updatedIngredient)
        addIngredient(updatedIngredient)

        // check updated ingredient in list
        onView(withId(R.id.ingredients_list_view)).check(matches(withListSize(1)))
        onData(anything()).inAdapterView(withId(R.id.ingredients_list_view)).atPosition(0)
            .onChildView(withId(R.id.ingredient_text_view)).check(matches(withText(formattedText)))
    }

    /**
     * Test that deleting an ingredient succeeds and empty state text is shown again afterwards
     */
    @Test
    fun testDeleteIngredient() {
        // add test ingredient
        val ingredient = Ingredient("Name", 1.0, IngredientUnit.GRAM)
        onView(withId(R.id.add_ingredient_fab)).perform(click())
        addIngredient(ingredient)
        onView(withId(R.id.ingredients_list_view)).check(matches(withListSize(1)))

        // remove and check
        onData(anything()).inAdapterView(withId(R.id.ingredients_list_view)).atPosition(0)
            .onChildView(withId(R.id.remove_button)).perform(click())
        onView(withId(R.id.ingredients_list_view)).check(matches(withListSize(0)))

        //empty state text should be shown again
        val emptyStateText = context.getString(R.string.add_ingredients_text)
        onView(withId(R.id.add_hint_text)).check(matches(withText(emptyStateText)))
    }

    /**
     * Test navigation on app's back button
     */
    @Test
    fun testBackButtonNavigation() {
        onView(withId(R.id.back_button)).perform(click())
        verify(navController).navigate(
            AddRecipeFragment2Directions.toAddRecipeNavGraph(clearValues = false)
        )
    }

    /**
     * Fill out dialog form (quantity, unit and name) and press add button
     */
    private fun addIngredient(ingredient: Ingredient) {
        val unitName = ingredient.unit.getNumberString(context.resources, ingredient.quantity)

        onView(withId(R.id.ingredient_quantity_field)).perform(replaceText(ingredient.quantity.toString()))
        onView(withId(R.id.ingredient_name_field)).perform(replaceText(ingredient.name))
        onView(withId(R.id.ingredient_unit_spinner)).perform(click())
        onData(allOf(`is`(instanceOf(String::class.java)), `is`(unitName)))
            .inRoot(isPlatformPopup()).perform(click())
        onView(withId(R.id.add_button)).perform(click())
    }

}
