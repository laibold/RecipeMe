package de.hs_rm.recipe_me.ui.profile.settings

import android.content.Context
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.preference.PreferenceManager
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import de.hs_rm.recipe_me.Constants
import de.hs_rm.recipe_me.R
import de.hs_rm.recipe_me.declaration.launchFragmentInHiltContainer
import de.hs_rm.recipe_me.persistence.AppDatabase
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject
import javax.inject.Named

@HiltAndroidTest
class SettingsFragmentTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Inject
    @Named(Constants.TEST_NAME)
    lateinit var db: AppDatabase

    lateinit var context: Context

    lateinit var themeRadioGroup: RadioGroup

    @Before
    fun beforeEach() {
        hiltRule.inject()
        assertThat(db.openHelper.databaseName).isEqualTo(AppDatabase.Environment.TEST.dbName)

        context = InstrumentationRegistry.getInstrumentation().targetContext
        db.clearAllTables()
        PreferenceManager.getDefaultSharedPreferences(context).edit().clear()

        launchFragmentInHiltContainer<SettingsFragment> {
            themeRadioGroup = requireView().findViewById(R.id.radio_group)
        }
    }

    /**
     * Test that on selecting radio button the night/day mode changes
     */
    @Test
    fun testThemeSettings() {
        onView(withId(R.id.radio_light_mode)).perform(click())
        var currentMode = AppCompatDelegate.getDefaultNightMode()
        assertThat(currentMode).isEqualTo(AppCompatDelegate.MODE_NIGHT_NO)

        onView(withId(R.id.radio_dark_mode)).perform(click())
        currentMode = AppCompatDelegate.getDefaultNightMode()
        assertThat(currentMode).isEqualTo(AppCompatDelegate.MODE_NIGHT_YES)

        onView(withId(R.id.radio_system_mode)).perform(click())
        currentMode = AppCompatDelegate.getDefaultNightMode()
        assertThat(currentMode).isEqualTo(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    }

    /**
     * Test that a checked radio button is also checked on Fragment restart
     * and that its settings will still be active
     */
    @Test
    fun testPersistingTheme() {
        // by default this one is not checked
        onView(withId(R.id.radio_dark_mode)).perform(click())

        // restart and check
        launchFragmentInHiltContainer<SettingsFragment>()
        onView(withId(R.id.radio_dark_mode)).check(matches(isChecked()))
        val currentMode = AppCompatDelegate.getDefaultNightMode()
        assertThat(currentMode).isEqualTo(AppCompatDelegate.MODE_NIGHT_YES)
    }

}
