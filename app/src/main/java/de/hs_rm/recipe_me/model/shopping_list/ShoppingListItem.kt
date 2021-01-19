package de.hs_rm.recipe_me.model.shopping_list

import android.content.Context
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import de.hs_rm.recipe_me.model.recipe.Ingredient
import de.hs_rm.recipe_me.service.Formatter

/**
 * Item for shopping list. Contains a list of [Ingredient]s with the same name and unit.
 * Those can be summarized to a single string by calling format().
 */
@Entity
class ShoppingListItem(
    var ingredients: MutableList<Ingredient>
) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
    var checked: Boolean = false

    fun format(context: Context): String {
        val name = ingredients[0].name
        val unit = ingredients[0].unit
        var quantitySum = 0.0
        for (ingredient in ingredients) {
            quantitySum += ingredient.quantity
        }

        return Formatter.formatIngredient(context, Ingredient(name, quantitySum, unit))
    }
}
