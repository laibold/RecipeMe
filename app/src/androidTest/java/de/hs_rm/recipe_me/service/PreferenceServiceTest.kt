package de.hs_rm.recipe_me.service

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import test_shared.TestDataProvider
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PreferenceServiceTest {

    private lateinit var context: Context
    lateinit var preferenceService: PreferenceService
    private lateinit var contextPreferences: SharedPreferences

    @Before
    fun beforeEach() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        preferenceService = PreferenceService(context)
        contextPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        contextPreferences.edit().clear().commit()
    }

    /**
     * Test setting and getting theme preference
     */
    @Test
    fun testThemePref() {
        val theme = AppCompatDelegate.MODE_NIGHT_YES
        val default = AppCompatDelegate.MODE_NIGHT_UNSPECIFIED

        preferenceService.setTheme(theme)
        val pref = contextPreferences.getInt(PreferenceService.THEME_KEY, default)

        assertThat(preferenceService.getTheme(default)).isEqualTo(pref)
        assertThat(preferenceService.getTheme(default)).isEqualTo(theme)
    }

    /**
     * Test setting and getting timer pref
     */
    @Test
    fun testTimerPref() {
        val value = true
        val default = false

        preferenceService.setTimerInBackground(value)
        val pref = contextPreferences.getBoolean(PreferenceService.TIMER_KEY, default)

        assertThat(preferenceService.getTimerInBackground(default)).isEqualTo(pref)
        assertThat(preferenceService.getTimerInBackground(default)).isEqualTo(value)
    }

    /**
     * Test setting and getting cooking step preview pref
     */
    @Test
    fun testCookingStepPref() {
        val value = false
        val default = true

        preferenceService.setShowCookingStepPreview(value)
        val pref = contextPreferences.getBoolean(PreferenceService.COOKING_STEP_KEY, default)

        assertThat(preferenceService.getShowCookingStepPreview(default)).isEqualTo(pref)
        assertThat(preferenceService.getShowCookingStepPreview(default)).isEqualTo(value)
    }

    /**
     * Test that default values are returned when no preference is set
     */
    @Test
    fun testDefaultValues() {
        val themeDefault = TestDataProvider.getRandomInt(-1000, 1000)
        assertThat(preferenceService.getTheme(themeDefault)).isEqualTo(themeDefault)

        assertThat(preferenceService.getTimerInBackground(true)).isTrue()
        assertThat(preferenceService.getTimerInBackground(false)).isFalse()

        assertThat(preferenceService.getShowCookingStepPreview(true)).isTrue()
        assertThat(preferenceService.getShowCookingStepPreview(false)).isFalse()
    }

    /**
     * Test generation of json string based on preferences
     */
    @Test
    fun testPreferencesToJsonString() {
        val theme = TestDataProvider.getRandomInt(-1000, 1000)
        val timer = true
        val cSteps = false
        val expectedStr =
            """{"THEME":$theme,"TIMER_IN_BACKGROUND":$timer,"SHOW_COOKING_STEP_PREVIEW":$cSteps}"""

        preferenceService.setTheme(theme)
        preferenceService.setTimerInBackground(timer)
        preferenceService.setShowCookingStepPreview(cSteps)
        val str = preferenceService.preferencesToJsonString()

        assertThat(str).isEqualTo(expectedStr)
    }

    /**
     * Test generation of preferences based on HashMap generated from json String
     */
    @Test
    fun testHasMapToPreferences() {
        val theme = TestDataProvider.getRandomInt(-1000, 1000)
        val timer = true
        val cSteps = false
        val expectedStr =
            """{"THEME":$theme,"TIMER_IN_BACKGROUND":$timer,"SHOW_COOKING_STEP_PREVIEW":$cSteps}"""
        val map = Gson().fromJson(expectedStr, HashMap::class.java)

        preferenceService.createFromHashMap(map)

        assertThat(preferenceService.getTheme(theme + 1)).isEqualTo(theme)
        assertThat(preferenceService.getTimerInBackground(!timer)).isEqualTo(timer)
        assertThat(preferenceService.getShowCookingStepPreview(!cSteps)).isEqualTo(cSteps)
    }
}
