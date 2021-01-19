package de.hs_rm.recipe_me.ui.shopping_list

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import de.hs_rm.recipe_me.model.shopping_list.ShoppingListItem
import de.hs_rm.recipe_me.service.ShoppingListRepository

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

}
