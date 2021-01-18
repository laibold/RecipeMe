package de.hs_rm.recipe_me.ui.recipe.detail

import androidx.databinding.ObservableInt
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.hs_rm.recipe_me.model.recipe.Recipe
import de.hs_rm.recipe_me.model.relation.RecipeWithRelations
import de.hs_rm.recipe_me.service.RecipeRepository

/**
 * Shared ViewModel for [RecipeDetailFragment] and [CookingStepFragment]
 */
class RecipeDetailViewModel @ViewModelInject constructor(
    private val repository: RecipeRepository
) : ViewModel() {

    lateinit var recipe: LiveData<RecipeWithRelations>
    var servings = ObservableInt()

    /**
     * Get [RecipeWithRelations] from repository and save it to ViewModel
     */
    fun loadRecipe(id: Long) {
        recipe = repository.getRecipeById(id)
    }

    /**
     * Set recipe to empty instance of [RecipeWithRelations]
     */
    fun clearRecipe() {
        recipe.value?.recipe = Recipe()
        recipe.value?.ingredients = listOf()
        recipe.value?.cookingSteps = listOf()
    }

    /**
     * Increase servings by 1
     */
    fun increaseServings() {
        servings.set(servings.get() + 1)
    }

    /**
     * Decrease servings by 1
     */
    fun decreaseServings() {
        if (servings.get() > 1) {
            servings.set(servings.get() - 1)
        }
    }

    /**
     * @return Multiplier for ingredients to calculate ingredient quantity based on selected servings
     */
    fun getServingsMultiplier(): Double {
        return servings.get().toDouble() / recipe.value!!.recipe.servings.toDouble()
    }

}
