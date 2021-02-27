package de.hs_rm.recipe_me.ui.shopping_list

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import de.hs_rm.recipe_me.TestDataProvider
import de.hs_rm.recipe_me.declaration.getOrAwaitValue
import de.hs_rm.recipe_me.persistence.AppDatabase
import de.hs_rm.recipe_me.persistence.dao.ShoppingListDao
import de.hs_rm.recipe_me.persistence.dao.UserDao
import de.hs_rm.recipe_me.service.repository.ShoppingListRepository
import de.hs_rm.recipe_me.service.repository.UserRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.Executors

@RunWith(AndroidJUnit4::class)
class ShoppingListViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var db: AppDatabase
    private lateinit var shoppingListRepository: ShoppingListRepository
    private lateinit var userRepository: UserRepository
    private lateinit var shoppingListDao: ShoppingListDao
    private lateinit var userDao: UserDao
    private lateinit var viewModel: ShoppingListViewModel

    private lateinit var appContext: Context

    /**
     * Build inMemory database and create ViewModel
     */
    @Before
    fun init() {
        appContext = InstrumentationRegistry.getInstrumentation().targetContext
    }

    /**
     * Clear all database tables and re-initialize ViewModel and its recipe
     */
    private fun beforeEach() {
        db = Room.inMemoryDatabaseBuilder(appContext, AppDatabase::class.java)
            .setTransactionExecutor(Executors.newSingleThreadExecutor())
            .build()

        shoppingListDao = db.shoppingListDao()
        userDao = db.userDao()
        shoppingListRepository = ShoppingListRepository(shoppingListDao)
        userRepository = UserRepository(userDao)

        db.clearAllTables()
        viewModel = ShoppingListViewModel(shoppingListRepository, userRepository)
    }

    @Test
    fun testIsItemChecked() {
        beforeEach()
        insertTestItems(5)
        viewModel.loadShoppingListItems()
        viewModel.shoppingListItems.getOrAwaitValue()

        var itemChecked = viewModel.isItemChecked()
        assertFalse(itemChecked!!)

        viewModel.toggleItemChecked(1)
        itemChecked = viewModel.isItemChecked()
        assertTrue(itemChecked!!)
    }

    @Test
    fun testCountCheckedItems() {
        beforeEach()
        insertTestItems(2)
        viewModel.loadShoppingListItems()
        viewModel.shoppingListItems.getOrAwaitValue()

        var allItemsChecked = viewModel.allItemsChecked()
        assertFalse(allItemsChecked)

        viewModel.toggleItemChecked(0)
        allItemsChecked = viewModel.allItemsChecked()
        assertFalse(allItemsChecked)

        viewModel.toggleItemChecked(1)
        allItemsChecked = viewModel.allItemsChecked()
        assertTrue(allItemsChecked)
    }

    /////

    private fun insertTestItems(amount: Int) {
        runBlocking {
            for (i in 1..amount) {
                shoppingListRepository.insertItem(TestDataProvider.getRandomShoppingListItem())
            }
        }
    }
}
