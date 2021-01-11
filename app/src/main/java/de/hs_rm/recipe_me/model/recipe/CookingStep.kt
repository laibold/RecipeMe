package de.hs_rm.recipe_me.model.recipe

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Step for cooking with text, optional image and time in seconds.  Set [recipeId] to id of belonging [Recipe]
 */
@Entity
data class CookingStep(
    val recipeId: Long,
    var imageUri: String,
    var text: String,
    var seconds: Int
) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0

    constructor(text: String, time: Int, timeUnit: TimeUnit) : this(
        -1,
        "",
        text,
        timeAndUnitToSeconds(time, timeUnit)
    )

    companion object {
        private fun timeAndUnitToSeconds(time: Int, timeUnit: TimeUnit): Int {
            return when (timeUnit) {
                TimeUnit.SECOND -> time
                TimeUnit.MINUTE -> time * 60
                TimeUnit.HOUR -> time * 3600
                else -> 0
            }
        }
    }

}
