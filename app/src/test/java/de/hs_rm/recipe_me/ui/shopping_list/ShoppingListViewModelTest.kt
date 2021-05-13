package de.hs_rm.recipe_me.ui.shopping_list

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.google.common.truth.Truth.assertThat
import de.hs_rm.recipe_me.EditableMock
import de.hs_rm.recipe_me.declaration.MainCoroutineRule
import de.hs_rm.recipe_me.model.user.User
import de.hs_rm.recipe_me.service.repository.ShoppingListRepository
import de.hs_rm.recipe_me.service.repository.UserRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.*
import test_shared.TestDataProvider
import test_shared.declaration.getOrAwaitValue

class ShoppingListViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    @get:Rule
    val coroutineRule = MainCoroutineRule()

    /**
     * Test that items get loaded from repository
     */
    @Test
    fun canLoadShoppingListItems() {
        val items = listOf(
            TestDataProvider.getRandomShoppingListItem(),
            TestDataProvider.getRandomShoppingListItem(),
            TestDataProvider.getRandomShoppingListItem()
        )

        val shoppingListRepository: ShoppingListRepository = mock()
        whenever(shoppingListRepository.getAllItems()).thenReturn(
            MutableLiveData(items)
        )
        val userRepository: UserRepository = mock()

        val viewModel = ShoppingListViewModel(shoppingListRepository, userRepository)
        viewModel.loadShoppingListItems()
        val loadedItems = viewModel.shoppingListItems.getOrAwaitValue()

        assertThat(loadedItems).isSameInstanceAs(items)
        assertThat(loadedItems).hasSize(3)
    }

    /**
     * Test function of isItemChecked()
     */
    @Test
    fun canIdentifyCheckedItems() {
        val shoppingListRepository: ShoppingListRepository = mock()
        whenever(shoppingListRepository.getAllItems()).thenReturn(
            MutableLiveData(
                listOf(
                    TestDataProvider.getRandomShoppingListItem(),
                    TestDataProvider.getRandomShoppingListItem(),
                    TestDataProvider.getRandomShoppingListItem()
                )
            )
        )
        val userRepository: UserRepository = mock()

        val viewModel = ShoppingListViewModel(shoppingListRepository, userRepository)
        viewModel.loadShoppingListItems()
        viewModel.shoppingListItems.getOrAwaitValue()

        var itemChecked = viewModel.isItemChecked()
        assertThat(itemChecked!!).isFalse()

        viewModel.toggleItemChecked(1)
        itemChecked = viewModel.isItemChecked()
        assertThat(itemChecked!!).isTrue()
    }

    /**
     * Test function of toggleItemChecked() and allItemsChecked()
     */
    @Test
    fun canCountCheckedItems() {
        val shoppingListRepository: ShoppingListRepository = mock {
            on { getAllItems() } doReturn MutableLiveData(
                listOf(
                    TestDataProvider.getRandomShoppingListItem(),
                    TestDataProvider.getRandomShoppingListItem(),
                )
            )
        }
        val userRepository: UserRepository = mock()

        val viewModel = ShoppingListViewModel(shoppingListRepository, userRepository)
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

    /**
     * Test that adding item calls repository function with trimmed Editable
     */
    @Test
    fun canAddShoppingListItems() {
        val shoppingListRepository: ShoppingListRepository = mock {
            onBlocking { addFromString(any()) } doAnswer {}
        }
        val userRepository: UserRepository = mock()
        val viewModel = ShoppingListViewModel(shoppingListRepository, userRepository)

        viewModel.addShoppingListItem(EditableMock(" name   "))

        verifyBlocking(shoppingListRepository, times(1)) { addFromString("name") }
    }

    /**
     * Test that clearCheckedItems calls function in repository
     */
    @Test
    fun canClearCheckedItems() {
        val shoppingListRepository: ShoppingListRepository = mock {
            onBlocking { clearCheckedItems() } doAnswer {}
        }
        val userRepository: UserRepository = mock()
        val viewModel = ShoppingListViewModel(shoppingListRepository, userRepository)

        viewModel.clearCheckedItems()

        verifyBlocking(shoppingListRepository, times(1)) { clearCheckedItems() }
    }

    /**
     * Test that user can be loaded from repository
     */
    @Test
    fun canLoadUser() {
        val user = User()

        val shoppingListRepository: ShoppingListRepository = mock()
        val userRepository: UserRepository = mock {
            on { getUser() } doReturn MutableLiveData(user)
        }
        val viewModel = ShoppingListViewModel(shoppingListRepository, userRepository)

        viewModel.loadUser()

        verifyBlocking(userRepository, times(1)) { getUser() }
        val loadedUser = viewModel.user.getOrAwaitValue()
        assertThat(loadedUser).isSameInstanceAs(user)
    }

}
