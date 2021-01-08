package de.hs_rm.recipe_me.service

import android.content.res.Resources
import de.hs_rm.recipe_me.model.recipe.IngredientUnit
import de.hs_rm.recipe_me.model.recipe.NumberModel

class NumberResolver {

    companion object {
        inline fun <reified E : Enum<E>> getNumberResourceId(
            resources: Resources,
            amount: Int?
        ): List<String> {
            val values = enumValues<E>() as Array<NumberModel>

            return if (amount == null || amount != 1) {
                values.map { resources.getString(it.getPluralId()) }
            } else {
                values.map { resources.getString(it.getSingularId()) }
            }
        }

        inline fun <reified E : Enum<E>> getNumberResourceId(
            resources: Resources,
            amount: Double?
        ): List<String> {
            val values = enumValues<E>() as Array<NumberModel>

            return if (amount == null || amount == 1.0) {
                values.map { resources.getString(it.getSingularId()) }
            } else {
                values.map { resources.getString(it.getPluralId()) }
            }
        }


    }

}