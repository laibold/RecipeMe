package de.hs_rm.recipe_me.ui.shopping_list

import android.text.Editable
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.hs_rm.recipe_me.model.shopping_list.ShoppingListItem
import de.hs_rm.recipe_me.model.user.User
import de.hs_rm.recipe_me.service.repository.ShoppingListRepository
import de.hs_rm.recipe_me.service.repository.UserRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for [ShoppingListFragment]
 */
@HiltViewModel
class ShoppingListViewModel @Inject constructor(
    private val shoppingListRepository: ShoppingListRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    lateinit var shoppingListItems: LiveData<List<ShoppingListItem>>
    lateinit var user: LiveData<User?>

    /**
     * Load items from repository and set them to shoppingListItems which can be observed
     */
    fun loadShoppingListItems() {
        shoppingListItems = shoppingListRepository.getAllItems()
    }

    /**
     * Returns if one or more items are checked
     */
    fun isItemChecked(): Boolean? {
        return shoppingListItems.value?.map { it.checked }?.contains(true)
    }

    /**
     * Returns if all items are checked
     */
    fun allItemsChecked(): Boolean {
        val countItems = shoppingListItems.value?.size
        val countChecked = shoppingListItems.value?.filter { it.checked }?.size
        return countItems ==  countChecked
    }

    /**
     * Updates given item in database
     */
    private fun updateItem(item: ShoppingListItem) {
        viewModelScope.launch {
            shoppingListRepository.updateItem(item)
        }
    }

    /**
     * Add item to shopping list
     */
    fun addShoppingListItem(name: Editable) {
        viewModelScope.launch {
            shoppingListRepository.addFromString(name.toString().trim())
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

    /**
     * Delete Checked Items from repository
     */
    fun clearCheckedItems() {
        viewModelScope.launch {
            shoppingListRepository.clearCheckedItems()
        }
    }

    /**
     * Load user from userRepository and save it to ViewModel
     */
    fun loadUser() {
        user = userRepository.getUser()
    }

}
