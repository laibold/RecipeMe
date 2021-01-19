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
    suspend fun addOrUpdateFromIngredient(ingredient: Ingredient) {
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
     * Update the given item
     */
    suspend fun updateItem(item: ShoppingListItem) {
        shoppingListDao.update(item)
    }

    /**
     * Delete the given item
     */
    suspend fun deleteItem(item: ShoppingListItem) {
        shoppingListDao.delete(item)
    }

    /**
     * Get LiveData with List of all Items
     */
    fun getAllItems(): LiveData<List<ShoppingListItem>> {
        return shoppingListDao.getItems()
    }

    /**
     * Private method to find an existing item
     */
    private fun findExistingItem(ingredient: Ingredient): ShoppingListItem? {
        return shoppingListDao.getItemByNameAndUnit(ingredient.name, ingredient.unit)
    }

}
