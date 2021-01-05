package de.hs_rm.recipe_me.ui.recipe

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.hs_rm.recipe_me.service.RecipeRepository
import kotlinx.coroutines.launch

/**
 * ViewModel for [RecipeHomeFragment]
 */
class RecipeViewModel @ViewModelInject constructor(
    private val repository: RecipeRepository
) : ViewModel() {

    val recipes = repository.getRecipes()

    /**
     * Insert recipes into database
     */
    fun insertTestRecipes() {
        viewModelScope.launch {
            repository.insertTestRecipes()
        }
    }

    /**
     * Clear database
     */
    fun clear() {
        viewModelScope.launch {
            repository.clearRecipes()
        }
    }

}
