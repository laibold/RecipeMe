package de.hs_rm.recipe_me.ui.recipe.add

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.hs_rm.recipe_me.model.recipe.Ingredient

class AddCookingStepViewModel constructor(
    allIngredients: MutableList<Ingredient>
) : ViewModel() {

//    lateinit var cookingStepWithIngredients: CookingStepWithIngredients

    // Create deep copy of ingredients from ViewModel Scope
    val ingredients = MutableLiveData(allIngredients.map { it.clone() })

}
