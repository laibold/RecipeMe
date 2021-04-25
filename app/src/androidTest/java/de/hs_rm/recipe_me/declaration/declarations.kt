package de.hs_rm.recipe_me.declaration

import android.text.Editable
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

fun String.toEditable(): Editable = Editable.Factory.getInstance().newEditable(this)

fun Int.toEditable(): Editable = Editable.Factory.getInstance().newEditable(this.toString())

fun Double.toEditable(): Editable = Editable.Factory.getInstance().newEditable(this.toString())
