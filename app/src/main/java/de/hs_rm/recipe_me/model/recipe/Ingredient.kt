package de.hs_rm.recipe_me.model.recipe

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Ingredient for recipe with name, quantity and unit. Set [recipeId] to id of belonging [Recipe]
 */
@Entity
data class Ingredient(
    val recipeId: Long,
    var name: String,
    var quantity: Double,
    var unit: IngredientUnit
) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
}
