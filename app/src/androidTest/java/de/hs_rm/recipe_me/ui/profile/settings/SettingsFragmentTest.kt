package de.hs_rm.recipe_me.ui.profile.settings

import android.content.Context
import android.content.SharedPreferences
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.preference.PreferenceManager
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import de.hs_rm.recipe_me.R
import de.hs_rm.recipe_me.declaration.launchFragmentInHiltContainer
import de.hs_rm.recipe_me.di.Constants
import de.hs_rm.recipe_me.persistence.AppDatabase
import de.hs_rm.recipe_me.service.PreferenceService
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
    private lateinit var prefs: SharedPreferences

    private lateinit var themeKey: String
    private lateinit var timerKey: String
    private lateinit var cookingStepKey: String

    private lateinit var themeRadioGroup: RadioGroup

    @Before
    fun beforeEach() {
        hiltRule.inject()
        assertThat(db.openHelper.databaseName).isEqualTo(AppDatabase.Environment.TEST.dbName)

        context = InstrumentationRegistry.getInstrumentation().targetContext
        db.clearAllTables()
        prefs = PreferenceManager.getDefaultSharedPreferences(context)
        prefs.edit().clear().commit()

        themeKey = PreferenceService.THEME_KEY
        timerKey = PreferenceService.TIMER_KEY
        cookingStepKey = PreferenceService.COOKING_STEP_KEY

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
        // check if pref was set successfully
        assertThat(getCurrentThemePref()).isEqualTo(AppCompatDelegate.MODE_NIGHT_NO)
        // check if app's theme mode changed
        var currentMode = AppCompatDelegate.getDefaultNightMode()
        assertThat(currentMode).isEqualTo(AppCompatDelegate.MODE_NIGHT_NO)

        onView(withId(R.id.radio_dark_mode)).perform(click())
        assertThat(getCurrentThemePref()).isEqualTo(AppCompatDelegate.MODE_NIGHT_YES)
        currentMode = AppCompatDelegate.getDefaultNightMode()
        assertThat(currentMode).isEqualTo(AppCompatDelegate.MODE_NIGHT_YES)

        onView(withId(R.id.radio_system_mode)).perform(click())
        assertThat(getCurrentThemePref()).isEqualTo(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        currentMode = AppCompatDelegate.getDefaultNightMode()
        assertThat(currentMode).isEqualTo(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    }

    /**
     * Test that a checked radio button is also checked on Fragment restart
     * and that its settings will still be active
     */
    @Test
    fun testPersistingThemeSetting() {
        // by default this one is not checked
        onView(withId(R.id.radio_dark_mode)).perform(click())

        // restart and check
        launchFragmentInHiltContainer<SettingsFragment>()
        onView(withId(R.id.radio_dark_mode)).check(matches(isChecked()))
        assertThat(getCurrentThemePref()).isEqualTo(AppCompatDelegate.MODE_NIGHT_YES)
        val currentMode = AppCompatDelegate.getDefaultNightMode()
        assertThat(currentMode).isEqualTo(AppCompatDelegate.MODE_NIGHT_YES)
    }

    /**
     * Test that switch changes preference of timer starting in background
     */
    @Test
    fun testTimerSetting() {
        // by default switch should not be checked
        onView(withId(R.id.timer_switch)).check(matches(isNotChecked()))
        assertThat(getCurrentTimerPref(false)).isEqualTo(false)

        onView(withId(R.id.timer_switch)).perform(click())
        onView(withId(R.id.timer_switch)).check(matches(isChecked()))
        assertThat(getCurrentTimerPref(false)).isEqualTo(true)

        onView(withId(R.id.timer_switch)).perform(click())
        onView(withId(R.id.timer_switch)).check(matches(isNotChecked()))
        assertThat(getCurrentTimerPref(true)).isEqualTo(false)
    }

    /**
     * Test that state of timer switch is set based on preference
     */
    @Test
    fun testPersistingTimerSetting() {
        onView(withId(R.id.timer_switch)).check(matches(isNotChecked()))
        onView(withId(R.id.timer_switch)).perform(click())

        // restart and check
        launchFragmentInHiltContainer<SettingsFragment>()
        onView(withId(R.id.timer_switch)).check(matches(isChecked()))
        assertThat(getCurrentTimerPref(false)).isEqualTo(true)
    }

    /**
     * Test that switch changes preference of cooking step preview
     */
    @Test
    fun testCookingStepPreviewSetting() {
        // by default switch should be checked
        onView(withId(R.id.cooking_step_preview_switch)).check(matches(isChecked()))
        assertThat(getCurrentCookingStepPreviewPref(true)).isEqualTo(true)

        onView(withId(R.id.cooking_step_preview_switch)).perform(click())
        onView(withId(R.id.cooking_step_preview_switch)).check(matches(isNotChecked()))
        assertThat(getCurrentCookingStepPreviewPref(true)).isEqualTo(false)

        onView(withId(R.id.cooking_step_preview_switch)).perform(click())
        onView(withId(R.id.cooking_step_preview_switch)).check(matches(isChecked()))
        assertThat(getCurrentCookingStepPreviewPref(false)).isEqualTo(true)
    }

    /**
     * Test stat state of cooking step preview switch is set based on preference
     */
    @Test
    fun testPersistingCookingStepPreviewSetting() {
        onView(withId(R.id.cooking_step_preview_switch)).check(matches(isChecked()))
        onView(withId(R.id.cooking_step_preview_switch)).perform(click())

        // restart and check
        launchFragmentInHiltContainer<SettingsFragment>()
        onView(withId(R.id.cooking_step_preview_switch)).check(matches(isNotChecked()))
        assertThat(getCurrentCookingStepPreviewPref(true)).isEqualTo(false)
    }

    /////

    /**
     * Returns value of current night theme preference
     *
     * @return current pref value or -100 (= MODE_NIGHT_UNSPECIFIED) if none defined
     */
    private fun getCurrentThemePref(): Int {
        val default = AppCompatDelegate.MODE_NIGHT_UNSPECIFIED
        return prefs.getInt(themeKey, default)
    }

    /**
     * Returns value of current "start timer in background" preference
     *
     * @param default return value if no value is defined
     */
    private fun getCurrentTimerPref(default: Boolean): Boolean {
        return prefs.getBoolean(timerKey, default)
    }

    /**
     * Returns value of current "show cooking steps in preview" preference
     *
     * @param default return value if no value is defined
     */
    private fun getCurrentCookingStepPreviewPref(default: Boolean): Boolean {
        return prefs.getBoolean(cookingStepKey, default)
    }
}
