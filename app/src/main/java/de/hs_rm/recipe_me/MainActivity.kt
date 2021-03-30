package de.hs_rm.recipe_me

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.preference.PreferenceManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var preferenceListener: PreferenceListener

    override fun onCreate(savedInstanceState: Bundle?) {
        preferenceListener = PreferenceListener(applicationContext)

        // if something changes in preference stuff here, please sync it with the function on HiltTestActivity for UI tests
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        preferences.registerOnSharedPreferenceChangeListener(preferenceListener)
        // Set theme mode by calling listener manually
        preferenceListener.onSharedPreferenceChanged(preferences, getString(R.string.theme_key))

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
            .unregisterOnSharedPreferenceChangeListener(preferenceListener)
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
     * This function can be used in layouts to let a view block all clicks.
     * Just add android:onClick="preventClicks" to the xml attributes
     */
    fun preventClicks(view: View) {}

}
