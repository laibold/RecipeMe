package de.hs_rm.recipe_me.model.recipe

import org.junit.Assert.*
import org.junit.Test

class RecipeTest {

    /**
     * Test default values for id, name, servings and category
     */
    @Test
    fun testDefaultValues() {
        val recipe = Recipe()

        assertEquals(Recipe.DEFAULT_ID, recipe.id)
        assertEquals("", recipe.name)
        assertEquals(0, recipe.servings)
        assertEquals(RecipeCategory.values()[0], recipe.category)
    }
}
