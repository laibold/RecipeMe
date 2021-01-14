package de.hs_rm.recipe_me.ui.recipe

import android.content.Context
import android.util.AttributeSet
import android.widget.ListView

class UnscrollableListView : ListView {

    constructor(ctx: Context) : super(ctx)

    constructor(ctx: Context, attrs: AttributeSet) : super(ctx, attrs)

    constructor(ctx: Context, attrs: AttributeSet, defStyle: Int) : super(ctx, attrs, defStyle)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val expandSpec: Int = MeasureSpec.makeMeasureSpec(Int.MAX_VALUE shr 2, MeasureSpec.AT_MOST)
        super.onMeasure(widthMeasureSpec, expandSpec)
    }

}