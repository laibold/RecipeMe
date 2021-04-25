package de.hs_rm.recipe_me.ui.recipe.add.cooking_step

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import de.hs_rm.recipe_me.declaration.MainCoroutineRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import test_shared.TestDataProvider
import test_shared.declaration.getOrAwaitValue

/**
 * Tests for [AddCookingStepViewModel]
 */
class AddCookingStepViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    @get:Rule
    val coroutineRule = MainCoroutineRule()

    private lateinit var viewModel: AddCookingStepViewModel

    @Before
    fun beforeEach() {
        viewModel = AddCookingStepViewModel()
    }

    /**
     * Test if list of assigned ingredients is initialized successfully
     */
    @Test
    fun initializesSuccessful() {
        assertThat(viewModel.assignedIngredients.getOrAwaitValue()).isNotNull()
    }

    /**
     * Test if checked states can be reset successfully (list will be cleared)
     */
    @Test
    fun resetsCheckedStatesSuccessful() {
        val ingredient1 = TestDataProvider.getRandomIngredient(0)
        val ingredient2 = TestDataProvider.getRandomIngredient(0)
        viewModel.addAssignedIngredients(listOf(ingredient1, ingredient2))

        viewModel.resetCheckedStates()
        val countAfter = viewModel.assignedIngredients.getOrAwaitValue().size

        assertThat(countAfter).isEqualTo(0)
    }

    /**
     * Test if checked state is toggled successful
     */
    @Test
    fun togglesCheckedStateSuccessful() {
        val ingredient1 = TestDataProvider.getRandomIngredient(0)
        val ingredient2 = TestDataProvider.getRandomIngredient(0)

        viewModel.toggleCheckedState(ingredient1)

        viewModel.assignedIngredients.getOrAwaitValue()

        val ingredients = viewModel.assignedIngredients.getOrAwaitValue()
        var count = ingredients.size

        assertThat(count).isEqualTo(1)

        viewModel.toggleCheckedState(ingredient2)

        count = ingredients.size
        assertThat(count).isEqualTo(2)

        viewModel.toggleCheckedState(ingredient1)
        viewModel.toggleCheckedState(ingredient2)

        count = ingredients.size
        assertThat(count).isEqualTo(0)
    }

    /**
     * Test if list of ingredients is successful assigned to ViewModel
     */
    @Test
    fun addsAssignedIngredients() {
        val ingredient1 = TestDataProvider.getRandomIngredient(0)
        val ingredient2 = TestDataProvider.getRandomIngredient(0)

        val countBefore = viewModel.assignedIngredients.getOrAwaitValue().size

        viewModel.addAssignedIngredients(listOf(ingredient1, ingredient2))
        val countAfter = viewModel.assignedIngredients.getOrAwaitValue().size

        assertThat(countAfter).isEqualTo(countBefore + 2)
    }

}
