package de.hs_rm.recipe_me.model.recipe

import de.hs_rm.recipe_me.R

/**
 * Time units used for describing [CookingStep]s
 */
enum class TimeUnit(val singularResId: Int, val pluralResId: Int) : NumberModel {

    NONE(R.string.none_time, R.string.none_time),
    SECOND(R.string.second_sg, R.string.second_pl),
    MINUTE(R.string.minute_sg, R.string.minute_pl),
    HOUR(R.string.hour_sg, R.string.hour_pl);

    override fun getSingularId(): Int {
        return singularResId
    }

    override fun getPluralId(): Int {
        return pluralResId
    }
}
