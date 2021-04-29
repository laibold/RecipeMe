package de.hs_rm.recipe_me.service

import android.content.Context
import com.google.common.truth.Truth.assertThat
import de.hs_rm.recipe_me.model.recipe.Recipe
import de.hs_rm.recipe_me.model.recipe.RecipeCategory
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy
import test_shared.TempDir
import java.io.File

class ImageHandlerTest {

    /**
     * Test if recipe image can be saved and directories are created
     */
    @Test
    fun canSaveRecipeImage() {
        val tempDir = TempDir()
        val context: Context = mock {
            on { getExternalFilesDir(null) } doReturn tempDir.getFile()
        }
        val imageHandler = ImageHandler(context)

        imageHandler.saveRecipeImage(mock(), 1)

        val imgFile = File(tempDir.getFile().absolutePath, "/images/recipes/1/recipe_image.jpg")
        assertThat(imgFile.exists()).isTrue()
    }

    /**
     * Test if recipe image file can be deleted successfully
     */
    @Test
    fun canDeleteRecipeImage() {
        val tempDir = TempDir()
        val tempImage = File(tempDir.getFile(), "/images/recipes/1/recipe_image.jpg")
        tempImage.mkdirs()
        tempImage.createNewFile()
        val context: Context = mock {
            on { getExternalFilesDir(null) } doReturn tempDir.getFile()
        }
        val imageHandler = ImageHandler(context)

        imageHandler.deleteRecipeImage(1)

        assertThat(tempImage.exists()).isFalse()
    }

    /**
     * Test if recipe image can be loaded from file system
     */
    @Test
    fun canGetRecipeImage() {
        val tempDir = TempDir()
        val tempImage = File(tempDir.getFile(), "/images/recipes/1/recipe_image.jpg")
        tempImage.mkdirs()
        tempImage.createNewFile()
        val context: Context = mock {
            on { getExternalFilesDir(null) } doReturn tempDir.getFile()
        }
        val imageHandler = spy(ImageHandler(context)) {
            onGeneric { getImageFromFile(any(), any(), any()) } doReturn mock()
        }
        val recipe = Recipe(RecipeCategory.MAIN_DISHES).apply { id = 1 }

        val bitmap = imageHandler.getRecipeImage(recipe)

        assertThat(bitmap).isNotNull()
    }

    /**
     * Test if null is returned when no image is saved for recipe but no error happens
     */
    @Test
    fun returnsNullOnNonExistingRecipeImage() {
        val tempDir = TempDir()
        val context: Context = mock {
            on { getExternalFilesDir(null) } doReturn tempDir.getFile()
        }
        val imageHandler = spy(ImageHandler(context)) {
            onGeneric { getImageFromFile(any(), any(), any()) } doReturn mock()
        }
        val recipe = Recipe(RecipeCategory.MAIN_DISHES).apply { id = 1 }

        val bitmap = imageHandler.getRecipeImage(recipe)

        assertThat(bitmap).isNull()
    }

    /**
     * Test that file of recipe image gets returned when existing
     */
    @Test
    fun canGetRecipeImageFile() {
        val tempDir = TempDir()
        val tempImage = File(tempDir.getFile(), "/images/recipes/1/recipe_image.jpg")
        tempImage.mkdirs()
        tempImage.createNewFile()
        val context: Context = mock {
            on { getExternalFilesDir(null) } doReturn tempDir.getFile()
        }
        val imageHandler = ImageHandler(context)

        val file = imageHandler.getRecipeImageFile(1)

        assertThat(file).isNotNull()
    }

    /**
     * Test if null is returned when no image file is existing for recipe but no error happens
     */
    @Test
    fun returnsNullOnNonExistingRecipeImageFile() {
        val tempDir = TempDir()
        val context: Context = mock {
            on { getExternalFilesDir(null) } doReturn tempDir.getFile()
        }
        val imageHandler = ImageHandler(context)

        val file = imageHandler.getRecipeImageFile(1)

        assertThat(file).isNull()
    }

    /**
     * Test if profile image can be saved and directories are created
     */
    @Test
    fun canSaveProfileImage() {
        val tempDir = TempDir()
        val context: Context = mock {
            on { getExternalFilesDir(null) } doReturn tempDir.getFile()
        }
        val imageHandler = ImageHandler(context)

        imageHandler.saveProfileImage(mock())

        val imgFile = File(tempDir.getFile().absolutePath, "/images/profile/profile_image.jpg")
        assertThat(imgFile.exists()).isTrue()
    }

    /**
     * Test if profile image can be saved if one already exists
     */
    @Test
    fun canOverwriteProfileImage() {
        val tempDir = TempDir()
        val context: Context = mock {
            on { getExternalFilesDir(null) } doReturn tempDir.getFile()
        }
        val imageHandler = ImageHandler(context)

        imageHandler.saveProfileImage(mock())
        imageHandler.saveProfileImage(mock())

        val imgFile = File(tempDir.getFile().absolutePath, "/images/profile/profile_image.jpg")
        assertThat(imgFile.exists()).isTrue()
        val files = File(tempDir.getFile().absolutePath, "/images/profile").listFiles()
        assertThat(files).hasLength(1)
    }

    /**
     * Test if profile image can be loaded from file system
     */
    @Test
    fun canGetProfileImage() {
        val tempDir = TempDir()
        val tempImage = File(tempDir.getFile(), "/images/profile/profile_image.jpg")
        tempImage.mkdirs()
        tempImage.createNewFile()
        val context: Context = mock {
            on { getExternalFilesDir(null) } doReturn tempDir.getFile()
        }
        val imageHandler = spy(ImageHandler(context)) {
            onGeneric { getImageFromFile(any(), any(), any()) } doReturn mock()
        }

        val bitmap = imageHandler.getProfileImage()

        assertThat(bitmap).isNotNull()
    }

    /**
     * Test if null is returned when no profile image is saved but no error happens
     */
    @Test
    fun returnsNullOnNonExistingProfileImage() {
        val tempDir = TempDir()

        val context: Context = mock {
            on { getExternalFilesDir(null) } doReturn tempDir.getFile()
        }
        val imageHandler = spy(ImageHandler(context)) {
            onGeneric { getImageFromFile(any(), any(), any()) } doReturn mock()
        }

        val bitmap = imageHandler.getProfileImage()

        assertThat(bitmap).isNull()
    }

}
