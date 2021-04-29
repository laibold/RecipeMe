package de.hs_rm.recipe_me.ui.recipe.add.recipe_information

import android.content.Context
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import de.hs_rm.recipe_me.R
import de.hs_rm.recipe_me.declaration.espresso.withSpinnerSize
import de.hs_rm.recipe_me.declaration.launchFragmentInHiltContainer
import de.hs_rm.recipe_me.di.Constants
import de.hs_rm.recipe_me.model.recipe.RecipeCategory
import de.hs_rm.recipe_me.persistence.AppDatabase
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.verify
import javax.inject.Inject
import javax.inject.Named

@HiltAndroidTest
class AddRecipeFragment1Test {

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
        launchFragmentInHiltContainer<AddRecipeFragment1>(bundleOf()) {
            Navigation.setViewNavController(requireView(), navController)
        }
    }

    /**
     * Test content of headline and category spinner
     */
    @Test
    fun testElements() {
        val headlineText = context.getString(R.string.new_recipe)
        onView(withId(R.id.headline)).check(matches(withText(headlineText)))

        val categoryCount = RecipeCategory.values().count()
        onView(withId(R.id.recipe_category_spinner)).check(matches(withSpinnerSize(categoryCount)))
    }

    /**
     * Test field validation and navigation on success
     */
    @Test
    fun testValidation() {
        val nameError = context.getString(R.string.err_enter_name)
        val servingsError = context.getString(R.string.err_enter_servings)
        val servingsSizeError = context.getString(R.string.err_servings_greater_than_zero)

        // name = " ", servings = ""
        onView(withId(R.id.recipe_name_field))
            .perform(replaceText(" ")).also { closeSoftKeyboard() }
        onView(withId(R.id.next_button)).perform(click())
        onView(withId(R.id.recipe_name_field)).check(matches(hasErrorText(nameError)))
        onView(withId(R.id.recipe_servings_field)).check(matches(hasErrorText(servingsError)))

        // name = "Name", servings = "0"
        onView(withId(R.id.recipe_name_field)).perform(replaceText("Name"))
        onView(withId(R.id.recipe_servings_field))
            .perform(replaceText("0")).also { closeSoftKeyboard() }
        onView(withId(R.id.next_button)).perform(click())
        onView(withId(R.id.recipe_servings_field)).check(matches(hasErrorText(servingsSizeError)))

        // name = "Name", servings = "1"
        onView(withId(R.id.recipe_servings_field))
            .perform(replaceText("1")).also { closeSoftKeyboard() }
        onView(withId(R.id.next_button)).perform(click())

        verify(navController).navigate(AddRecipeFragment1Directions.toAddRecipeFragment2())
    }

    // TODO test image
}
