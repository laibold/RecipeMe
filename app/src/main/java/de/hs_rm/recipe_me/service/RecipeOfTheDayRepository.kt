package de.hs_rm.recipe_me.service

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import de.hs_rm.recipe_me.model.RecipeOfTheDay
import de.hs_rm.recipe_me.model.recipe.Recipe
import de.hs_rm.recipe_me.persistence.RecipeDao
import de.hs_rm.recipe_me.persistence.RecipeOfTheDayDao
import javax.inject.Inject
import kotlin.random.Random

/**
 * Single Source of Truth for [RecipeOfTheDay]. Use it with Dependency Injection
 */
class RecipeOfTheDayRepository @Inject constructor(
    private val rotdDao: RecipeOfTheDayDao,
    private val recipeDao: RecipeDao
) {

    suspend fun getRecipeOfTheDay(): LiveData<Recipe> {
        val rotd = rotdDao.getRecipeOfTheDay()

        val numberOfRecipes = recipeDao.getRecipeCount()

        when {
            rotd == null -> {
                // no rotd existing
                return MutableLiveData(generateRecipeOfTheDay())
            }
            numberOfRecipes == 1 -> {
                // rotd existing, but only one recipe available
                return recipeDao.getRecipeById(rotd.recipeId)
            }
            else -> {
                // rotd existing and more recipes available -> rotd should change
                var newRotd = generateRecipeOfTheDay()
                while (rotd.recipeId == newRotd.id) {
                    newRotd = generateRecipeOfTheDay()
                }
                return MutableLiveData(newRotd)
            }
        }

    }

    private fun generateRecipeOfTheDay(): Recipe {
        val numberOfRecipes = recipeDao.getRecipeCount()

        if (numberOfRecipes == 0) {
            return Recipe()
        }

        val offset = Random.nextInt(0, numberOfRecipes)
        return recipeDao.getRecipeByOffset(offset)
    }

}
