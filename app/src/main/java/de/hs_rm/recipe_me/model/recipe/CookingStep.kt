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
    var imageUri: String,
    var text: String,
    var time: Int,
    var timeUnit: TimeUnit
) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0

    constructor(text: String, time: Int, timeUnit: TimeUnit) : this(
        -1,
        "",
        text,
        time,
        timeUnit
    )

    companion object {
        @Ignore
        const val DEFAULT_TIME = 0
    }

}
