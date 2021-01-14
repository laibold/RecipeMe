package de.hs_rm.recipe_me.service

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

}