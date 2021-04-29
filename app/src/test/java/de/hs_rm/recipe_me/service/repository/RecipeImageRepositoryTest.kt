package de.hs_rm.recipe_me.service.repository

import android.graphics.Bitmap
import android.net.Uri
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import de.hs_rm.recipe_me.declaration.MainCoroutineRule
import de.hs_rm.recipe_me.model.recipe.Recipe
import de.hs_rm.recipe_me.service.ImageHandler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.*

class RecipeImageRepositoryTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    @get:Rule
    val coroutineRule = MainCoroutineRule()

    @Test
    fun canGetImageFromUri() {
        val uri: Uri = mock()
        val imageHandler: ImageHandler = mock {
            on { getImageFromUri(any(), eq(1), eq(2)) } doReturn mock()
        }
        val repository = RecipeImageRepository(imageHandler)

        val imgFromRepo = repository.getImageFromUri(uri, 1, 2)

        assertThat(imgFromRepo).isNotNull()
    }

    @Test
    fun canSaveRecipeImage() {
        val bitmap: Bitmap = mock()
        val imageHandler: ImageHandler = mock {
            onBlocking { saveRecipeImage(any(), eq(1)) } doReturn ""
        }
        val repository = RecipeImageRepository(imageHandler)

        repository.saveRecipeImage(bitmap, 1)

        verifyBlocking(imageHandler, times(1)) { saveRecipeImage(eq(bitmap), eq(1)) }
    }

    @Test
    fun canGetRecipeImage() {
        val recipe: Recipe = mock()
        val imageHandler: ImageHandler = mock {
            on { getRecipeImage(recipe) } doReturn mock()
        }
        val repository = RecipeImageRepository(imageHandler)

        val bitmap = repository.getRecipeImage(recipe)

        verify(imageHandler, times(1)).getRecipeImage(eq(recipe))
        assertThat(bitmap).isNotNull()
    }

    @Test
    fun canGetRecipeImageFile() {
        val imageHandler: ImageHandler = mock {
            on { getRecipeImageFile(eq(1)) } doReturn mock()
        }
        val repository = RecipeImageRepository(imageHandler)

        val file = repository.getRecipeImageFile(1)

        verify(imageHandler, times(1)).getRecipeImageFile(eq(1))
        assertThat(file).isNotNull()
    }

    @Test
    fun canDeleteRecipeImage() {
        val imageHandler: ImageHandler = mock {
            onBlocking { deleteRecipeImage(eq(1)) } doAnswer {}
        }
        val repository = RecipeImageRepository(imageHandler)

        repository.deleteRecipeImage(1)

        verifyBlocking(imageHandler, times(1)) { deleteRecipeImage(eq(1)) }
    }

}
