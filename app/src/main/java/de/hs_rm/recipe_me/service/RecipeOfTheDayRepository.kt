package de.hs_rm.recipe_me.service

import androidx.lifecycle.LiveData
import de.hs_rm.recipe_me.model.RecipeOfTheDay
import de.hs_rm.recipe_me.model.recipe.Recipe
import de.hs_rm.recipe_me.persistence.RecipeDao
import de.hs_rm.recipe_me.persistence.RecipeOfTheDayDao
import java.time.LocalDate
import javax.inject.Inject
import kotlin.random.Random

/**
 * Single Source of Truth for [RecipeOfTheDay]. Use it with Dependency Injection
 */
class RecipeOfTheDayRepository @Inject constructor(
    private val rotdDao: RecipeOfTheDayDao,
    private val recipeDao: RecipeDao
) {

    /**
     * Returns LiveDate with [RecipeOfTheDay]. Change of the recipe is depending on following scenarios:
     * - there is no recipe of the day -> create one, except there is no recipe in database
     * - the current rotd is at least from yesterday and multiple recipes in database -> switch it
     * - the current rotd is at least from yesterday, but the only one in database -> don't switch
     * - the current rotd is from today -> don't switch
     *
     * if there's no change in the rotd, the date of the database object won't get updated.
     */
    suspend fun getRecipeOfTheDay(): LiveData<Recipe> {
        var currentRotd = rotdDao.getRecipeOfTheDay()
        val numberOfRecipes = recipeDao.getRecipeCount()

        when {
            currentRotd == null -> {
                // no rotd existing, insert it
                val recipe = generateRecipeOfTheDay()
                currentRotd = RecipeOfTheDay(LocalDate.now(), recipe.id)
                rotdDao.insert(currentRotd)
            }
            rotdInvalid(currentRotd) and (numberOfRecipes > 1) -> {
                // rotd existing, not valid anymore and more than 1 recipes available -> rotd should change
                var newRotd = generateRecipeOfTheDay()
                while (currentRotd.recipeId == newRotd.id) {
                    newRotd = generateRecipeOfTheDay()
                }
                currentRotd.date = LocalDate.now()
                currentRotd.recipeId = newRotd.id
                rotdDao.update(currentRotd)
            }
            // else rotd existing and still valid or just one recipe available -> no change
        }

        return recipeDao.getRecipeById(currentRotd!!.recipeId)
    }
    /**
     * @return True if date of given [RecipeOfTheDay] if from the day before today or earlier
     */
    fun rotdInvalid(rotd: RecipeOfTheDay): Boolean {
        return rotd.date.isBefore(LocalDate.now())
    }

    /**
     * Gets random Recipe from RecipeDao
     */
    private fun generateRecipeOfTheDay(): Recipe {
        val numberOfRecipes = recipeDao.getRecipeCount()

        if (numberOfRecipes == 0) {
            return Recipe()
        }

        val offset = Random.nextInt(0, numberOfRecipes)
        return recipeDao.getRecipeByOffset(offset)
    }

}
