package de.hs_rm.recipe_me.declaration

import androidx.lifecycle.MutableLiveData

/**
 * Notify observers of MutableLiveData. Observers won't recognize if for example
 * if the content of a [MutableList] inside of LiveData gets changed.
 * Call this method as a workaround.
 */
fun <T> MutableLiveData<T>.notifyObserver() {
    this.value = this.value
}
