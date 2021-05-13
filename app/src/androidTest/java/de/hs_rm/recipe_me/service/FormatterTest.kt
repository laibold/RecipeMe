package de.hs_rm.recipe_me.service

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import de.hs_rm.recipe_me.LocaleContextProvider
import de.hs_rm.recipe_me.model.recipe.Ingredient
import de.hs_rm.recipe_me.model.recipe.IngredientUnit
import com.google.common.truth.Truth.assertThat
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
        assertThat(str).isEqualTo("Curcuma")
    }

    /**
     * Format ingredient with quantity
     */
    @Test
    fun formatIngredientWithoutUnit() {
        val ingredient = Ingredient("Bananas", 3.0, IngredientUnit.NONE)
        val str = Formatter.formatIngredient(context, ingredient)
        assertThat(str).isEqualTo("3 Bananas")
    }

    /**
     * Format ingredient with quantity and unit
     */
    @Test
    fun formatIngredientWithQuantityAndUnit() {
        context = LocaleContextProvider.createLocaleContext(Locale.US, context)

        val ingredient = Ingredient("chickpeas", 1.0, IngredientUnit.CAN)
        val str = Formatter.formatIngredient(context, ingredient)
        assertThat(str).isEqualTo("1 can chickpeas")
    }

    /**
     * Format ingredient with quantity, unit and multiplier
     */
    @Test
    fun formatIngredientWithQuantityAndUnitAndMultiplier() {
        context = LocaleContextProvider.createLocaleContext(Locale.US, context)

        val ingredient = Ingredient("Chickpeas", 1.5, IngredientUnit.CAN)
        val str = Formatter.formatIngredient(context, ingredient, 3.0)
        assertThat(str).isEqualTo("4.5 cans Chickpeas")
    }

    /**
     * Test formatting ingredient list with US locale (english strings and dot as separator)
     */
    @Test
    fun formatIngredientListWithMultipleItemsUS() {
        context = LocaleContextProvider.createLocaleContext(Locale.US, context)

        val ingredients = listOf(
            Ingredient("Bananas", 3.0, IngredientUnit.NONE),
            Ingredient("Chickpeas", 1.5, IngredientUnit.CAN),
            Ingredient("Curcuma", 0.0, IngredientUnit.NONE)
        )
        val str = Formatter.formatIngredientList(context, ingredients)

        assertThat(str).isEqualTo("3 Bananas, 1.5 cans Chickpeas, Curcuma")
    }

    /**
     * Test formatting ingredient list with DE locale (english strings and dot as separator)
     */
    @Test
    fun formatIngredientListWithMultipleItemsDE() {
        context = LocaleContextProvider.createLocaleContext(Locale.GERMANY, context)

        val ingredients = listOf(
            Ingredient("Bananen", 3.0, IngredientUnit.NONE),
            Ingredient("Kichererbsen", 1.5, IngredientUnit.CAN),
            Ingredient("Kurkuma", 2.5, IngredientUnit.GRAM)
        )
        val str = Formatter.formatIngredientList(context, ingredients)

        assertThat(str).isEqualTo("3 Bananen, 1,5 Dosen Kichererbsen, 2,5 g Kurkuma")
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

        assertThat(str).isEqualTo("3 g Bananas")
    }
}
