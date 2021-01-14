package de.hs_rm.recipe_me.ui.recipe

import androidx.databinding.ObservableInt
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import de.hs_rm.recipe_me.model.relation.RecipeWithRelations
import de.hs_rm.recipe_me.service.RecipeRepository


class RecipeDetailViewModel @ViewModelInject constructor(
    private val repository: RecipeRepository
) : ViewModel() {

    lateinit var recipe: LiveData<RecipeWithRelations>
    var servings = ObservableInt()

    fun loadRecipe(id: Long) {
        recipe = repository.getRecipeById(id)
    }

    fun increaseServings() {
        servings.set(servings.get() + 1)
    }

    fun decreaseServings() {
        if (servings.get() > 1) {
            servings.set(servings.get() - 1)
        }
    }

    fun getServingsMultiplier(): Double {
        return servings.get().toDouble() / recipe.value!!.recipe.servings.toDouble()
    }


}