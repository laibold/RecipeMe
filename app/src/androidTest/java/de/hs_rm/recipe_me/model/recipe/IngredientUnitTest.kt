package de.hs_rm.recipe_me.model.recipe

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
class IngredientUnitTest {

    private lateinit var context: Context

    @Before
    fun initContext() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
    }

    /**
     * Test list of all Ingredient names in singular and plural
     */
    @Test
    fun testNumberStringList() {
        setLocale(Locale.US)
        val singularList = IngredientUnit.getNumberStringList(context.resources, 1.0)
        assertEquals(IngredientUnit.values().size, singularList.size)
        assertEquals("package", singularList[6])

        val pluralList = IngredientUnit.getNumberStringList(context.resources, 1.1)
        assertEquals(IngredientUnit.values().size, pluralList.size)
        assertEquals("packages", pluralList[6])
    }

    /**
     * Test singular and plural string of a IngredientUnit value in US Locale
     */
    @Test
    fun testNumberStringUS() {
        setLocale(Locale.US)
        val singularString1 = IngredientUnit.STICK.getNumberString(context.resources, 1.0)
        val singularString2 = IngredientUnit.STICK.getNumberString(context.resources, null)

        assertEquals("stick", singularString1)
        assertEquals("stick", singularString2)

        val pluralString1 = IngredientUnit.STICK.getNumberString(context.resources, 0.0)
        val pluralString2 = IngredientUnit.STICK.getNumberString(context.resources, 2.0)
        assertEquals("sticks", pluralString1)
        assertEquals("sticks", pluralString2)
    }

    /**
     * Test singular and plural string of a IngredientUnit value in GERMANY Locale
     */
    @Test
    fun testNumberStringDE() {
        setLocale(Locale.GERMANY)
        val singularString1 = IngredientUnit.STICK.getNumberString(context.resources, 1.0)
        val singularString2 = IngredientUnit.STICK.getNumberString(context.resources, null)

        assertEquals("Stange", singularString1)
        assertEquals("Stange", singularString2)

        val pluralString1 = IngredientUnit.STICK.getNumberString(context.resources, 0.0)
        val pluralString2 = IngredientUnit.STICK.getNumberString(context.resources, 2.0)
        assertEquals("Stangen", pluralString1)
        assertEquals("Stangen", pluralString2)
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
