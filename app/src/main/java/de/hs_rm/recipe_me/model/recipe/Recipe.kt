package de.hs_rm.recipe_me.model.recipe

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Recipe entity without relations
 */
@Entity
data class Recipe(
    var name: String,
    var servings: Int,
    var category: RecipeCategory,
) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = DEFAULT_ID

    constructor() : this(
        "",
        0,
        RecipeCategory.values()[0]
    )

    constructor(recipeCategory: RecipeCategory) : this(
        "",
        0,
        recipeCategory
    )

    companion object {
        /** id a Recipe has by default (when it has not been persisted) */
        const val DEFAULT_ID = 0L
    }
}
