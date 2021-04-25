package de.hs_rm.recipe_me.ui.recipe.home

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import de.hs_rm.recipe_me.declaration.MainCoroutineRule
import test_shared.declaration.getOrAwaitValue
import de.hs_rm.recipe_me.model.recipe.Recipe
import de.hs_rm.recipe_me.service.repository.RecipeImageRepository
import de.hs_rm.recipe_me.service.repository.RecipeOfTheDayRepository
import de.hs_rm.recipe_me.service.repository.RecipeRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.*
import java.io.File

class RecipeHomeViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    @get:Rule
    val coroutineRule = MainCoroutineRule()

    /**
     * Test if recipeOfTheDay gets loaded
     */
    @Test
    fun loadsRecipeOfTheDay() {
        val recipe = Recipe()

        val recipeOfTheDayRepository: RecipeOfTheDayRepository = mock {
            onBlocking { updateRecipeOfTheDay() } doAnswer {}
            onBlocking { getRecipeOfTheDayId() } doReturn 77L
        }
        val recipeRepository: RecipeRepository = mock {
            onBlocking { getRecipeById(77L) } doReturn (recipe)
        }
        val recipeImageRepository: RecipeImageRepository = mock()
        val viewModel =
            RecipeHomeViewModel(recipeOfTheDayRepository, recipeRepository, recipeImageRepository)

        viewModel.loadRecipeOfTheDay()

        verifyBlocking(recipeOfTheDayRepository, times(1)) { updateRecipeOfTheDay() }
        assertThat(viewModel.recipeOfTheDay.getOrAwaitValue()).isEqualTo(recipe)
    }

    /**
     * Test if file for recipe image gets returned
     */
    @Test
    fun returnsRecipeImageFile() {
        val imageFile = File("path")

        val recipeOfTheDayRepository: RecipeOfTheDayRepository = mock()
        val recipeRepository: RecipeRepository = mock()
        val recipeImageRepository: RecipeImageRepository = mock {
            on { getRecipeImageFile(77) } doReturn imageFile
        }
        val viewModel =
            RecipeHomeViewModel(recipeOfTheDayRepository, recipeRepository, recipeImageRepository)

        val outputFile = viewModel.getRecipeImageFile(77)
        assertThat(outputFile).isSameInstanceAs(imageFile)
    }

}
