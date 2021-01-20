package de.hs_rm.recipe_me.ui.shopping_list

import android.text.Editable
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.hs_rm.recipe_me.model.shopping_list.ShoppingListItem
import de.hs_rm.recipe_me.service.ShoppingListRepository
import kotlinx.coroutines.launch

/**
 * ViewModel for [ShoppingListFragment]
 */
class ShoppingListViewModel @ViewModelInject constructor(
    private val repository: ShoppingListRepository
) : ViewModel() {

    lateinit var shoppingListItems: LiveData<List<ShoppingListItem>>

    /**
     * Load items from repository and set them to shoppingListItems which can be observed
     */
    fun loadShoppingListItems() {
        shoppingListItems = repository.getAllItems()
    }

    /**
     * Update given item
     */
    private fun updateItem(item: ShoppingListItem) {
        viewModelScope.launch {
            repository.updateItem(item)
        }
    }

    /**
     * Add item to shopping list
     */
    fun addShoppingListItem(name: Editable) {
        viewModelScope.launch {
            repository.addFromString(name.toString().trim())
        }
    }

    /**
     * Switch value of attribute 'checked' of item with given index
     */
    fun toggleItemChecked(index: Int) {
        val item = shoppingListItems.value?.get(index)
        if (item != null) {
            item.toggleChecked()
            updateItem(item)
        }
    }

}
