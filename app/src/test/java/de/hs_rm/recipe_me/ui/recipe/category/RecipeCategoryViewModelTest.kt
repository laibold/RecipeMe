package de.hs_rm.recipe_me.ui.recipe.category

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.google.common.truth.Truth.assertThat
import test_shared.TestDataProvider
import test_shared.declaration.getOrAwaitValue
import de.hs_rm.recipe_me.declaration.MainCoroutineRule
import de.hs_rm.recipe_me.model.recipe.Recipe
import de.hs_rm.recipe_me.model.recipe.RecipeCategory
import de.hs_rm.recipe_me.service.repository.RecipeImageRepository
import de.hs_rm.recipe_me.service.repository.RecipeRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.*
import java.io.File

class RecipeCategoryViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    @get:Rule
    val coroutineRule = MainCoroutineRule()

    private lateinit var recipeRepository: RecipeRepository

    private lateinit var imageRepository: RecipeImageRepository

    @Before
    fun init() {
        recipeRepository = mock()
        imageRepository = mock()
    }

    /**
     * Test that getRecipesByCategory returns LiveData with list of recipes
     */
    @Test
    fun getsRecipesByCategory() {
        val category = RecipeCategory.BAKED_GOODS

        val recipes = MutableLiveData(
            listOf(
                TestDataProvider.getRandomRecipe(),
                TestDataProvider.getRandomRecipe(),
                TestDataProvider.getRandomRecipe()
            )
        )

        whenever(recipeRepository.getRecipesByCategory(category)).thenReturn(recipes)

        val viewModel = RecipeCategoryViewModel(recipeRepository, imageRepository)

        val categoryRecipes = viewModel.getRecipesByCategory(category).getOrAwaitValue()

        assertThat(categoryRecipes).isNotNull()
        assertThat(categoryRecipes.size).isEqualTo(3)
    }

    /**
     * Test that viewModel returns recipe image file from ImageRepository
     */
    @Test
    fun getsRecipeImageFile() {
        val id = 77L
        val file = File("image")
        whenever(imageRepository.getRecipeImageFile(id)).thenReturn(file)

        val viewModel = RecipeCategoryViewModel(recipeRepository, imageRepository)
        viewModel.getRecipeImageFile(id)

        val returnedFile = viewModel.getRecipeImageFile(id)
        assertThat(returnedFile).isSameInstanceAs(file)
    }

    /**
     * Test that deleteRecipeAndRelations calls functions in imageRepository and recipeRepository
     */
    @Test
    fun deletesRecipe() {
        val recipeId = 5L
        val recipe = Recipe().apply { id = recipeId }

        val recipeRepository: RecipeRepository = mock {
            onBlocking { deleteRecipeAndRelations(recipe) } doAnswer {}
        }
        whenever(imageRepository.deleteRecipeImage(recipeId)).thenAnswer { }

        val viewModel = RecipeCategoryViewModel(recipeRepository, imageRepository)
        viewModel.deleteRecipeAndRelations(recipe)

        verify(imageRepository, times(1)).deleteRecipeImage(recipeId)
        verifyBlocking(recipeRepository, times(1)) { deleteRecipeAndRelations(recipe) }
    }

}
