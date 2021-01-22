package de.hs_rm.recipe_me.service

import androidx.lifecycle.LiveData
import de.hs_rm.recipe_me.model.recipe.Ingredient
import de.hs_rm.recipe_me.model.shopping_list.ShoppingListItem
import de.hs_rm.recipe_me.persistence.ShoppingListDao
import javax.inject.Inject

/**
 * Single Source of Truth for [ShoppingListItem]. Use it with Dependency Injection
 */
class ShoppingListRepository @Inject constructor(
    private val shoppingListDao: ShoppingListDao
) {

    suspend fun addFromString(name: String) {
        val newShoppingListItem = ShoppingListItem(name)
        shoppingListDao.insert(newShoppingListItem)
    }

    /**
     * Hand in an Ingredient. If there is already is an item with the same name and unit in the repo,
     * the quantity of the item will be increased. If not, the item will be added to the list.
     * @param ingredient Ingredient to be added to the repository
     */
    suspend fun addOrUpdateFromIngredient(ingredient: Ingredient, multiplier: Double = 1.0) {
        ingredient.quantity *= multiplier
        val item = findExistingItem(ingredient)
        if (item != null) {
            item.addIngredient(ingredient)
            shoppingListDao.update(item)
        } else {
            val newShoppingListItem = ShoppingListItem(ingredient)
            shoppingListDao.insert(newShoppingListItem)
        }
    }

    /**
     * Insert the given item
     */
    suspend fun insertItem(item: ShoppingListItem): Long {
        return shoppingListDao.insert(item)
    }

    /**
     * Update the given item
     */
    suspend fun updateItem(item: ShoppingListItem) {
        shoppingListDao.update(item)
    }

    /**
     * Delete item by given id
     */
    suspend fun deleteItemById(id: Long) {
        shoppingListDao.delete(id)
    }

    /**
     * Get LiveData with List of all Items
     */
    fun getAllItems(): LiveData<List<ShoppingListItem>> {
        return shoppingListDao.getItems()
    }

    /**
     * Private method to find an existing item by its name and unit
     */
    private suspend fun findExistingItem(ingredient: Ingredient): ShoppingListItem? {
        return shoppingListDao.getItemByNameAndUnit(ingredient.name, ingredient.unit)
    }

    /**
     * Delete Items where checked == true
     */
    suspend fun clearCheckedItems() {
        shoppingListDao.deleteChecked()
    }

}
