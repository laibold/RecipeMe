package de.hs_rm.recipe_me.model.recipe

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import de.hs_rm.recipe_me.LocaleContextProvider
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
class TimeUnitTest {

    private lateinit var context: Context

    @Before
    fun initContext() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
    }

    /**
     * Test singular and plural strings in Locale US
     */
    @Test
    fun getStringsUS() {
        context = LocaleContextProvider.createLocaleContext(Locale.US, context)

        val secondSingular = TimeUnit.SECOND.getNumberString(context.resources, 1)
        assertThat(secondSingular).isEqualTo("second")

        val secondPlural1 = TimeUnit.SECOND.getNumberString(context.resources, 0)
        val secondPlural2 = TimeUnit.SECOND.getNumberString(context.resources, null)
        assertThat(secondPlural1).isEqualTo("seconds")
        assertThat(secondPlural2).isEqualTo("seconds")
    }

    /**
     * Test singular and plural strings in Locale GERMANY
     */
    @Test
    fun getStringsDE() {
        context = LocaleContextProvider.createLocaleContext(Locale.GERMANY, context)

        val secondSingular = TimeUnit.SECOND.getNumberString(context.resources, 1)
        assertThat(secondSingular).isEqualTo("Sekunde")

        val secondPlural1 = TimeUnit.SECOND.getNumberString(context.resources, 0)
        val secondPlural2 = TimeUnit.SECOND.getNumberString(context.resources, null)
        assertThat(secondPlural1).isEqualTo("Sekunden")
        assertThat(secondPlural2).isEqualTo("Sekunden")
    }
}
