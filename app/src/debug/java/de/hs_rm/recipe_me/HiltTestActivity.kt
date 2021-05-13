package de.hs_rm.recipe_me

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import dagger.hilt.android.AndroidEntryPoint
import de.hs_rm.recipe_me.service.PreferenceService

@AndroidEntryPoint
class HiltTestActivity : AppCompatActivity() {

    private lateinit var preferenceListener: PreferenceListener

    override fun onCreate(savedInstanceState: Bundle?) {
        preferenceListener = PreferenceListener()

        // copied from MainActivity, keep in sync
        val preferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        preferences.registerOnSharedPreferenceChangeListener(preferenceListener)
        // Set theme mode by calling listener manually
        preferenceListener.onSharedPreferenceChanged(preferences, PreferenceService.THEME_KEY)

        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
    }

    override fun onDestroy() {
        super.onDestroy()

        PreferenceManager
            .getDefaultSharedPreferences(this)
            .unregisterOnSharedPreferenceChangeListener(preferenceListener)
    }

}
