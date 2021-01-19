package de.hs_rm.recipe_me.persistence

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import de.hs_rm.recipe_me.model.shopping_list.ShoppingListItem

/**
 * Data Access Object for [ShoppingListItem]s
 */
@Dao
interface ShoppingListDao {

    @Insert
    suspend fun insert(item: ShoppingListItem): Long

    @Query("SELECT * FROM ShoppingListItem")
    fun getItems(): LiveData<List<ShoppingListItem>>

}
