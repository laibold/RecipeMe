package de.hs_rm.recipe_me

import de.hs_rm.recipe_me.model.recipe.*
import de.hs_rm.recipe_me.model.shopping_list.ShoppingListItem
import java.math.RoundingMode
import kotlin.math.round
import kotlin.random.Random

object TestDataProvider {

    fun getRandomRecipe(): Recipe {
        val name = getRandomString(10)
        val servings = Random.nextInt(1, 10)
        val category = getRandomRecipeCategory()

        return Recipe(name, servings, category)
    }

    fun getRandomIngredient(recipeId: Long = 0L, minQuantity: Double = 0.0): Ingredient {
        val name = getRandomString(5)
        val quantity = getRandomDouble(minQuantity, 100.0)
        val unit = getRandomIngredientUnit()

        return Ingredient(recipeId, name, quantity, unit)
    }

    fun getRandomCookingStep(recipeId: Long): CookingStep {
        val description = getRandomString(20)
        val time = getRandomInt(1, 60)
        val unit = getRandomTimeUnit()

        return CookingStep(recipeId, description, time, unit)
    }

    fun getRandomShoppingListItem(): ShoppingListItem {
        val name = getRandomString(10)
        val quantity = getRandomDouble(maxValue = 100.0)
        val ingredientUnit = getRandomIngredientUnit()

        return ShoppingListItem(name, quantity, ingredientUnit)
    }

    fun getRandomString(length: Int): String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }

    fun getRandomDouble(minValue: Double = 0.0, maxValue: Double): Double {
        return Random.nextDouble(minValue, maxValue).round(2)
    }

    private fun getRandomInt(minValue: Int, maxValue: Int): Int {
        return Random.nextInt(minValue, maxValue)
    }

    private fun getRandomRecipeCategory(): RecipeCategory {
        val categoryIndex = Random.nextInt(0, RecipeCategory.values().size)
        return RecipeCategory.values()[categoryIndex]
    }

    private fun getRandomTimeUnit(): TimeUnit {
        val unitIndex = Random.nextInt(0, TimeUnit.values().size)
        return TimeUnit.values()[unitIndex]
    }

    private fun getRandomIngredientUnit(): IngredientUnit {
        val unitIndex = Random.nextInt(0, IngredientUnit.values().size)
        return IngredientUnit.values()[unitIndex]
    }

    fun Double.round(decimals: Int): Double {
        var multiplier = 1.0
        repeat(decimals) { multiplier *= 10 }
        return round(this * multiplier) / multiplier
    }

}