package de.hs_rm.recipe_me.model.shopping_list

import de.hs_rm.recipe_me.model.recipe.Ingredient
import de.hs_rm.recipe_me.model.recipe.IngredientUnit
import org.junit.Assert.*
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

        assertEquals(item.name, name)
        assertEquals(item.quantity, 6.0, 0.0)
        assertEquals(item.unit, unit)
    }

    @Test(expected = MismatchingIngredientException::class)
    fun testAddingFailsByName() {
        val name = "Tomato"
        val wrongName = "Ginger"
        val quantity = 3.0
        val unit = IngredientUnit.GRAM

        val item = ShoppingListItem(name, quantity, unit)
        val ingredient = Ingredient(wrongName, quantity, unit)
        item.addIngredient(ingredient)
    }

    @Test(expected = MismatchingIngredientException::class)
    fun testAddingFailsByUnit() {
        val name = "Tomato"
        val quantity = 3.0
        val unit = IngredientUnit.GRAM
        val wrongUnit = IngredientUnit.CAN

        val item = ShoppingListItem(name, quantity, unit)
        val ingredient = Ingredient(name, quantity, wrongUnit)
        item.addIngredient(ingredient)
    }


}
