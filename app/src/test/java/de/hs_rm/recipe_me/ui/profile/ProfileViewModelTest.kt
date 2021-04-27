package de.hs_rm.recipe_me.ui.profile

import android.net.Uri
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.google.common.truth.Truth.assertThat
import de.hs_rm.recipe_me.declaration.MainCoroutineRule
import de.hs_rm.recipe_me.model.user.User
import de.hs_rm.recipe_me.service.repository.RecipeRepository
import de.hs_rm.recipe_me.service.repository.UserImageRepository
import de.hs_rm.recipe_me.service.repository.UserRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.*
import test_shared.declaration.getOrAwaitValue

class ProfileViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    @get:Rule
    val coroutineRule = MainCoroutineRule()

    lateinit var recipeRepository: RecipeRepository
    lateinit var userRepository: UserRepository
    lateinit var imageRepository: UserImageRepository

    lateinit var viewModel: ProfileViewModel

    @Before
    fun init() {
        recipeRepository = mock {
            on { getRecipeTotal() } doReturn MutableLiveData(3)
        }
        userRepository = mock {
            on { getUser() } doReturn MutableLiveData(User("username"))
            onBlocking { insertOrUpdate(any()) } doAnswer {}
        }
        imageRepository = mock {
            on { getProfileImage() } doReturn mock()
            on { saveProfileImage(any()) } doAnswer {}
            on { getImageFromUri(any(), any(), any()) } doReturn mock()
        }

        viewModel = ProfileViewModel(recipeRepository, userRepository, imageRepository)
    }

    @After
    fun cleanup() {
        Mockito.reset(recipeRepository)
        Mockito.reset(userRepository)
        Mockito.reset(imageRepository)
    }

    /**
     * Test if loadRecipeTotal() calls method in repository and sets attribute
     */
    @Test
    fun canLoadRecipeTotal() {
        //val viewModel = ProfileViewModel(recipeRepository, mock(), mock())

        viewModel.loadRecipeTotal()

        verify(recipeRepository, times(1)).getRecipeTotal()
        assertThat(viewModel.recipeTotal.getOrAwaitValue()).isEqualTo(3)
    }

    /**
     * Test if loadUser() calls method in repository and sets attribute
     */
    @Test
    fun canLoadUser() {
        viewModel.loadUser()

        verify(userRepository, times(1)).getUser()
        verify(imageRepository, times(1)).getProfileImage()
        assertThat(viewModel.user.getOrAwaitValue()).isEqualTo(User("username"))
        assertThat(viewModel.profileImage.getOrAwaitValue()).isNotNull()
    }

    /**
     * Test if saving of user and profile image is delegated to the repositories
     * and that its function sets profileImage and clear editProfileImage
     */
    @Test
    fun canSaveUserWithImage() {
        val username = "username"
        viewModel._editProfileImage.value = mock()

        viewModel.saveUser("   $username ")

        verifyBlocking(userRepository, times(1)) { insertOrUpdate(username) }
        verify(imageRepository, times(1)).saveProfileImage(any())
        assertThat(viewModel.profileImage.getOrAwaitValue()).isNotNull()
        assertThat(viewModel.editProfileImage.getOrAwaitValue()).isNull()
    }

    /**
     * Test if saving of user without profile image is delegated to the repository
     */
    @Test
    fun canSaveUserWithoutImage() {
        val username = "username"

        viewModel.saveUser("   $username ")

        verifyBlocking(userRepository, times(1)) { insertOrUpdate(username) }
        verify(imageRepository, never()).saveProfileImage(any())
        assertThat(viewModel.profileImage.value).isNull()
        assertThat(viewModel.editProfileImage.value).isNull()
    }

    /**
     * Test if editProfileImage can be cleared
     */
    @Test
    fun canClearEditProfileImage() {
        viewModel.clearEditProfileImage()
        assertThat(viewModel.editProfileImage.getOrAwaitValue()).isNull()
    }

    /**
     * Test generation of recipeTotalText
     */
    @Test
    fun canGetRecipeTotalText() {
        val firstPart = "Du hast bisher"
        val mapStr = "{\"1\":\"Rezept erstellt - da geht noch mehr!\"," +
                "\"50\":\"Rezepte erstellt - deine Waage wollt\\' ich nicht sein.\"," +
                "\"61\":\"Rezepte erstellt - du bist ein wahrer Meisterkoch!\"}"

        val text1 = viewModel.getRecipeTotalText(firstPart, mapStr, 1)
        val text46 = viewModel.getRecipeTotalText(firstPart, mapStr, 46)
        val text100 = viewModel.getRecipeTotalText(firstPart, mapStr, 100)

        assertThat(text1).isEqualTo("Du hast bisher 1 Rezept erstellt - da geht noch mehr!")
        assertThat(text46).isEqualTo("Du hast bisher 46 Rezepte erstellt - deine Waage wollt\' ich nicht sein.")
        assertThat(text100).isEqualTo("Du hast bisher 100 Rezepte erstellt - du bist ein wahrer Meisterkoch!")
    }

    /**
     * Test if Bitmap of editProfile image can be set
     */
    @Test
    fun canSetPickedRecipeImage() {
        val imageUri: Uri = mock()

        viewModel.setPickedRecipeImage(imageUri, 1, 2)

        verify(imageRepository, times(1)).getImageFromUri(eq(imageUri), eq(1), eq(2))
        assertThat(viewModel.editProfileImage.getOrAwaitValue()).isNotNull()
    }

}
