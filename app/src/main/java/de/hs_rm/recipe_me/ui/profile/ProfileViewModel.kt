package de.hs_rm.recipe_me.ui.profile

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.lifecycle.HiltViewModel
import de.hs_rm.recipe_me.model.user.User
import de.hs_rm.recipe_me.service.repository.RecipeRepository
import de.hs_rm.recipe_me.service.repository.UserImageRepository
import de.hs_rm.recipe_me.service.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for [ProfileViewModel]
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository,
    private val userRepository: UserRepository,
    private val imageRepository: UserImageRepository
) : ViewModel() {

    lateinit var total: LiveData<Int>
    lateinit var user: LiveData<User?>

    private val _editProfileImage = MutableLiveData<Bitmap?>()
    val editProfileImage: LiveData<Bitmap?>
        get() = _editProfileImage

    private val _profileImage = MutableLiveData<Bitmap?>()
    val profileImage: LiveData<Bitmap?>
        get() = _profileImage

    /**
     * Get total from repository and save it to ViewModel
     */
    fun loadRecipeTotal() {
        total = recipeRepository.getRecipeTotal()
    }

    /**
     * Load user from repository and save it to ViewModel
     */
    fun loadUser() {
        user = userRepository.getUser()
        CoroutineScope(Dispatchers.IO).launch {
            _profileImage.postValue(imageRepository.getProfileImage())
        }
    }

    /**
     * Insert or update user with given name in the repository.
     * Save image if provided.
     */
    fun saveUser(name: String) {
        if (name.trim() != "") {
            viewModelScope.launch {
                userRepository.insertOrUpdate(name.trim())
            }
        }
        _editProfileImage.value?.let {
            CoroutineScope(Dispatchers.IO).launch {
                _profileImage.postValue(it)
            }
            imageRepository.saveProfileImage(it)
        }
        clearEditProfileImage()
    }

    /**
     * Reset editProfile image to null
     */
    fun clearEditProfileImage() {
        _editProfileImage.value = null
    }

    /**
     * Returns text with number of recipes and message
     * @param firstPart text like "You already created..."
     * @param mapStr HashMap with int-Strings as key and message as value
     * @param total number of created recipes
     */
    fun getRecipeTotalText(firstPart: String, mapStr: String, total: Int): String {
        val messageMap: HashMap<String, String> =
            Gson().fromJson(mapStr, object : TypeToken<HashMap<String, String>>() {}.type)

        val message = when {
            total == 1 -> messageMap["1"].toString()
            total <= 10 -> messageMap["10"].toString()
            total <= 20 -> messageMap["20"].toString()
            total <= 30 -> messageMap["30"].toString()
            total <= 40 -> messageMap["40"].toString()
            total <= 50 -> messageMap["50"].toString()
            total <= 60 -> messageMap["60"].toString()
            else -> messageMap["61"].toString()
        }

        return "$firstPart $total $message"
    }

    /**
     * Load picture from given uri and save it to viewModel scope
     */
    fun setPickedRecipeImage(uri: Uri, width: Int, height: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            _editProfileImage.postValue(
                imageRepository.getImageFromUri(uri, width, height)
            )
        }
    }

}
