package de.hs_rm.recipe_me.persistence

import androidx.lifecycle.LiveData
import androidx.room.*
import de.hs_rm.recipe_me.model.recipe.IngredientUnit
import de.hs_rm.recipe_me.model.shopping_list.ShoppingListItem

/**
 * Data Access Object for [ShoppingListItem]s
 */
@Dao
interface ShoppingListDao {

    @Insert
    suspend fun insert(item: ShoppingListItem): Long

    @Update
    suspend fun update(item: ShoppingListItem)

    @Delete
    suspend fun delete(item: ShoppingListItem)

    @Query("SELECT * FROM ShoppingListItem")
    fun getItems(): LiveData<List<ShoppingListItem>>

    @Query("SELECT * FROM ShoppingListItem WHERE name = :name AND unit = :unit LIMIT 1")
    fun getItemByNameAndUnit(name: String, unit: IngredientUnit): ShoppingListItem?

}
