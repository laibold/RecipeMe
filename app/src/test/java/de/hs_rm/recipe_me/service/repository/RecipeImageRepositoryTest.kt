//package de.hs_rm.recipe_me.service.repository
//
//import android.graphics.Bitmap
//import android.net.Uri
//import androidx.arch.core.executor.testing.InstantTaskExecutorRule
//import androidx.test.platform.app.InstrumentationRegistry
//import com.google.common.truth.Truth
//import de.hs_rm.recipe_me.TempDir
//import org.junit.Before
//import org.junit.Rule
//import org.junit.Test
//import java.io.File
//
//class RecipeImageRepositoryTest {
//
//    @get:Rule
//    var instantExecutorRule = InstantTaskExecutorRule()
//
//    private lateinit var repository: RecipeImageRepository
//
//    @Before
//    fun beforeEach() {
//        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
//        repository = RecipeImageRepository(appContext)
//    }
//
//    @Test
//    fun canGetImageFromUri() {
//        val img = saveBitmap("image.png")
//        val uri = Uri.fromFile(img)
//
//        val imgFromRepo = repository.getImageFromUri(uri, 1, 2)
//
//        Truth.assertThat(imgFromRepo).isNotNull()
//    }
//
//    fun canSaveRecipeImage() {
//
//    }
//
//    fun canGetRecipeImage() {
//
//    }
//
//    fun canGetRecipeImageFile() {
//
//    }
//
//    fun canDeleteRecipeImage() {
//
//    }
//
//    /////
//
//    private fun saveBitmap(filename: String): File {
//        val bitmap = Bitmap.createBitmap(10, 10, Bitmap.Config.RGB_565)
//        val tempDir = TempDir()
//        val img = File(tempDir.getFile(), filename).apply { createNewFile() }
//        img.outputStream().use { out ->
//            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
//        }
//
//        return img
//    }
//
//}
