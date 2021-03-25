package de.hs_rm.recipe_me.ui.shopping_list

import android.content.Context
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import de.hs_rm.recipe_me.Constants
import de.hs_rm.recipe_me.R
import de.hs_rm.recipe_me.declaration.espresso.withListSize
import de.hs_rm.recipe_me.declaration.launchFragmentInHiltContainer
import de.hs_rm.recipe_me.model.shopping_list.ShoppingListItem
import de.hs_rm.recipe_me.persistence.AppDatabase
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers.anything
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito
import javax.inject.Inject
import javax.inject.Named

@HiltAndroidTest
class ShoppingListFragmentTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    @Named(Constants.TEST_NAME)
    lateinit var db: AppDatabase

    lateinit var context: Context

    @Before
    fun beforeEach() {
        hiltRule.inject()
        Assert.assertEquals(AppDatabase.Environment.TEST.dbName, db.openHelper.databaseName)

        context = InstrumentationRegistry.getInstrumentation().targetContext
        db.clearAllTables()

        val navController = Mockito.mock(NavController::class.java)
        launchFragmentInHiltContainer<ShoppingListFragment> {
            Navigation.setViewNavController(requireView(), navController)
        }
    }

    /**
     * Check that on empty list empty state text is shown and buttons (clear & share) are gone
     */
    @Test
    fun testEmptyState() {
        val emptyStateText = context.getString(R.string.add_items_text)
        onView(withId(R.id.add_hint_text)).check(matches(withText(emptyStateText)))

        onView(withId(R.id.share_button)).check(matches(withEffectiveVisibility(Visibility.GONE)))
        onView(withId(R.id.clear_list_button)).check(matches(withEffectiveVisibility(Visibility.GONE)))
    }

    /**
     * Check adding only succeeds if text is given and that buttons are displayed if list is not empty
     */
    @Test
    fun addItems() {
        val itemName = "New item"

        // Try to add item without text
        onView(withId(R.id.list_view)).check(matches(withListSize(0)))
        onView(withId(R.id.add_item_button)).perform(click())
        onView(withId(R.id.list_view)).check(matches(withListSize(0)))

        // Add item with text
        onView(withId(R.id.add_item_edit_text)).perform(typeText(itemName))
        onView(withId(R.id.add_item_button)).perform(click())
        onView(withId(R.id.list_view)).check(matches(withListSize(1)))

        // check name and checked state
        onData(anything()).inAdapterView(withId(R.id.list_view)).atPosition(0)
            .onChildView(withId(R.id.item_text)).check(matches(withText(itemName)))
        onData(anything()).inAdapterView(withId(R.id.list_view)).atPosition(0)
            .onChildView(withId(R.id.item_checkbox)).check(matches(isNotChecked()))

        // Check is buttons are displayed now
        onView(withId(R.id.share_button)).check(matches(isDisplayed()))
        onView(withId(R.id.clear_list_button)).check(matches(isDisplayed()))
    }

    /**
     * Test checking and unchecking of items
     */
    @Test
    fun testCheckItems() {
        runBlocking {
            db.shoppingListDao().insert(ShoppingListItem("Item1"))
        }

        // should be unchecked by default
        onData(anything()).inAdapterView(withId(R.id.list_view)).atPosition(0)
            .onChildView(withId(R.id.item_checkbox)).check(matches(isNotChecked()))

        // check
        onData(anything()).inAdapterView(withId(R.id.list_view)).atPosition(0)
            .onChildView(withId(R.id.item_checkbox)).perform(click())
        onData(anything()).inAdapterView(withId(R.id.list_view)).atPosition(0)
            .onChildView(withId(R.id.item_checkbox)).check(matches(isChecked()))

        // uncheck
        onData(anything()).inAdapterView(withId(R.id.list_view)).atPosition(0)
            .onChildView(withId(R.id.item_checkbox)).perform(click())
        onData(anything()).inAdapterView(withId(R.id.list_view)).atPosition(0)
            .onChildView(withId(R.id.item_checkbox)).check(matches(isNotChecked()))
    }

    /**
     * Test removing items from list (only checked ones)
     */
    @Test
    fun removeItems() {
        runBlocking {
            db.shoppingListDao().insert(ShoppingListItem("Item1"))
            db.shoppingListDao().insert(ShoppingListItem("Item2"))
            db.shoppingListDao().insert(ShoppingListItem("Item3"))
        }

        // click clear button without checking items
        onView(withId(R.id.list_view)).check(matches(withListSize(3)))
        onView(withId(R.id.clear_list_button)).perform(click())
        onView(withId(R.id.list_view)).check(matches(withListSize(3)))

        // check 2 items and clear
        onData(anything()).inAdapterView(withId(R.id.list_view)).atPosition(0)
            .onChildView(withId(R.id.item_checkbox)).perform(click())
        onData(anything()).inAdapterView(withId(R.id.list_view)).atPosition(1)
            .onChildView(withId(R.id.item_checkbox)).perform(click())
        onView(withId(R.id.clear_list_button)).perform(click())

        // 1 item should be left
        onView(withId(R.id.list_view)).check(matches(withListSize(1)))
    }

    //TODO test share button (Intent)

}
