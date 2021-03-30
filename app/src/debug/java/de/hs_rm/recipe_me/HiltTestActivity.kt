package de.hs_rm.recipe_me

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HiltTestActivity : AppCompatActivity() {

    private lateinit var preferenceListener: PreferenceListener

    override fun onCreate(savedInstanceState: Bundle?) {
        preferenceListener = PreferenceListener(applicationContext)

        // copied from MainActivity, keep in sync
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        preferences.registerOnSharedPreferenceChangeListener(preferenceListener)
        // Set theme mode by calling listener manually
        preferenceListener.onSharedPreferenceChanged(preferences, getString(R.string.theme_key))

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
