package de.hs_rm.recipe_me.declaration

import androidx.lifecycle.MutableLiveData

/**
 * Add item to MutableList in MutableLiveData and notify observers.
 * Observers won't recognize if for example if the content of a [MutableList] inside of LiveData gets changed.
 * Call this method as a workaround.
 */
fun <T> MutableLiveData<MutableList<T>>.addToValue(item: T) {
    val updatedItems = this.value as ArrayList
    updatedItems.add(item)
    this.postValue(updatedItems)
}

/**
 * Set item at given index at MutableList in MutableLiveData and notify observers.
 * Observers won't recognize if for example if the content of a [MutableList] inside of LiveData gets changed.
 * Call this method as a workaround.
 */
fun <T> MutableLiveData<MutableList<T>>.setValueAt(index: Int, item: T) {
    val items = this.value as ArrayList
    items[index] = item
    this.postValue(items)
}

/**
 * Notify observers of MutableLiveData. Observers won't recognize if for example
 * if the content of a [MutableList] inside of LiveData gets changed.
 * Call this method as a workaround.
 */
fun <T> MutableLiveData<T>.notifyObservers() {
    this.value = this.value
}
