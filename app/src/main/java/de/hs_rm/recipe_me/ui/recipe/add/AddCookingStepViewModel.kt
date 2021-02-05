package de.hs_rm.recipe_me.ui.recipe.add

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.hs_rm.recipe_me.declaration.notifyObservers
import de.hs_rm.recipe_me.model.recipe.Ingredient

/**
 * Holds a List of the ingredients that have been assigned to the cooking step.
 * Important: Never reassign the ingredient list, just add or remove items or use clear()
 */
class AddCookingStepViewModel : ViewModel() {

    val assignedIngredients = MutableLiveData(mutableListOf<Ingredient>())

    /**
     * Clear List of assigned Ingredients and notify observers
     */
    fun resetCheckedStates() {
        assignedIngredients.value!!.clear()
        assignedIngredients.notifyObservers()
    }
}
