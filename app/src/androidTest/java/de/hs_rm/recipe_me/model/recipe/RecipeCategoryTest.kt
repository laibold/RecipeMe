package de.hs_rm.recipe_me.model.recipe

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.util.*

class RecipeCategoryTest {

    private lateinit var context: Context

    @Before
    fun initContext() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
    }

    /**
     * Test list of all Category names in Locale US
     */
    @Test
    fun testStringListUS() {
        setLocale(Locale.US)
        val categoryList = RecipeCategory.getStringList(context.resources)
        Assert.assertEquals(RecipeCategory.values().size, categoryList.size)
        Assert.assertEquals("Main dishes", categoryList[0])
    }

    /**
     * Test list of all Category names in Locale GERMANY
     */
    @Test
    fun testStringListDE() {
        setLocale(Locale.GERMANY)
        val categoryList = RecipeCategory.getStringList(context.resources)
        Assert.assertEquals(RecipeCategory.values().size, categoryList.size)
        Assert.assertEquals("Hauptspeisen", categoryList[0])
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
