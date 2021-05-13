package de.hs_rm.recipe_me.declaration

import android.text.Editable

fun String.toEditable(): Editable = Editable.Factory.getInstance().newEditable(this)

fun Int.toEditable(): Editable = Editable.Factory.getInstance().newEditable(this.toString())

fun Double.toEditable(): Editable = Editable.Factory.getInstance().newEditable(this.toString())

fun Boolean.toInt() = if (this) 1 else 0
