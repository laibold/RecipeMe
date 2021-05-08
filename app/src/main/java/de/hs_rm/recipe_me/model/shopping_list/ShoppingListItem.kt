package de.hs_rm.recipe_me.model.shopping_list

import android.content.Context
import androidx.room.Entity
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
        if (ingredient.name == this.name && ingredient.unit == this.unit) {
            this.quantity += ingredient.quantity
        } else {
            throw MismatchingIngredientException()
        }
    }

    fun format(context: Context): String {
        return Formatter.formatIngredientValues(context, name, quantity, unit)
    }

    fun toggleChecked() {
        checked = !checked
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ShoppingListItem

        if (name != other.name) return false
        if (quantity != other.quantity) return false
        if (unit != other.unit) return false
        if (id != other.id) return false
        if (checked != other.checked) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + quantity.hashCode()
        result = 31 * result + unit.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + checked.hashCode()
        return result
    }

}
