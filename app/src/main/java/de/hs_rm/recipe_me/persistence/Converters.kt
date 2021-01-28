package de.hs_rm.recipe_me.persistence

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import de.hs_rm.recipe_me.model.recipe.Ingredient
import de.hs_rm.recipe_me.model.recipe.IngredientUnit
import de.hs_rm.recipe_me.model.recipe.RecipeCategory
import de.hs_rm.recipe_me.model.recipe.TimeUnit
import java.util.*

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

    /**
     * [Ingredient] -> [String]
     */
    @TypeConverter
    fun ingredientToString(ingredients: MutableList<Ingredient>): String {
        return Gson().toJson(ingredients)
    }

    /**
     * [Int] -> [TimeUnit]
     */
    @TypeConverter
    fun stringToIngredient(str: String): MutableList<Ingredient> {
        return Gson().fromJson(str, object : TypeToken<List<Ingredient>>() {}.type)
    }

    /**
     * [Date] -> [Long]
     */
    @TypeConverter
    fun dateToLong(date: Date): Long {
        return date.time
    }

    /**
     * [Long] -> [Date]
     */
    @TypeConverter
    fun longToDate(timestamp: Long): Date {
        return Date(timestamp)
    }

}
