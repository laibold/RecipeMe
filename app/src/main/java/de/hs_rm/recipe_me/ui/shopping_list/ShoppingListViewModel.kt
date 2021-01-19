package de.hs_rm.recipe_me.ui.shopping_list

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
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

    fun addShoppingListItem(name: String) {
        viewModelScope.launch {
            repository.addFromString(name)
        }
    }

}
