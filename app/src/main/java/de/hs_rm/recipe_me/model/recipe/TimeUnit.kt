package de.hs_rm.recipe_me.model.recipe

import android.content.res.Resources
import de.hs_rm.recipe_me.R

/**
 * Time units used for describing [CookingStep]s
 */
enum class TimeUnit(private val singularResId: Int, private val pluralResId: Int) {

    SECOND(R.string.second_sg, R.string.second_pl),
    MINUTE(R.string.minute_sg, R.string.minute_pl),
    HOUR(R.string.hour_sg, R.string.hour_pl);

    companion object {
        fun getNumberStringList(resources: Resources, amount: Int?): List<String> {
            return values().map { it.getNumberString(resources, amount) }
        }
    }

    fun getNumberString(
        resources: Resources,
        amount: Int?
    ): String {
        return if (amount == null || amount == 1) {
            resources.getString(singularResId)
        } else {
            resources.getString(pluralResId)
        }
    }
}
