package de.hs_rm.recipe_me.model.recipe

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import de.hs_rm.recipe_me.LocaleContextProvider
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
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
        context = LocaleContextProvider.createLocaleContext(Locale.US, context)

        val categoryList = RecipeCategory.getStringList(context.resources)
        Assert.assertEquals(RecipeCategory.values().size, categoryList.size)
        Assert.assertEquals("Main dishes", categoryList[0])
    }

    /**
     * Test list of all Category names in Locale GERMANY
     */
    @Test
    fun testStringListDE() {
        context = LocaleContextProvider.createLocaleContext(Locale.GERMANY, context)

        val categoryList = RecipeCategory.getStringList(context.resources)
        Assert.assertEquals(RecipeCategory.values().size, categoryList.size)
        Assert.assertEquals("Hauptspeisen", categoryList[0])
    }
}
