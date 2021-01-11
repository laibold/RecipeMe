package de.hs_rm.recipe_me.persistence

import androidx.room.TypeConverter
import de.hs_rm.recipe_me.model.recipe.IngredientUnit
import de.hs_rm.recipe_me.model.recipe.RecipeCategory
import de.hs_rm.recipe_me.model.recipe.TimeUnit

/**
 * Converters for [AppDatabase]
 */
class Converters {

    /**
     * [RecipeCategory] -> [Int]
     */
    @TypeConverter
    fun recipeCategoryToInt(category: RecipeCategory): Int {
        return category.ordinal
    }

    /**
     * [Int] -> [RecipeCategory]
     */
    @TypeConverter
    fun intToRecipeCategory(i: Int): RecipeCategory {
        return RecipeCategory.values()[i]
    }

    /**
     * [IngredientUnit] -> [Int]
     */
    @TypeConverter
    fun ingredientUnitToInt(unit: IngredientUnit): Int {
        return unit.ordinal
    }

    /**
     * [Int] -> [IngredientUnit]
     */
    @TypeConverter
    fun intToIngredientUnit(i: Int): IngredientUnit {
        return IngredientUnit.values()[i]
    }

    /**
     * [TimeUnit] -> [Int]
     */
    @TypeConverter
    fun timeUnitToInt(unit: TimeUnit): Int {
        return unit.ordinal
    }

    /**
     * [Int] -> [TimeUnit]
     */
    @TypeConverter
    fun intToTimeUnit(i: Int): TimeUnit {
        return TimeUnit.values()[i]
    }

}
