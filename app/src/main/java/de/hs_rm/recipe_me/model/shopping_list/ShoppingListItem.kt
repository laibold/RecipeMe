package de.hs_rm.recipe_me.model.shopping_list

import android.content.Context
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import de.hs_rm.recipe_me.model.recipe.Ingredient
import de.hs_rm.recipe_me.model.recipe.IngredientUnit
import de.hs_rm.recipe_me.service.Formatter

/**
 * Item for shopping list. Contains a list of [Ingredient]s with the same name and unit.
 * Those can be summarized to a single string by calling format().
 */
@Entity
class ShoppingListItem(
    var name: String = "",
    var quantity: Double = Ingredient.DEFAULT_QUANTITY,
    var unit: IngredientUnit = IngredientUnit.NONE
) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
    var checked: Boolean = false

    constructor(ingredient: Ingredient) : this(
        ingredient.name,
        ingredient.quantity,
        ingredient.unit
    )

    @Throws(MismatchingIngredientException::class)
    fun addIngredient(ingredient: Ingredient) {
        if (ingredient.name != this.name && ingredient.unit != this.unit) {
            this.quantity += ingredient.quantity
        } else {
            throw MismatchingIngredientException()
        }
    }
}
