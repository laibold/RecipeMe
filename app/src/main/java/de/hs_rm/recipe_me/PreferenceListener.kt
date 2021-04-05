package de.hs_rm.recipe_me

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import de.hs_rm.recipe_me.service.PreferenceService

class PreferenceListener : SharedPreferences.OnSharedPreferenceChangeListener {

    /**
     * Observe changes in preferences and switch application-scoped changes here
     */
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        val themeKey = PreferenceService.THEME_KEY
        if (key == themeKey) {
            // if key for theme changes, get value for theme or default current theme mode and switch to it
            val newTheme =
                sharedPreferences.getInt(themeKey, AppCompatDelegate.getDefaultNightMode())
            AppCompatDelegate.setDefaultNightMode(newTheme)
        }
    }

}
