package de.hs_rm.recipe_me.declaration.ui

import android.view.View
import androidx.appcompat.widget.TooltipCompat
import androidx.databinding.BindingAdapter

/**
 * This can be used in xml to set tooltip texts via ' app:tooltipTextCompat="@{@string/string_name}" '.
 * ' android:toolTipText="..." ' can just be used vom API 26+ and this is a workaround to not set
 * every tooltip text programmatically via TooltipCompat.setTooltipText() in the belonging Fragments.
 */
@BindingAdapter("tooltipTextCompat")
fun bindTooltipText(view: View, tooltipText: String) {
    TooltipCompat.setTooltipText(view, tooltipText)
}
