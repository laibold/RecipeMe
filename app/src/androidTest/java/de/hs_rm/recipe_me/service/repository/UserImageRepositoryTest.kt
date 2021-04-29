package de.hs_rm.recipe_me.service.repository

import android.graphics.Bitmap
import android.net.Uri
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidTest
import de.hs_rm.recipe_me.TempDir
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File

@HiltAndroidTest
class UserImageRepositoryTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var repository: UserImageRepository

    @Before
    fun beforeEach() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        repository = UserImageRepository(appContext)
    }

    @Test
    fun canGetImageFromUri() {
        val img = saveBitmap("image.png")
        val uri = Uri.fromFile(img)

        val imgFromRepo = repository.getImageFromUri(uri, 1, 2)

        assertThat(imgFromRepo).isNotNull()
    }

    // TODO
    fun canGetProfileImage() {

    }

    // TODO
    fun canSaveProfileImage() {

    }

    private fun saveBitmap(filename: String): File {
        val bitmap = Bitmap.createBitmap(10, 10, Bitmap.Config.RGB_565)
        val tempDir = TempDir()
        val img = File(tempDir.getFile(), filename).apply { createNewFile() }
        img.outputStream().use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }

        return img
    }

}
