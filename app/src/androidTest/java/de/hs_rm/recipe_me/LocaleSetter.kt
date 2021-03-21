package de.hs_rm.recipe_me

import android.content.Context
import java.util.*

object LocaleContextProvider {

    /**
     * Set Locale to context
     */
    fun createLocaleContext(locale: Locale, context: Context): Context {
        Locale.setDefault(locale)
        val config = context.resources.configuration
        config.setLocale(locale)
        return context.createConfigurationContext(config)!!
    }
}
