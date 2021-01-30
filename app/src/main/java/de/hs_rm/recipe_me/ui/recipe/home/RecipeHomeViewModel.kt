package de.hs_rm.recipe_me.ui.recipe.home

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.hs_rm.recipe_me.model.recipe.Recipe
import de.hs_rm.recipe_me.service.RecipeOfTheDayRepository
import de.hs_rm.recipe_me.service.RecipeRepository
import kotlinx.coroutines.launch

/**
 * ViewModel for [RecipeHomeFragment]
 */
class RecipeHomeViewModel @ViewModelInject constructor(
    private val rotdRepository: RecipeOfTheDayRepository,
    private val recipeRepository: RecipeRepository
) : ViewModel() {

    private val _recipeOfTheDay = MutableLiveData<Recipe>()
    val recipeOfTheDay: LiveData<Recipe>
        get() = _recipeOfTheDay

    /**
     * Load recipe of the day to LiveData
     */
    fun loadRecipeOfTheDay() {
        viewModelScope.launch {
            rotdRepository.updateRecipeOfTheDay()
            val rotdId = rotdRepository.getRecipeOfTheDayId()
            _recipeOfTheDay.value = recipeRepository.getRecipeById(rotdId)
        }
    }

}
