package de.hs_rm.recipe_me.service.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import de.hs_rm.recipe_me.declaration.MainCoroutineRule
import de.hs_rm.recipe_me.service.ImageHandler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.*

class UserImageRepositoryTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    @get:Rule
    val coroutineRule = MainCoroutineRule()

    @Test
    fun canGetImageFromUri() {
        val imageHandler: ImageHandler = mock {
            on { getImageFromUri(any(), eq(1), eq(2)) } doReturn mock()
        }
        val repository = UserImageRepository(imageHandler)

        val imgFromRepo = repository.getImageFromUri(mock(), 1, 2)

        verify(imageHandler, times(1)).getImageFromUri(any(), eq(1), eq(2))
        assertThat(imgFromRepo).isNotNull()
    }

    @Test
    fun canGetProfileImage() {
        val imageHandler: ImageHandler = mock {
            on { getProfileImage() } doReturn mock()
        }
        val repository = UserImageRepository(imageHandler)

        val imgFromRepo = repository.getProfileImage()

        assertThat(imgFromRepo).isNotNull()
    }

    @Test
    fun canSaveProfileImage() {
        val imageHandler: ImageHandler = mock {
            onBlocking { saveProfileImage(any()) } doReturn ""
        }
        val repository = UserImageRepository(imageHandler)

        repository.saveProfileImage(mock())

        verifyBlocking(imageHandler, times(1)) { saveProfileImage(any()) }
    }

}
