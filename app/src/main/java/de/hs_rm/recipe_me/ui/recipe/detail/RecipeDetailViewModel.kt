package de.hs_rm.recipe_me.ui.recipe.detail

import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableInt
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.hs_rm.recipe_me.model.recipe.Recipe
import de.hs_rm.recipe_me.model.relation.RecipeWithRelations
import de.hs_rm.recipe_me.service.RecipeRepository
import de.hs_rm.recipe_me.service.ShoppingListRepository
import kotlinx.coroutines.launch

/**
 * Shared ViewModel for [RecipeDetailFragment] and [CookingStepFragment]
 */
class RecipeDetailViewModel @ViewModelInject constructor(
    private val recipeRepository: RecipeRepository,
    private val shoppingListRepository: ShoppingListRepository
) : ViewModel() {

    lateinit var recipe: LiveData<RecipeWithRelations>
    var servings = ObservableInt()
    var ingredientSelectionActive = ObservableBoolean(false)

    /**
     * Get [RecipeWithRelations] from repository and save it to ViewModel
     */
    fun loadRecipe(id: Long) {
        recipe = recipeRepository.getRecipeById(id)
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

    /**
     * Add selected ingredients to shopping lits
     */
    fun addSelectedIngredientsToShoppingList() {
        recipe.value?.let {
            for (ingredient in it.ingredients) {
                if (ingredient.checked) {
                    viewModelScope.launch {
                        shoppingListRepository.addOrUpdateFromIngredient(
                            ingredient,
                            getServingsMultiplier()
                        )
                    }
                }
            }
        }
    }

    /**
     * Set checked = false to every ingredient of current recipe
     */
    fun clearSelections() {
        recipe.value?.let {
            for (ingredient in it.ingredients) {
                ingredient.checked = false
            }
        }
    }

}
