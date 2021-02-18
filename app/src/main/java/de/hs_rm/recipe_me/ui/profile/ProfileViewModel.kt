package de.hs_rm.recipe_me.ui.profile

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import de.hs_rm.recipe_me.model.user.User
import de.hs_rm.recipe_me.service.RecipeRepository
import de.hs_rm.recipe_me.service.UserRepository
import kotlinx.coroutines.launch

/**
 * ViewModel for [ProfileViewModel]
 */
class ProfileViewModel @ViewModelInject constructor(
    private val recipeRepository: RecipeRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    lateinit var total: LiveData<Int>
    lateinit var user: LiveData<User?>

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
    }

    /**
     * Insert or update user with given name in the repository
     */
    fun saveUser(name: String) {
        viewModelScope.launch {
            userRepository.insertOrUpdate(name)
        }
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

}
