package de.hs_rm.recipe_me

import android.view.View
import android.widget.ListView
import androidx.test.espresso.*
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.util.HumanReadables
import androidx.test.espresso.util.TreeIterables
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import java.util.concurrent.TimeoutException

/**
 * This ViewAction tells espresso to wait till a certain view is found in the view hierarchy.
 * @param viewId The id of the view to wait for.
 * @param timeout The maximum time which espresso will wait for the view to show up (in milliseconds)
 */
fun waitForView(viewId: Int, timeout: Long = 2000): ViewAction {
    return object : ViewAction {
        override fun getConstraints(): Matcher<View> {
            return isRoot()
        }

        override fun getDescription(): String {
            return "wait for a specific view with id $viewId; during $timeout millis."
        }

        override fun perform(uiController: UiController, rootView: View) {
            uiController.loopMainThreadUntilIdle()
            val startTime = System.currentTimeMillis()
            val endTime = startTime + timeout
            val viewMatcher = withId(viewId)

            do {
                // Iterate through all views on the screen and see if the view we are looking for is there already
                for (child in TreeIterables.breadthFirstViewTraversal(rootView)) {
                    // found view with required ID
                    if (viewMatcher.matches(child)) {
                        return
                    }
                }
                // Loops the main thread for a specified period of time.
                // Control may not return immediately, instead it'll return after the provided delay has passed and the queue is in an idle state again.
                uiController.loopMainThreadForAtLeast(100)
            } while (System.currentTimeMillis() < endTime) // in case of a timeout we throw an exception -&gt; test fails
            throw PerformException.Builder()
                .withCause(TimeoutException())
                .withActionDescription(this.description)
                .withViewDescription(HumanReadables.describe(rootView))
                .build()
        }
    }
}


// View Assertions

fun ViewInteraction.checkIsGone() = getViewAssertion(ViewMatchers.Visibility.GONE)
fun ViewInteraction.checkIsVisible() = getViewAssertion(ViewMatchers.Visibility.VISIBLE)
fun ViewInteraction.checkIsInvisible() = getViewAssertion(ViewMatchers.Visibility.INVISIBLE)

private fun getViewAssertion(visibility: ViewMatchers.Visibility): ViewAssertion? {
    return ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(visibility))
}

//

fun withListSize(size: Int): Matcher<View?>? {
    return object : TypeSafeMatcher<View?>() {
        override fun matchesSafely(item: View?): Boolean {
            return (item as ListView).count == size
        }

        override fun describeTo(description: Description) {
            description.appendText("ListView should have $size items")
        }

    }
}
