package de.hs_rm.recipe_me.service

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import de.hs_rm.recipe_me.model.recipe.Ingredient
import de.hs_rm.recipe_me.model.recipe.IngredientUnit
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

@RunWith(AndroidJUnit4::class)
class FormatterTest {

    lateinit var context: Context

    @Before
    fun initContext() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
    }

    @Test
    fun formatIngredientWithoutQuantityAndUnit() {
        val ingredient = Ingredient("Curcuma", 0.0, IngredientUnit.NONE)
        val str = Formatter.formatIngredient(context, ingredient)
        assertEquals(str, "Curcuma")
    }

    @Test
    fun formatIngredientWithoutUnit() {
        val ingredient = Ingredient("Bananas", 3.0, IngredientUnit.NONE)
        val str = Formatter.formatIngredient(context, ingredient)
        assertEquals(str, "3  Bananas")
    }

    @Test
    fun formatIngredientWithQuantityAndUnit() {
        val ingredient = Ingredient("Chickpeas", 1.0, IngredientUnit.CAN)
        val str = Formatter.formatIngredient(context, ingredient)
        assertEquals(str, "1 Dose  Chickpeas")
    }

    @Test
    fun formatIngredientWithQuantityAndUnitAndMultiplier() {
        val ingredient = Ingredient("Chickpeas", 1.5, IngredientUnit.CAN)
        val str = Formatter.formatIngredient(context, ingredient, 3.0)
        assertEquals(str, "4,5 Dosen  Chickpeas")
    }

}
