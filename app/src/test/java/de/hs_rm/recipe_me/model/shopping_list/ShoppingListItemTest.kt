package de.hs_rm.recipe_me.model.shopping_list

import de.hs_rm.recipe_me.model.recipe.Ingredient
import de.hs_rm.recipe_me.model.recipe.IngredientUnit
import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test

class ShoppingListItemTest {

    @Test
    fun testAddIngredient() {
        val name = "Coconut"
        val quantity = 3.0
        val unit = IngredientUnit.GRAM

        val item = ShoppingListItem(name, quantity, unit)
        val ingredient = Ingredient(name, quantity, unit)
        item.addIngredient(ingredient)

        assertThat(item.name).isEqualTo(name)
        assertThat(item.quantity).isEqualTo(6.0)
        assertThat(item.unit).isEqualTo(unit)
    }

    @Test
    fun testAddingFailsByName() {
        val name = "Tomato"
        val wrongName = "Ginger"
        val quantity = 3.0
        val unit = IngredientUnit.GRAM

        val item = ShoppingListItem(name, quantity, unit)
        val ingredient = Ingredient(wrongName, quantity, unit)

        assertThrows(MismatchingIngredientException::class.java) {
            item.addIngredient(ingredient)
        }
    }

    @Test
    fun testAddingFailsByUnit() {
        val name = "Tomato"
        val quantity = 3.0
        val unit = IngredientUnit.GRAM
        val wrongUnit = IngredientUnit.CAN

        val item = ShoppingListItem(name, quantity, unit)
        val ingredient = Ingredient(name, quantity, wrongUnit)

        assertThrows(MismatchingIngredientException::class.java) {
            item.addIngredient(ingredient)
        }
    }
}
