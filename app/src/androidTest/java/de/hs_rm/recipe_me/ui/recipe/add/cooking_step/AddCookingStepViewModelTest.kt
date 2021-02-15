package de.hs_rm.recipe_me.ui.recipe.add.cooking_step

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import de.hs_rm.recipe_me.TestDataProvider
import de.hs_rm.recipe_me.declaration.getOrAwaitValue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Tests for [AddCookingStepViewModel]
 */
class AddCookingStepViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: AddCookingStepViewModel

    @Before
    fun init() {
        viewModel = AddCookingStepViewModel()
    }

    /**
     * Test if list of assigned ingredients is initialized successfully
     */
    @Test
    fun initializationSuccessful() {
        assertNotNull(viewModel.assignedIngredients.getOrAwaitValue())
    }

    /**
     * Test if checked states can be reset successfully (list will be cleared)
     */
    @Test
    fun resetCheckedStatesSuccessful() {
        val ingredient1 = TestDataProvider.getRandomIngredient(0)
        val ingredient2 = TestDataProvider.getRandomIngredient(0)
        viewModel.addAssignedIngredients(listOf(ingredient1, ingredient2))

        viewModel.resetCheckedStates()
        val countAfter = viewModel.assignedIngredients.getOrAwaitValue().size

        assertEquals(0, countAfter)
    }

    /**
     * Test if checked state is toggled successful
     */
    @Test
    fun toggleCheckedStateSuccessful() {
        val ingredient1 = TestDataProvider.getRandomIngredient(0)
        val ingredient2 = TestDataProvider.getRandomIngredient(0)

        viewModel.toggleCheckedState(ingredient1)

        viewModel.assignedIngredients.getOrAwaitValue()

        val ingredients = viewModel.assignedIngredients.getOrAwaitValue()
        var count = ingredients.size

        assertEquals(1, count)

        viewModel.toggleCheckedState(ingredient2)

        count = ingredients.size
        assertEquals(2, count)

        viewModel.toggleCheckedState(ingredient1)
        viewModel.toggleCheckedState(ingredient2)

        count = ingredients.size
        assertEquals(0, count)
    }

    /**
     * Test if list of ingredients is successful assigned to ViewModel
     */
    @Test
    fun addAssignedIngredients() {
        val ingredient1 = TestDataProvider.getRandomIngredient(0)
        val ingredient2 = TestDataProvider.getRandomIngredient(0)

        val countBefore = viewModel.assignedIngredients.getOrAwaitValue().size

        viewModel.addAssignedIngredients(listOf(ingredient1, ingredient2))
        val countAfter = viewModel.assignedIngredients.getOrAwaitValue().size

        assertEquals(countBefore + 2, countAfter)
    }
}
