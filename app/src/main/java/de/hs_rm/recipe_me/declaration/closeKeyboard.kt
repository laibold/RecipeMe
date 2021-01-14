package de.hs_rm.recipe_me.declaration

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.FragmentActivity

fun FragmentActivity?.closeKeyboard() {
    val view: View? = this?.currentFocus
    if (view != null) {
        val imm: InputMethodManager =
            this?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}
