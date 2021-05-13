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
    var category: RecipeCategory
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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Recipe

        if (name != other.name) return false
        if (servings != other.servings) return false
        if (category != other.category) return false
        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + servings
        result = 31 * result + category.hashCode()
        result = 31 * result + id.hashCode()
        return result
    }

}
