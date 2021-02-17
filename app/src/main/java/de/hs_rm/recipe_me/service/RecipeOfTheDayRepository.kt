package de.hs_rm.recipe_me.service

import de.hs_rm.recipe_me.model.RecipeOfTheDay
import de.hs_rm.recipe_me.model.recipe.Recipe
import de.hs_rm.recipe_me.persistence.RecipeDao
import de.hs_rm.recipe_me.persistence.RecipeOfTheDayDao
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

/**
 * Single Source of Truth for [RecipeOfTheDay]. Use it with Dependency Injection
 */
@Singleton
class RecipeOfTheDayRepository @Inject constructor(
    private val rotdDao: RecipeOfTheDayDao,
    private val recipeDao: RecipeDao
) {

    /**
     * Generates Recipe of the day. Change of this is depending on following scenarios:
     * - there is no recipe of the day -> create one, except there is no recipe in database
     * - the current rotd is at least from yesterday and multiple recipes in database -> switch it
     * - the current rotd is at least from yesterday, but the only one in database -> don't switch
     * - the current rotd is from today -> don't switch
     *
     * if there's no change in the rotd, the date of the database object won't get updated.
     */
    suspend fun updateRecipeOfTheDay() {
        val currentRecipeOtD = rotdDao.getRecipeOfTheDay()
        val numberOfRecipes = recipeDao.getRecipeCount()

        when {
            currentRecipeOtD == null -> {
                // no rotd existing, insert it
                if (numberOfRecipes > 0) {
                    val recipe = generateRecipeOfTheDay(numberOfRecipes)
                    rotdDao.insert(RecipeOfTheDay(LocalDate.now(), recipe.id))
                }
            }
            rotdInvalid(currentRecipeOtD) && (numberOfRecipes > 1) -> {
                // rotd existing, not valid anymore and more than 1 recipes available -> rotd should change
                var newRotd = generateRecipeOfTheDay(numberOfRecipes)
                while (currentRecipeOtD.recipeId == newRotd.id) {
                    newRotd = generateRecipeOfTheDay(numberOfRecipes)
                }
                currentRecipeOtD.date = LocalDate.now()
                currentRecipeOtD.recipeId = newRotd.id
                rotdDao.update(currentRecipeOtD)
            }
            // else rotd existing and still valid or just one recipe available -> no change
        }
    }

    /**
     * @return id of the [Recipe] which is currently recipe of the day, -1 when there's none
     */
    suspend fun getRecipeOfTheDayId(): Long {
        val rotd = rotdDao.getRecipeOfTheDay()
        return rotd?.recipeId ?: -1
    }

    /**
     * @return True if date of given [RecipeOfTheDay] if from the day before today or earlier
     */
    fun rotdInvalid(rotd: RecipeOfTheDay): Boolean {
        return rotd.date.isBefore(LocalDate.now())
    }

    /**
     * Gets random Recipe from RecipeDao
     * @return random recipe if available, otherwise null
     */
    private suspend fun generateRecipeOfTheDay(numberOfRecipes: Int): Recipe {
        val offset = Random.nextInt(0, numberOfRecipes)
        return recipeDao.getRecipeByOffset(offset)
    }

}
