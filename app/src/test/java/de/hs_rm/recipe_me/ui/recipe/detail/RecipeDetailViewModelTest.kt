package de.hs_rm.recipe_me.ui.recipe.detail

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.google.common.truth.Truth.assertThat
import de.hs_rm.recipe_me.declaration.MainCoroutineRule
import de.hs_rm.recipe_me.model.recipe.Recipe
import de.hs_rm.recipe_me.model.recipe.RecipeCategory
import de.hs_rm.recipe_me.model.relation.RecipeWithRelations
import de.hs_rm.recipe_me.service.repository.RecipeImageRepository
import de.hs_rm.recipe_me.service.repository.RecipeRepository
import de.hs_rm.recipe_me.service.repository.ShoppingListRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.*
import test_shared.TestDataProvider
import test_shared.declaration.getOrAwaitValue

class RecipeDetailViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    @get:Rule
    val coroutineRule = MainCoroutineRule()

    @Test
    fun canLoadRecipe() {
        val recipe = TestDataProvider.getRandomRecipe()
        val recipeWithRelations = RecipeWithRelations(recipe, listOf(), listOf())

        val recipeRepository: RecipeRepository = mock {
            on { getRecipeWithRelationsById(eq(1)) } doReturn MutableLiveData(recipeWithRelations)
        }
        val viewModel = RecipeDetailViewModel(recipeRepository, mock(), mock())

        viewModel.loadRecipe(1)

        verify(recipeRepository, times(1)).getRecipeWithRelationsById(1)
        assertThat(viewModel.recipe.getOrAwaitValue()).isNotNull()
        assertThat(viewModel.recipe.getOrAwaitValue()).isSameInstanceAs(recipeWithRelations)
    }

    @Test
    fun canIncreaseServings() {
        val viewModel = RecipeDetailViewModel(mock(), mock(), mock())
        val before = viewModel.servings.get()

        viewModel.increaseServings()
        val after = viewModel.servings.get()

        assertThat(after).isEqualTo(before + 1)
    }

    @Test
    fun canDecreaseServings() {
        val viewModel = RecipeDetailViewModel(mock(), mock(), mock())
        viewModel.servings.set(2)
        val before = viewModel.servings.get()

        viewModel.decreaseServings()
        val after = viewModel.servings.get()

        assertThat(after).isEqualTo(before - 1)
    }

    @Test
    fun doesNotDecreaseServingsBelowOne() {
        val viewModel = RecipeDetailViewModel(mock(), mock(), mock())
        viewModel.servings.set(1)

        viewModel.decreaseServings()
        val after = viewModel.servings.get()

        assertThat(after).isEqualTo(1)
    }

    @Test
    fun canClearSelections() {
        val recipe = TestDataProvider.getRandomRecipe()
        val ingredients = listOf(
            TestDataProvider.getRandomIngredient().apply { checked = true },
            TestDataProvider.getRandomIngredient().apply { checked = true },
            TestDataProvider.getRandomIngredient(),
            TestDataProvider.getRandomIngredient().apply { checked = true },
        )
        val recipeWithRelations = RecipeWithRelations(recipe, ingredients, listOf())
        val recipeRepository: RecipeRepository = mock {
            on { getRecipeWithRelationsById(any()) } doReturn MutableLiveData(recipeWithRelations)
        }
        val viewModel = RecipeDetailViewModel(recipeRepository, mock(), mock())

        viewModel.loadRecipe(1)

        viewModel.clearSelections()
        for (ingredient in recipeWithRelations.ingredients) {
            assertThat(ingredient.checked).isFalse()
        }
    }

    @Test
    fun canCalculateServingsMultiplier() {
        val recipe = Recipe().apply { servings = 3 }
        val recipeWithRelations = RecipeWithRelations(recipe, listOf(), listOf())
        val recipeRepository: RecipeRepository = mock {
            on { getRecipeWithRelationsById(eq(1)) } doReturn MutableLiveData(recipeWithRelations)
        }
        val viewModel = RecipeDetailViewModel(recipeRepository, mock(), mock())
        viewModel.loadRecipe(1)

        viewModel.servings.set(1)
        assertThat(300 * viewModel.getServingsMultiplier()).isEqualTo(100)

        viewModel.servings.set(6)
        assertThat(100 * viewModel.getServingsMultiplier()).isEqualTo(200)

        viewModel.servings.set(3)
        assertThat(20 * viewModel.getServingsMultiplier()).isEqualTo(20)
    }

    @Test
    fun canAddSelectedIngredientsToShoppingList() {
        val recipe = TestDataProvider.getRandomRecipe()
        val ingredients = listOf(
            TestDataProvider.getRandomIngredient().apply { checked = true },
            TestDataProvider.getRandomIngredient().apply { checked = true },
            TestDataProvider.getRandomIngredient(),
            TestDataProvider.getRandomIngredient().apply { checked = true },
        )
        val recipeWithRelations = RecipeWithRelations(recipe, ingredients, listOf())
        val recipeRepository: RecipeRepository = mock {
            on { getRecipeWithRelationsById(any()) } doReturn MutableLiveData(recipeWithRelations)
        }
        val shoppingListRepository: ShoppingListRepository = mock {
            onBlocking { addOrUpdateFromIngredient(any(), any()) } doAnswer {}
        }
        val viewModel = RecipeDetailViewModel(recipeRepository, mock(), shoppingListRepository)
        viewModel.loadRecipe(1)

        viewModel.addSelectedIngredientsToShoppingList()

        verifyBlocking(shoppingListRepository, times(3)) { addOrUpdateFromIngredient(any(), any()) }
    }

    @Test
    fun getGetRecipeFile() {
        val imageRepository: RecipeImageRepository = mock {
            on { getRecipeImageFile(1) } doReturn mock()
        }
        val viewModel = RecipeDetailViewModel(mock(), imageRepository, mock())

        val imageFile = viewModel.getRecipeImageFile(1)

        assertThat(imageFile).isNotNull()
    }

}