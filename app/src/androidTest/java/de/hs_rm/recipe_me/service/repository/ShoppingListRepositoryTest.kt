package de.hs_rm.recipe_me.service.repository

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.platform.app.InstrumentationRegistry
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import de.hs_rm.recipe_me.Constants
import de.hs_rm.recipe_me.declaration.getOrAwaitValue
import de.hs_rm.recipe_me.model.recipe.Ingredient
import de.hs_rm.recipe_me.model.recipe.IngredientUnit
import de.hs_rm.recipe_me.model.shopping_list.ShoppingListItem
import de.hs_rm.recipe_me.persistence.AppDatabase
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject
import javax.inject.Named

@HiltAndroidTest
class ShoppingListRepositoryTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Inject
    @Named(Constants.TEST_NAME)
    lateinit var db: AppDatabase

    @Inject
    @Named(Constants.TEST_NAME)
    lateinit var repository: ShoppingListRepository

    private lateinit var appContext: Context

    @Before
    fun beforeEach() {
        hiltRule.inject()
        assertEquals(AppDatabase.Environment.TEST.dbName, db.openHelper.databaseName)

        appContext = InstrumentationRegistry.getInstrumentation().targetContext
        db.clearAllTables()
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
            repository.insertItem(ShoppingListItem("Item", 3.0, IngredientUnit.GRAM))
        }
    }

}
