package de.hs_rm.recipe_me.ui.recipe.home

import android.content.Context
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import de.hs_rm.recipe_me.R
import de.hs_rm.recipe_me.model.recipe.Recipe
import de.hs_rm.recipe_me.model.recipe.RecipeCategory
import de.hs_rm.recipe_me.persistence.AppDatabase
import de.hs_rm.recipe_me.ui.launchFragmentInHiltContainer
import de.hs_rm.recipe_me.waitForView
import de.hs_rm.recipe_me.withListSize
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers.hasToString
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import javax.inject.Inject
import javax.inject.Named

@HiltAndroidTest
class RecipeHomeFragmentTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    @Named("android_test_db")
    lateinit var db: AppDatabase

    lateinit var context: Context

    @Before
    fun init() {
        hiltRule.inject()
        db.clearAllTables()
        context = InstrumentationRegistry.getInstrumentation().targetContext
    }

    @After
    fun tearDown() {
        db.close()
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
        val navController = mock(NavController::class.java)
        launchFragmentInHiltContainer<RecipeHomeFragment> {
            Navigation.setViewNavController(requireView(), navController)
        }

        val categoriesCount = RecipeCategory.values().size

        onView(withId(R.id.category_list)).check(matches(withListSize(categoriesCount)))
    }

    /**
     * Test if recipe of the day's empty state is shown and button is hidden if no recipe exists
     */
    @Test
    fun testRecipeOfTheDayEmptyState() {
        db.clearAllTables()

        val navController = mock(NavController::class.java)
        launchFragmentInHiltContainer<RecipeHomeFragment> {
            Navigation.setViewNavController(requireView(), navController)
        }

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
        runBlocking {
            db.recipeDao().insert(Recipe("Test Recipe", 1, RecipeCategory.SALADS))
        }

        val navController = mock(NavController::class.java)
        launchFragmentInHiltContainer<RecipeHomeFragment> {
            Navigation.setViewNavController(requireView(), navController)
        }
    }

}
