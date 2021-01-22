package de.hs_rm.recipe_me.service

import android.content.Context
import de.hs_rm.recipe_me.model.recipe.Ingredient
import de.hs_rm.recipe_me.model.recipe.IngredientUnit
import java.text.DecimalFormat

/**
 * Creates formatted Strings
 */
object Formatter {

    /**
     * Formats Double to comma-separated String without redundant decimals
     * (eg 5.5 -> 5,5 / 5.0 -> 5)
     */
    fun formatIngredientQuantity(quantity: Double): String {
        return DecimalFormat("#.##").format(quantity).replace(".", ",")
    }

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

            var unitStr = " "
            if (unit != IngredientUnit.NONE) {
                unitStr = unit.getNumberString(context.resources, calculatedQuantity) + "  "
            }

            unitText = "$quantityStr $unitStr"
        }

        return unitText + name
    }

}
