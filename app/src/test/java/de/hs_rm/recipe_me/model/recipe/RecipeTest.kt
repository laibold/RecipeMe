package de.hs_rm.recipe_me.model.recipe

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class RecipeTest {

    /**
     * Test default values for id, name, servings and category
     */
    @Test
    fun testDefaultValues() {
        val recipe = Recipe()

        assertThat(recipe.id).isEqualTo(Recipe.DEFAULT_ID)
        assertThat(recipe.name).isEqualTo("")
        assertThat(recipe.servings).isEqualTo(0)
        assertThat(recipe.category).isEqualTo(RecipeCategory.values()[0])
    }
}
