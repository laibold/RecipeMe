package de.hs_rm.recipe_me.ui.recipe.detail

import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableInt
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.hs_rm.recipe_me.model.relation.RecipeWithRelations
import de.hs_rm.recipe_me.service.repository.RecipeImageRepository
import de.hs_rm.recipe_me.service.repository.RecipeRepository
import de.hs_rm.recipe_me.service.repository.ShoppingListRepository
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

/**
 * Shared ViewModel for [RecipeDetailFragment] and [CookingStepFragment]
 */
@HiltViewModel
class RecipeDetailViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository,
    private val imageRepository: RecipeImageRepository,
    internal var shoppingListRepository: ShoppingListRepository
) : ViewModel() {

    var recipe: LiveData<RecipeWithRelations> = MutableLiveData()
    var servings = ObservableInt(NOT_INITIALIZED)
    var ingredientSelectionActive = ObservableBoolean(false)

    /**
     * Get [RecipeWithRelations] from repository and save it to ViewModel
     */
    fun loadRecipe(id: Long) {
        recipe = recipeRepository.getRecipeWithRelationsById(id)
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
     * Add selected ingredients to shopping list
     */
    fun addSelectedIngredientsToShoppingList() {
        recipe.value?.let {
            for (ingredient in it.ingredients) {
                if (ingredient.checked) {
                    viewModelScope.launch {
                        shoppingListRepository.addOrUpdateFromIngredient(
                            ingredient.copy(),
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

    /**
     * Returns file of recipeImage that can be loaded into view via Glide.
     * If no image is available, null will be returned
     */
    fun getRecipeImageFile(recipeId: Long): File? {
        return imageRepository.getRecipeImageFile(recipeId)
    }

    companion object {
        const val NOT_INITIALIZED = -1
    }

}
