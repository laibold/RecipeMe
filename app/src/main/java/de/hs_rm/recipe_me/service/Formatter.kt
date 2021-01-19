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
        val calculatedQuantity = ingredient.quantity * multiplier
        var unitText = ""

        if (calculatedQuantity > Ingredient.DEFAULT_QUANTITY) {
            // quantity existing, there will be no ingredient with 0.0 (Unit)
            var quantity = ""
            var unit = ""

            quantity = formatIngredientQuantity(calculatedQuantity) + " "
            unit = ingredient.unit.getNumberString(context.resources, calculatedQuantity) + "  "

            unitText = "$quantity $unit  "
        }

        return unitText + ingredient.name
    }

}
