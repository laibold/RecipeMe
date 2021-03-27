package de.hs_rm.recipe_me.model.recipe

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import de.hs_rm.recipe_me.LocaleContextProvider
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
        context = LocaleContextProvider.createLocaleContext(Locale.US, context)

        val singularList = IngredientUnit.getNumberStringList(context.resources, 1.0)
        assertThat(singularList.size).isEqualTo(IngredientUnit.values().size)
        assertThat(singularList[6]).isEqualTo("package")

        val pluralList = IngredientUnit.getNumberStringList(context.resources, 1.1)
        assertThat(pluralList.size).isEqualTo(IngredientUnit.values().size)
        assertThat(pluralList[6]).isEqualTo("packages")
    }

    /**
     * Test singular and plural string of a IngredientUnit value in US Locale
     */
    @Test
    fun testNumberStringUS() {
        context = LocaleContextProvider.createLocaleContext(Locale.US, context)

        val singularString1 = IngredientUnit.STICK.getNumberString(context.resources, 1.0)
        val singularString2 = IngredientUnit.STICK.getNumberString(context.resources, null)
        assertThat(singularString1).isEqualTo("stick")
        assertThat(singularString2).isEqualTo("stick")

        val pluralString1 = IngredientUnit.STICK.getNumberString(context.resources, 0.0)
        val pluralString2 = IngredientUnit.STICK.getNumberString(context.resources, 2.0)
        assertThat(pluralString1).isEqualTo("sticks")
        assertThat(pluralString2).isEqualTo("sticks")
    }

    /**
     * Test singular and plural string of a IngredientUnit value in GERMANY Locale
     */
    @Test
    fun testNumberStringDE() {
        context = LocaleContextProvider.createLocaleContext(Locale.GERMANY, context)

        val singularString1 = IngredientUnit.STICK.getNumberString(context.resources, 1.0)
        val singularString2 = IngredientUnit.STICK.getNumberString(context.resources, null)
        assertThat(singularString1).isEqualTo("Stange")
        assertThat(singularString2).isEqualTo("Stange")

        val pluralString1 = IngredientUnit.STICK.getNumberString(context.resources, 0.0)
        val pluralString2 = IngredientUnit.STICK.getNumberString(context.resources, 2.0)
        assertThat(pluralString1).isEqualTo("Stangen")
        assertThat(pluralString2).isEqualTo("Stangen")
    }
}
