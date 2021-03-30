package de.hs_rm.recipe_me

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate

class PreferenceListener(
    private val applicationContext: Context,
) : SharedPreferences.OnSharedPreferenceChangeListener {

    /**
     * Observe changes in preferences and switch application-scoped changes here
     */
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        val themeKey = applicationContext.getString(R.string.theme_key)
        if (key == themeKey) {
            // if key for theme changes, get value for theme or default current theme mode and switch to it
            val newTheme =
                sharedPreferences.getInt(themeKey, AppCompatDelegate.getDefaultNightMode())
            AppCompatDelegate.setDefaultNightMode(newTheme)
        }
    }

}
