package de.hs_rm.recipe_me.ui.shopping_list

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.platform.app.InstrumentationRegistry
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import de.hs_rm.recipe_me.Constants
import test_shared.TestDataProvider
import test_shared.declaration.getOrAwaitValue
import de.hs_rm.recipe_me.persistence.AppDatabase
import de.hs_rm.recipe_me.service.repository.ShoppingListRepository
import de.hs_rm.recipe_me.service.repository.UserRepository
import kotlinx.coroutines.runBlocking
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
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

    private lateinit var viewModel: ShoppingListViewModel

    private lateinit var appContext: Context

    /**
     * Build inMemory database and create ViewModel
     */
    @Before
    fun beforeEach() {
        hiltRule.inject()
        assertThat(db.openHelper.databaseName).isEqualTo(AppDatabase.Environment.TEST.dbName)

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
        assertThat(itemChecked!!).isFalse()

        viewModel.toggleItemChecked(1)
        itemChecked = viewModel.isItemChecked()
        assertThat(itemChecked!!).isTrue()
    }

    @Test
    fun testCountCheckedItems() {
        insertTestItems(2)
        viewModel.loadShoppingListItems()
        viewModel.shoppingListItems.getOrAwaitValue()

        var allItemsChecked = viewModel.allItemsChecked()
        assertThat(allItemsChecked).isFalse()

        viewModel.toggleItemChecked(0)
        allItemsChecked = viewModel.allItemsChecked()
        assertThat(allItemsChecked).isFalse()

        viewModel.toggleItemChecked(1)
        allItemsChecked = viewModel.allItemsChecked()
        assertThat(allItemsChecked).isTrue()
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
