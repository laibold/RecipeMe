package de.hs_rm.recipe_me.model.recipe

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

/**
 * Ingredient for recipe with name, quantity and unit. Set [recipeId] to id of belonging [Recipe]
 */
@Entity
data class Ingredient(
    var recipeId: Long,
    var name: String,
    var quantity: Double,
    var unit: IngredientUnit
) {
    @PrimaryKey(autoGenerate = true)
    var ingredientId: Long = 0

    @Ignore
    var checked = false

    constructor(name: String, quantity: Double, unit: IngredientUnit) : this(
        -1,
        name,
        quantity,
        unit
    )

    companion object {
        @Ignore
        const val DEFAULT_QUANTITY = 0.0
    }

}
