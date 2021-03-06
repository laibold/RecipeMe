package de.hs_rm.recipe_me

import de.hs_rm.recipe_me.model.recipe.*
import de.hs_rm.recipe_me.model.shopping_list.ShoppingListItem
import kotlin.random.Random

object TestDataProvider {

    fun getRandomRecipe(): Recipe {
        val name = getRandomString(10)
        val servings = Random.nextInt(1, 10)
        val category = getRandomRecipeCategory()

        return Recipe(name, servings, category)
    }

    fun getRandomIngredient(recipeId: Long): Ingredient {
        val name = getRandomString(10)
        val quantity = getRandomDouble(100.0)
        val unit = getRandomIngredientUnit()

        return Ingredient(recipeId, name, quantity, unit)
    }

    fun getRandomCookingStep(recipeId: Long): CookingStep {
        val description = getRandomString(20)
        val time = getRandomInt(60)
        val unit = getRandomTimeUnit()

        return CookingStep(recipeId, description, time, unit)
    }

    fun getRandomShoppingListItem(): ShoppingListItem {
        val name = getRandomString(10)
        val quantity = getRandomDouble(100.0)
        val ingredientUnit = getRandomIngredientUnit()

        return ShoppingListItem(name, quantity, ingredientUnit)
    }

    private fun getRandomString(length: Int): String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }

    private fun getRandomDouble(maxValue: Double): Double {
        return Random.nextDouble(0.0, maxValue)
    }

    private fun getRandomInt(maxValue: Int): Int {
        return Random.nextInt(maxValue)
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

}