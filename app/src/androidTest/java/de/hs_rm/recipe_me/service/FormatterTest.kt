package de.hs_rm.recipe_me.service

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import de.hs_rm.recipe_me.model.recipe.Ingredient
import de.hs_rm.recipe_me.model.recipe.IngredientUnit
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
class FormatterTest {

    private lateinit var context: Context

    @Before
    fun initContext() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
    }

    /**
     * Format plain ingredient
     */
    @Test
    fun formatIngredientWithoutQuantityAndUnit() {
        val ingredient = Ingredient("Curcuma", 0.0, IngredientUnit.NONE)
        val str = Formatter.formatIngredient(context, ingredient)
        assertEquals("Curcuma", str)
    }

    /**
     * Format ingredient with quantity
     */
    @Test
    fun formatIngredientWithoutUnit() {
        val ingredient = Ingredient("Bananas", 3.0, IngredientUnit.NONE)
        val str = Formatter.formatIngredient(context, ingredient)
        assertEquals("3 Bananas", str)
    }

    /**
     * Format ingredient with quantity and unit
     */
    @Test
    fun formatIngredientWithQuantityAndUnit() {
        setLocale(Locale.US)
        val ingredient = Ingredient("chickpeas", 1.0, IngredientUnit.CAN)
        val str = Formatter.formatIngredient(context, ingredient)
        assertEquals("1 can chickpeas", str)
    }

    /**
     * Format ingredient with quantity, unit and multiplier
     */
    @Test
    fun formatIngredientWithQuantityAndUnitAndMultiplier() {
        setLocale(Locale.US)
        val ingredient = Ingredient("Chickpeas", 1.5, IngredientUnit.CAN)
        val str = Formatter.formatIngredient(context, ingredient, 3.0)
        assertEquals("4.5 cans Chickpeas", str)
    }

    /**
     * Test formatting ingredient list with US locale (english strings and dot as separator)
     */
    @Test
    fun formatIngredientListWithMultipleItemsUS() {
        setLocale(Locale.US)
        val ingredients = listOf(
            Ingredient("Bananas", 3.0, IngredientUnit.NONE),
            Ingredient("Chickpeas", 1.5, IngredientUnit.CAN),
            Ingredient("Curcuma", 0.0, IngredientUnit.NONE)
        )
        val str = Formatter.formatIngredientList(context, ingredients)

        assertEquals("3 Bananas, 1.5 cans Chickpeas, Curcuma", str)
    }

    /**
     * Test formatting ingredient list with DE locale (english strings and dot as separator)
     */
    @Test
    fun formatIngredientListWithMultipleItemsDE() {
        setLocale(Locale.GERMANY)
        val ingredients = listOf(
            Ingredient("Bananen", 3.0, IngredientUnit.NONE),
            Ingredient("Kichererbsen", 1.5, IngredientUnit.CAN),
            Ingredient("Kurkuma", 2.5, IngredientUnit.GRAM)
        )
        val str = Formatter.formatIngredientList(context, ingredients)

        assertEquals("3 Bananen, 1,5 Dosen Kichererbsen, 2,5 g Kurkuma", str)
    }

    /**
     * Format ingredient list with just one item (no comma expected)
     */
    @Test
    fun formatIngredientListWithSingleItem() {
        val ingredients = listOf(
            Ingredient("Bananas", 3.0, IngredientUnit.GRAM)
        )
        val str = Formatter.formatIngredientList(context, ingredients)

        assertEquals("3 g Bananas", str)
    }

    /**
     * Set Locale to context
     */
    private fun setLocale(locale: Locale) {
        Locale.setDefault(locale)
        val config = context.resources.configuration
        config.setLocale(locale)
        context = context.createConfigurationContext(config)
    }
}
