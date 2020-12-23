package de.hs_rm.recipe_me.model.recipe

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Step for cooking with text, optional image and time in seconds.  Set [recipeId] to id of belonging [Recipe]
 */
@Entity
data class CookingStep(
    val recipeId: Long,
    var imageResId: Int,
    var text: String,
    var seconds: Int
) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
}
