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

    @Query("DELETE FROM ShoppingListItem WHERE checked = 1")
    suspend fun deleteChecked()

    @Query("SELECT * FROM ShoppingListItem ORDER BY id DESC")
    fun getItems(): LiveData<List<ShoppingListItem>>

    @Query("SELECT * FROM ShoppingListItem WHERE name = :name AND unit = :unit LIMIT 1")
    suspend fun getItemByNameAndUnit(name: String, unit: IngredientUnit): ShoppingListItem?

}
