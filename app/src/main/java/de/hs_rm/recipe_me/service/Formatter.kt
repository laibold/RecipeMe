package de.hs_rm.recipe_me.service

import android.content.Context
import de.hs_rm.recipe_me.model.recipe.Ingredient
import de.hs_rm.recipe_me.model.recipe.IngredientUnit
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

/**
 * Creates formatted Strings
 */
object Formatter {

    /**
     * Formats Double to comma or dot-separated (depending on locale) String without redundant decimals
     * (eg 5.5 -> 5,5 / 5.0 -> 5)
     */
    fun formatIngredientQuantity(quantity: Double): String {
        val separator = DecimalFormatSymbols(Locale.getDefault()).decimalSeparator
        return DecimalFormat("#.##").format(quantity).replace(".", separator.toString())
    }

    /**
     * Format Ingredient to String with Quantity and Unit
     */
    fun formatIngredient(
        context: Context,
        ingredient: Ingredient,
        multiplier: Double = 1.0
    ): String {
        return formatIngredientValues(
            context,
            ingredient.name,
            ingredient.quantity,
            ingredient.unit,
            multiplier
        )
    }

    /**
     * Format values of Ingredient to String with Quantity and Unit
     */
    fun formatIngredientValues(
        context: Context,
        name: String,
        quantity: Double,
        unit: IngredientUnit,
        multiplier: Double = 1.0
    ): String {
        val calculatedQuantity = quantity * multiplier
        var unitText = ""

        if (calculatedQuantity > Ingredient.DEFAULT_QUANTITY) {
            // quantity existing, there will be no ingredient with 0.0 (Unit)
            val quantityStr = formatIngredientQuantity(calculatedQuantity)

            var unitStr = ""
            if (unit != IngredientUnit.NONE) {
                unitStr = unit.getNumberString(context.resources, calculatedQuantity) + " "
            }

            unitText = "$quantityStr $unitStr"
        }

        return unitText + name
    }

    /**
     * Format List of ingredients to comma separated String
     */
    fun formatIngredientList(context: Context, ingredients: List<Ingredient>, multiplier: Double = 1.0): String {
        val stringList = ingredients.map { formatIngredient(context, it, multiplier) }

        return stringList.joinToString(", ")
    }

    /**
     * Add 's' or apostrophe to end of name
     */
    fun formatNameToGenitive(name: String): String {
        if (name.endsWith("s", true) ||
            name.endsWith("x", true) ||
            name.endsWith("ß", true)) {
            return "$name'"
        }
        return "${name}s"
    }

}
