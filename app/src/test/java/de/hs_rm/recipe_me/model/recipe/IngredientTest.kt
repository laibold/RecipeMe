package de.hs_rm.recipe_me.model.recipe

import org.junit.Assert.*
import org.junit.Test

class IngredientTest {

    /**
     * Test default values for id and checked
     */
    @Test
    fun testDefaultValues() {
        val ingredient = Ingredient("name", 1.0, IngredientUnit.NONE)

        assertEquals(Ingredient.DEFAULT_ID, ingredient.ingredientId)
        assertEquals(false, ingredient.checked)
    }

    /**
     * Test that equals() compares only name, quantity and unit
     */
    @Test
    fun testEquals() {
        val equalsId = 1L
        val equalsName = "equalsName"
        val equalsQuantity = 2.0
        val equalsUnit = IngredientUnit.STICK

        val differentId = 2L
        val differentName = "differentName"
        val differentQuantity = 31.0
        val differentUnit = IngredientUnit.NONE

        val original = Ingredient(equalsId, equalsName, equalsQuantity, equalsUnit)
        val equals = Ingredient(differentId, equalsName, equalsQuantity, equalsUnit)

        val different1 = Ingredient(equalsId, differentName, equalsQuantity, equalsUnit)
        val different2 = Ingredient(equalsId, equalsName, differentQuantity, equalsUnit)
        val different3 = Ingredient(equalsId, equalsName, equalsQuantity, differentUnit)

        assertEquals(original, original)
        assertEquals(original, equals)

        assertNotEquals(original, different1)
        assertNotEquals(original, different2)
        assertNotEquals(original, different3)
    }
}