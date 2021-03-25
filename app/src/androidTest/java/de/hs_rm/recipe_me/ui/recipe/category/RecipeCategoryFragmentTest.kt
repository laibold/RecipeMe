package de.hs_rm.recipe_me.ui.recipe.category

import android.content.Context
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.longClick
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import de.hs_rm.recipe_me.Constants
import de.hs_rm.recipe_me.R
import de.hs_rm.recipe_me.TestDataProvider
import de.hs_rm.recipe_me.declaration.launchFragmentInHiltContainer
import de.hs_rm.recipe_me.declaration.waitForView
import de.hs_rm.recipe_me.declaration.withListSize
import de.hs_rm.recipe_me.model.recipe.RecipeCategory
import de.hs_rm.recipe_me.persistence.AppDatabase
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers.allOf
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.verify
import javax.inject.Inject
import javax.inject.Named

@HiltAndroidTest
class RecipeCategoryFragmentTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    @Named(Constants.TEST_NAME)
    lateinit var db: AppDatabase

    private lateinit var context: Context
    private lateinit var navController: NavController

    companion object {
        val TEST_CATEGORY = RecipeCategory.SALADS
    }

    @Before
    fun beforeEach() {
        hiltRule.inject()
        Assert.assertEquals(AppDatabase.Environment.TEST.dbName, db.openHelper.databaseName)

        context = InstrumentationRegistry.getInstrumentation().targetContext
        db.clearAllTables()

        navController = Mockito.mock(NavController::class.java)
        val args = bundleOf("recipeCategory" to TEST_CATEGORY)
        launchFragmentInHiltContainer<RecipeCategoryFragment>(args) {
            Navigation.setViewNavController(requireView(), navController)
        }
    }

    /**
     * Test that empty state text is shown if RecyclerView is empty
     */
    @Test
    fun testEmptyState() {
        val emptyStateText = context.getString(R.string.add_recipe_text)
        onView(withId(R.id.add_hint_text)).check(matches(withText(emptyStateText)))
        onView(withId(R.id.recipe_list)).check(matches(withListSize(0)))
    }

    /**
     * Test navigation to recipe
     */
    @Test
    fun testRecipeDetailNavigation() {
        val id = insertTestRecipes(1)[0]
        onView(isRoot()).perform(waitForView(R.id.recipe_list))

        onView(withId(R.id.recipe_list)).perform(
            actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click())
        )

        verify(navController).navigate(
            RecipeCategoryFragmentDirections.toRecipeDetailFragment(id, false)
        )
    }

    /**
     * Test navigation to AddRecipeFragment with category as argument
     */
    @Test
    fun testAddRecipeNavigation() {
        onView(withId(R.id.add_button)).perform(click())

        verify(navController).navigate(
            RecipeCategoryFragmentDirections.toAddRecipeNavGraph(
                TEST_CATEGORY,
                0,
                true
            )
        )
    }

    /**
     * Test that all inserted recipes are displayed
     */
    @Test
    fun countRecipes() {
        val count = 4
        insertTestRecipes(count)
        onView(isRoot()).perform(waitForView(R.id.recipe_list))

        onView(isRoot()).perform(waitForView(R.id.recipe_list))
        onView(withId(R.id.recipe_list)).check(matches(withListSize(count)))
    }

    /**
     * Test that editing overlay is displayed after long click
     * Also test that it switches after long click on other recipe and hides after back navigation
     */
    @Test
    fun testRecipeLongPress() {
        val count = 3
        insertTestRecipes(count)
        onView(isRoot()).perform(waitForView(R.id.recipe_list))

        val firstItem = allOf(withParent(withId(R.id.recipe_list)), withParentIndex(0))
        val secondItem = allOf(withParent(withId(R.id.recipe_list)), withParentIndex(1))

        onView(allOf(withId(R.id.edit_overlay), withParent(firstItem)))
            .check(matches(withEffectiveVisibility(Visibility.GONE)))

        // long click on first item, overlay should be visible
        onView(firstItem).perform(longClick())
        onView(allOf(withId(R.id.edit_overlay), withParent(firstItem)))
            .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))

        // long click on second item, overlay should be gone on first item and visible on second
        onView(secondItem).perform(longClick())
        onView(allOf(withId(R.id.edit_overlay), withParent(firstItem)))
            .check(matches(withEffectiveVisibility(Visibility.GONE)))
        onView(allOf(withId(R.id.edit_overlay), withParent(secondItem)))
            .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))

        // press back navigation, overlay should be gone, but we should still be on CategoryFragment
        Espresso.pressBack()
        onView(allOf(withId(R.id.edit_overlay), withParent(secondItem)))
            .check(matches(withEffectiveVisibility(Visibility.GONE)))
        onView(withId(R.id.recipe_list)).check(matches(isDisplayed()))
    }

    /**
     * Test navigation to edit recipe
     */
    @Test
    fun testEditRecipeNavigation() {
        val id = insertTestRecipes(1)[0]
        onView(isRoot()).perform(waitForView(R.id.recipe_list))

        // we can use item_wrapper here because there's only one list item
        onView(withId(R.id.item_wrapper)).perform(longClick())
        onView(withId(R.id.edit_button)).perform(click())

        verify(navController).navigate(
            RecipeCategoryFragmentDirections.toAddRecipeNavGraph(
                recipeId = id,
                clearValues = true
            )
        )
    }

    /**
     * Open delete dialog and click cancel, recipe should not be deleted
     * Open dialog again and click delete, recipe should be deleted
     */
    @Test
    fun testDeleteRecipe() {
        insertTestRecipes(1)
        onView(isRoot()).perform(waitForView(R.id.recipe_list))

        // we can use item_wrapper here because there's only one list item
        onView(withId(R.id.item_wrapper)).perform(longClick())
        onView(withId(R.id.delete_button)).perform(click())

        // click cancel
        onView(withId(R.id.delete_dialog_layout)).check(matches(isDisplayed()))
        onView(withId(R.id.alert_button_negative)).perform(click())
        onView(withId(R.id.delete_dialog_layout)).check(doesNotExist())
        onView(withId(R.id.recipe_list)).check(matches(withListSize(1)))

        // open again and delete
        onView(withId(R.id.item_wrapper)).perform(longClick())
        onView(withId(R.id.delete_button)).perform(click())
        onView(withId(R.id.alert_button_positive)).perform(click())
        onView(withId(R.id.delete_dialog_layout)).check(doesNotExist())

        onView(withId(R.id.recipe_list)).check(matches(withListSize(0)))
    }

    /////

    /**
     * Insert given number of Recipes with TEST_CATEGORY to database
     * @return list of all inserted IDs
     */
    private fun insertTestRecipes(quantity: Int): MutableList<Long> {
        val ids = mutableListOf<Long>()
        runBlocking {
            for (i in 1..quantity) {
                val id = db.recipeDao().insert(
                    TestDataProvider.getRandomRecipe().apply { category = TEST_CATEGORY }
                )
                ids.add(id)
            }
        }
        return ids
    }
}
