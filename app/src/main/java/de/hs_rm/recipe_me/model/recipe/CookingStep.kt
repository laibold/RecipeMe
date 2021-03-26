package de.hs_rm.recipe_me.model.recipe

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

/**
 * Step for cooking with text, optional image and time in seconds.  Set [recipeId] to id of belonging [Recipe]
 */
@Entity
data class CookingStep(
    var recipeId: Long,
    var text: String,
    var time: Int,
    var timeUnit: TimeUnit
) {
    @PrimaryKey(autoGenerate = true)
    var cookingStepId: Long = DEFAULT_ID

    constructor(text: String, time: Int, timeUnit: TimeUnit) : this(
        -1,
        text,
        time,
        timeUnit
    )

    /**
     * Returns a given time in seconds
     */
    fun getTimeInSeconds(): Int {
        return when (timeUnit) {
            TimeUnit.SECOND -> time
            TimeUnit.MINUTE -> time * 60
            TimeUnit.HOUR -> time * 3600
        }
    }

    /**
     * Return if text, time and timeUnit are equal
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CookingStep

        if (text != other.text) return false
        if (time != other.time) return false
        if (timeUnit != other.timeUnit) return false

        return true
    }

    override fun hashCode(): Int {
        var result = text.hashCode()
        result = 31 * result + time
        result = 31 * result + timeUnit.hashCode()
        return result
    }

    companion object {
        /** id a CookingStep has by default (when it has not been persisted) */
        @Ignore
        const val DEFAULT_ID = 0L

        @Ignore
        const val DEFAULT_TIME = 0
    }
}
