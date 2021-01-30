package de.hs_rm.recipe_me

import de.hs_rm.recipe_me.model.recipe.*
import kotlin.random.Random

object TestDataProvider {

    fun getRandomRecipe(): Recipe {
        val name = getRandomString(10)
        val servings = Random.nextInt(1, 10)
        val categoryIndex = Random.nextInt(0, RecipeCategory.values().size)
        val category = RecipeCategory.values()[categoryIndex]
        val imageUri = ""

        return Recipe(name, servings, category, imageUri)
    }

    fun getRandomIngredient(recipeId: Long): Ingredient {
        val name = getRandomString(10)
        val quantity = Random.nextDouble(0.0, 100.0)
        val unitIndex = Random.nextInt(0, IngredientUnit.values().size)
        val unit = IngredientUnit.values()[unitIndex]

        return Ingredient(recipeId, name, quantity, unit)
    }

    fun getRandomCookingStep(recipeId: Long): CookingStep {
        val imageUri = ""
        val description = getRandomString(20)
        val time = Random.nextInt(0, 60)
        val unitIndex = Random.nextInt(0, TimeUnit.values().size)
        val unit = TimeUnit.values()[unitIndex]

        return CookingStep(recipeId, imageUri, description, time, unit)
    }

    private fun getRandomString(length: Int): String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }

//    fun getRecipes(): List<Recipe> {
//        val r1 = Recipe("Recipe1", 1, RecipeCategory.SNACKS, "")
//        r1.id = 1
//        val r2 = Recipe("Recipe2", 2, RecipeCategory.SOUPS, "")
//        r2.id = 2
//        val r3 = Recipe("Recipe3", 3, RecipeCategory.BEVERAGES, "")
//        r3.id = 3
//        val r4 = Recipe("Recipe4", 4, RecipeCategory.MAIN_DISHES, "")
//        r4.id = 4
//
//        return listOf(r1, r2, r3, r4)
//    }

//    fun getIngredients(): List<Ingredient> {
//        val i1 = Ingredient(1, "Ingredient1_R1", 1.0, IngredientUnit.MILLILITER)
//        val i2 = Ingredient(1, "Ingredient2_R1", 1.0, IngredientUnit.GRAM)
//        val i3 = Ingredient(2, "Ingredient1_R2", 1.0, IngredientUnit.CENTIMETER)
//        val i4 = Ingredient(2, "Ingredient2_R2", 1.0, IngredientUnit.DASH)
//        val i5 = Ingredient(3, "Ingredient1_R3", 1.0, IngredientUnit.PACK)
//        val i6 = Ingredient(3, "Ingredient2_R3", 1.0, IngredientUnit.GRAM)
//        val i7 = Ingredient(4, "Ingredient1_R4", 1.0, IngredientUnit.CLOVE)
//        val i8 = Ingredient(4, "Ingredient2_R4", 1.0, IngredientUnit.PINCH)
//
//        return listOf(i1, i2, i3, i4, i5, i6, i7, i8)
//    }
//
//    fun getCookingSteps(): List<CookingStep> {
//        val s1 = CookingStep(1, "", "description", 1, TimeUnit.SECOND)
//        val s2 = CookingStep(1, "", "description", 1, TimeUnit.SECOND)
//        val s3 = CookingStep(2, "", "description", 1, TimeUnit.SECOND)
//        val s4 = CookingStep(2, "", "description", 1, TimeUnit.SECOND)
//        val s5 = CookingStep(3, "", "description", 1, TimeUnit.SECOND)
//        val s6 = CookingStep(3, "", "description", 1, TimeUnit.SECOND)
//        val s7 = CookingStep(4, "", "description", 1, TimeUnit.SECOND)
//        val s8 = CookingStep(4, "", "description", 1, TimeUnit.SECOND)
//
//        return listOf(s1, s2, s3, s4, s5, s6, s7, s8)
//    }

}