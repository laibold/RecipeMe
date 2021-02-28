package de.hs_rm.recipe_me.ui.recipe.add.cooking_step

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import de.hs_rm.recipe_me.declaration.notifyObservers
import de.hs_rm.recipe_me.model.recipe.Ingredient
import javax.inject.Inject

/**
 * Holds a List of the ingredients that have been assigned to the cooking step.
 * Important: Never reassign the ingredient list, just add or remove items or use clear()
 */
@HiltViewModel
class AddCookingStepViewModel @Inject constructor() : ViewModel() {

    private val _assignedIngredients = MutableLiveData(mutableListOf<Ingredient>())
    val assignedIngredients: LiveData<MutableList<Ingredient>>
        get() = _assignedIngredients

    /**
     * Clear List of assigned Ingredients and notify observers
     */
    fun resetCheckedStates() {
        _assignedIngredients.value!!.clear()
        _assignedIngredients.notifyObservers()
    }

    /**
     * Toggle checked state of assignedIngredients by adding or removing them to internal list
     */
    fun toggleCheckedState(ingredient: Ingredient) {
        if (ingredient in _assignedIngredients.value!!) {
            _assignedIngredients.value!!.remove(ingredient)
        } else {
            _assignedIngredients.value!!.add(ingredient)
        }
        _assignedIngredients.notifyObservers()
    }

    /**
     * Add already assigned Ingredients to internal list
     */
    fun addAssignedIngredients(ingredients: List<Ingredient>) {
        _assignedIngredients.value!!.addAll(ingredients)
        _assignedIngredients.notifyObservers()
    }
}
