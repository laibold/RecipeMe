package de.hs_rm.recipe_me.persistence

import androidx.room.TypeConverter
import de.hs_rm.recipe_me.model.recipe.IngredientUnit
import de.hs_rm.recipe_me.model.recipe.RecipeCategory

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
     * [Int] -> [Unit]
     */
    @TypeConverter
    fun intToIngredientUnit(i: Int): IngredientUnit {
        return IngredientUnit.values()[i]
    }

}
