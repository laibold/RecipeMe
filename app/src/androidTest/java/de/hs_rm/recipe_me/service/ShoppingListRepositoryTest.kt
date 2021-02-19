package de.hs_rm.recipe_me.service

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import de.hs_rm.recipe_me.declaration.getOrAwaitValue
import de.hs_rm.recipe_me.model.recipe.Ingredient
import de.hs_rm.recipe_me.model.recipe.IngredientUnit
import de.hs_rm.recipe_me.model.shopping_list.ShoppingListItem
import de.hs_rm.recipe_me.persistence.AppDatabase
import de.hs_rm.recipe_me.persistence.dao.ShoppingListDao
import de.hs_rm.recipe_me.service.repository.ShoppingListRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ShoppingListRepositoryTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var db: AppDatabase
    private lateinit var shoppingListDao: ShoppingListDao
    private lateinit var repository: ShoppingListRepository

    private lateinit var appContext: Context
    private val testIds = mutableListOf<Long>()

    @Before
    fun init() {
        appContext = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(appContext, AppDatabase::class.java).build()
        shoppingListDao = db.shoppingListDao()
        repository = ShoppingListRepository(shoppingListDao)

        insertTestItems()
    }

    /**
     * Tests if the quantities of the same [Ingredient] (added from a recipe) are summed up
     */
    @Test
    fun testAddOrUpdate() {
        runBlocking {
            repository.addOrUpdateFromIngredient(
                Ingredient(
                    1L,
                    "Flour",
                    200.0,
                    IngredientUnit.GRAM
                )
            )
            repository.addOrUpdateFromIngredient(
                Ingredient(
                    2L,
                    "Flour",
                    200.0,
                    IngredientUnit.GRAM
                )
            )
            repository.addOrUpdateFromIngredient(
                Ingredient(
                    3L,
                    "Paprika",
                    1.0,
                    IngredientUnit.NONE
                )
            )
        }

        val allItems = repository.getAllItems().getOrAwaitValue()

        assertEquals(3, allItems.size) // 2 + testItem

        assertEquals(400.0, allItems[1].quantity, 0.0)
        assertEquals(1.0, allItems[0].quantity, 0.0)
    }

    private fun insertTestItems() {
        runBlocking {
            testIds.add(repository.insertItem(ShoppingListItem("Item", 3.0, IngredientUnit.GRAM)))
        }
    }

}
