package de.hs_rm.recipe_me

import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.preference.PreferenceManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), OnSharedPreferenceChangeListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        preferences.registerOnSharedPreferenceChangeListener(this)
        // Set theme mode
        onSharedPreferenceChanged(preferences, getString(R.string.theme_key))

        // set theme after default theme showed splash screen
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)

        setContentView(R.layout.main_activity)
        setUpNavigation()
    }

    override fun onDestroy() {
        super.onDestroy()

        PreferenceManager
            .getDefaultSharedPreferences(this)
            .unregisterOnSharedPreferenceChangeListener(this)
    }

    /**
     * Setup navigation with BottomNavigation and FragmentContainerView
     */
    private fun setUpNavigation() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)

        if (navHostFragment != null) {
            NavigationUI.setupWithNavController(
                bottomNavigationView,
                navHostFragment.findNavController()
            )
        }
    }

    /**
     * Observe changes in preferences and switch application-scoped changes here
     */
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        val themeKey = getString(R.string.theme_key)
        if (key == themeKey) {
            // if key for theme changes, get value for theme or default current theme mode and switch to it
            val newTheme = sharedPreferences.getInt(themeKey, AppCompatDelegate.getDefaultNightMode())
            AppCompatDelegate.setDefaultNightMode(newTheme)
        }
    }

    /**
     * This function can be used in layouts to let a view block all clicks.
     * Just add android:onClick="preventClicks" to the xml attributes
     */
    fun preventClicks(view: View) {}

}
