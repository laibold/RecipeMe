package de.hs_rm.recipe_me.service.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import de.hs_rm.recipe_me.model.recipe.Ingredient
import de.hs_rm.recipe_me.model.recipe.IngredientUnit
import de.hs_rm.recipe_me.model.shopping_list.ShoppingListItem
import de.hs_rm.recipe_me.persistence.AppDatabase
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import test_shared.declaration.getOrAwaitValue

class ShoppingListRepositoryTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    lateinit var repository: ShoppingListRepository

    @Before
    fun beforeEach() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val db = Room.inMemoryDatabaseBuilder(appContext, AppDatabase::class.java).build()
        val dao = db.shoppingListDao()
        repository = ShoppingListRepository(dao)
        insertTestItems()
    }

    /**
     * Tests if the quantities of the same [Ingredient] (added from a recipe) are summed up
     */
    @Test
    fun testAddOrUpdate() {
        runBlocking {
            repository.addOrUpdateFromIngredient(
                Ingredient(1L, "Flour", 200.0, IngredientUnit.GRAM)
            )
            repository.addOrUpdateFromIngredient(
                Ingredient(2L, "Flour", 200.0, IngredientUnit.GRAM)
            )
            repository.addOrUpdateFromIngredient(
                Ingredient(3L, "Paprika", 1.0, IngredientUnit.NONE)
            )
        }

        val allItems = repository.getAllItems().getOrAwaitValue()

        assertThat(allItems.size).isEqualTo(3) // 2 + testItem

        assertThat(allItems[1].quantity).isEqualTo(400.0)
        assertThat(allItems[0].quantity).isEqualTo(1.0)
    }

    private fun insertTestItems() {
        runBlocking {
            repository.insertItem(ShoppingListItem("Item", 3.0, IngredientUnit.GRAM))
        }
    }

}
