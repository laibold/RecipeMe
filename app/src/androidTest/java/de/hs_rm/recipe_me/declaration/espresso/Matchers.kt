package de.hs_rm.recipe_me.declaration.espresso

import android.view.View
import android.widget.ListView
import android.widget.Spinner
import androidx.recyclerview.widget.RecyclerView
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher

// TODO add some waiting time
fun withListSize(size: Int): Matcher<View?> {
    return object : TypeSafeMatcher<View?>() {
        override fun matchesSafely(item: View?): Boolean {
            if (item is ListView) {
                return item.count == size
            } else if (item is RecyclerView) {
                return item.adapter?.itemCount == size
            }
            throw ClassCastException(item!!::class.toString() + " must be ListView or RecyclerView")
        }

        override fun describeTo(description: Description) {
            description.appendText("ListView should have $size items")
        }

    }
}

fun withSpinnerSize(size: Int): Matcher<View?> {
    return object : TypeSafeMatcher<View?>() {
        override fun matchesSafely(item: View?): Boolean {
            if (item is Spinner) {
                return item.adapter.count == size
            }
            throw ClassCastException(item!!::class.toString() + " must be Spinner")
        }

        override fun describeTo(description: Description) {
            description.appendText("Spinner should have $size items")
        }

    }
}
