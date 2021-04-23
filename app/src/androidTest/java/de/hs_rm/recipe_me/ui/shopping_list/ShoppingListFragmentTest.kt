package de.hs_rm.recipe_me.ui.shopping_list

import android.content.Context
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
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
import org.junit.Before
import org.junit.Rule
import org.junit.Test
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
        assertThat(db.openHelper.databaseName).isEqualTo(AppDatabase.Environment.TEST.dbName)

        context = InstrumentationRegistry.getInstrumentation().targetContext
        db.clearAllTables()

        launchFragmentInHiltContainer<ShoppingListFragment>()
    }

    /**
     * Check that on empty list the empty state text is shown and buttons (clear & share) are gone
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
        onView(withId(R.id.add_item_edit_text)).perform(replaceText(itemName))
        onView(withId(R.id.add_item_button)).perform(click())
        onView(withId(R.id.list_view)).check(matches(withListSize(1)))

        // check name and checked state
        onData(anything()).inAdapterView(withId(R.id.list_view)).atPosition(0)
            .onChildView(withId(R.id.item_text)).check(matches(withText(itemName)))
        onData(anything()).inAdapterView(withId(R.id.list_view)).atPosition(0)
            .onChildView(withId(R.id.item_checkbox)).check(matches(isNotChecked()))

        // Check if buttons are displayed now
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

        Thread.sleep(100)

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
