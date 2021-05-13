package de.hs_rm.recipe_me.model.recipe

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class IngredientTest {

    /**
     * Test default values for id and checked
     */
    @Test
    fun testDefaultValues() {
        val ingredient = Ingredient("name", 1.0, IngredientUnit.NONE)

        assertThat(ingredient.ingredientId).isEqualTo(Ingredient.DEFAULT_ID)
        assertThat(ingredient.checked).isEqualTo(false)
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

        assertThat(original).isEqualTo(original)
        assertThat(equals).isEqualTo(original)

        assertThat(different1).isNotEqualTo(original)
        assertThat(different2).isNotEqualTo(original)
        assertThat(different3).isNotEqualTo(original)
    }
}
