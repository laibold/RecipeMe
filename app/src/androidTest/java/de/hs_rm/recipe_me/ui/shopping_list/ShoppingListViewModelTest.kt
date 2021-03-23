package de.hs_rm.recipe_me.ui.shopping_list

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import de.hs_rm.recipe_me.Constants
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
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.inject.Named

@HiltAndroidTest
class ShoppingListViewModelTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Inject
    @Named(Constants.TEST_NAME)
    lateinit var db: AppDatabase

    @Inject
    @Named(Constants.TEST_NAME)
    lateinit var shoppingListRepository: ShoppingListRepository

    @Inject
    @Named(Constants.TEST_NAME)
    lateinit var userRepository: UserRepository

    @Inject
    @Named(Constants.TEST_NAME)
    lateinit var shoppingListDao: ShoppingListDao

    @Inject
    @Named(Constants.TEST_NAME)
    lateinit var userDao: UserDao

    private lateinit var viewModel: ShoppingListViewModel

    private lateinit var appContext: Context

    /**
     * Build inMemory database and create ViewModel
     */
    @Before
    fun beforeEach() {
        hiltRule.inject()
        assertEquals(AppDatabase.Environment.TEST.dbName, db.openHelper.databaseName)

        viewModel = ShoppingListViewModel(shoppingListRepository, userRepository)
        appContext = InstrumentationRegistry.getInstrumentation().targetContext
        db.clearAllTables()
    }

    @Test
    fun testIsItemChecked() {
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
