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
    var imageUri: String,
) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0

    constructor(recipeCategory: RecipeCategory) : this(
        "",
        2,
        recipeCategory,
        ""
    )
}
