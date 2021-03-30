package de.hs_rm.recipe_me.ui.recipe.home

import android.content.Context
import android.widget.Button
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import de.hs_rm.recipe_me.Constants
import de.hs_rm.recipe_me.R
import de.hs_rm.recipe_me.declaration.espresso.touch
import de.hs_rm.recipe_me.declaration.espresso.waitForView
import de.hs_rm.recipe_me.declaration.espresso.withListSize
import de.hs_rm.recipe_me.declaration.launchFragmentInHiltContainer
import de.hs_rm.recipe_me.model.RecipeOfTheDay
import de.hs_rm.recipe_me.model.recipe.Recipe
import de.hs_rm.recipe_me.model.recipe.RecipeCategory
import de.hs_rm.recipe_me.persistence.AppDatabase
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers.hasToString
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Named

@HiltAndroidTest
class RecipeHomeFragmentTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    @Named(Constants.TEST_NAME)
    lateinit var db: AppDatabase

    lateinit var context: Context

    @Before
    fun beforeEach() {
        hiltRule.inject()
        assertThat(db.openHelper.databaseName).isEqualTo(AppDatabase.Environment.TEST.dbName)

        context = getInstrumentation().targetContext
        db.clearAllTables()
    }

    /**
     * Test navigation to AddRecipeFragment
     */
    @Test
    fun testAddRecipeNavigation() {
        val navController = mock(NavController::class.java)
        launchFragmentInHiltContainer<RecipeHomeFragment> {
            Navigation.setViewNavController(requireView(), navController)
        }

        onView(withId(R.id.add_button)).perform(click())

        verify(navController).navigate(
            RecipeHomeFragmentDirections.toAddRecipeNavGraph(
                RecipeCategory.MAIN_DISHES,
                0,
                true
            )
        )
    }

    /**
     * Test navigation to category
     */
    @Test
    fun testCategoryNavigation() {
        val category = RecipeCategory.SALADS

        val navController = mock(NavController::class.java)
        launchFragmentInHiltContainer<RecipeHomeFragment> {
            Navigation.setViewNavController(requireView(), navController)
        }

        onData(hasToString(category.toString())).perform(click())

        verify(navController).navigate(
            RecipeHomeFragmentDirections.toRecipeCategoryFragment(category)
        )
    }

    /**
     * Test if all categories are displayed
     */
    @Test
    fun testCountCategories() {
        launchFragmentInHiltContainer<RecipeHomeFragment>()

        val categoriesCount = RecipeCategory.values().size
        onView(withId(R.id.category_list)).check(matches(withListSize(categoriesCount)))
    }

    /**
     * Test if recipe of the day's empty state is shown and button is hidden if no recipe exists
     */
    @Test
    fun testRecipeOfTheDayEmptyState() {
        launchFragmentInHiltContainer<RecipeHomeFragment>()

        Thread.sleep(500)

        val emptyStateText = context.getString(R.string.no_recipe_otd)
        onView(isRoot()).perform(waitForView(R.id.recipe_of_the_day_name))
        onView(withId(R.id.recipe_of_the_day_name)).check(matches(withText(emptyStateText)))
        onView(withId(R.id.recipe_of_the_day_button)).check(
            matches(withEffectiveVisibility(Visibility.GONE))
        )
    }

    /**
     * Test navigation to recipe of the day
     */
    @Test
    fun testRecipeOfTheDayNavigation() {
        val id: Long
        runBlocking {
            id = db.recipeDao().insert(Recipe("Test Recipe", 1, RecipeCategory.SALADS))
            db.recipeOfTheDayDao().insert(RecipeOfTheDay(LocalDate.now(), id))
        }

        var button: Button? = null

        val navController = mock(NavController::class.java)
        launchFragmentInHiltContainer<RecipeHomeFragment> {
            Navigation.setViewNavController(requireView(), navController)
            button = view?.findViewById(R.id.recipe_of_the_day_button)
        }

        Thread.sleep(500)

        val location = IntArray(2)
        button?.getLocationOnScreen(location)

        // Perform touch on button coordinates
        onView(isRoot()).perform(touch(location[0], location[1]))

        verify(navController).navigate(
            RecipeHomeFragmentDirections.toRecipeDetailFragment(id, true)
        )
    }

}
