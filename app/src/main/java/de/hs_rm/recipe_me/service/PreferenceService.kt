package de.hs_rm.recipe_me.service

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import javax.inject.Inject

class PreferenceService @Inject constructor(val context: Context) {

    private var preferences: SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(context.applicationContext)
    private var editor: SharedPreferences.Editor = preferences.edit()

    /**
     * Get current theme (Value from AppCompatDelegate) or default if not set
     */
    fun getTheme(default: Int): Int {
        return preferences.getInt(THEME_KEY, default)
    }

    /**
     * Set current theme (Value from AppCompatDelegate)
     */
    fun setTheme(value: Int) {
        editor.putInt(THEME_KEY, value).apply()
    }

    /**
     * Get value of "start timer in background" or default if not set
     */
    fun getTimerInBackground(default: Boolean): Boolean {
        return preferences.getBoolean(TIMER_KEY, default)
    }

    /**
     * Set value for starting timer in background
     */
    fun setTimerInBackground(value: Boolean) {
        editor.putBoolean(TIMER_KEY, value).apply()
    }

    /**
     * Get value of "show cooking step preview" or default if not set
     */
    fun getShowCookingStepPreview(default: Boolean): Boolean {
        return preferences.getBoolean(COOKING_STEP_KEY, default)
    }

    /**
     * Set value for showing cooking step preview
     */
    fun setShowCookingStepPreview(value: Boolean) {
        editor.putBoolean(COOKING_STEP_KEY, value).apply()
    }

    /**
     * Export all preferences to json String with (key, value) pairs
     */
    fun preferencesToJsonString(): String {
        val prefsMap = preferences.all
        return Gson().toJson(prefsMap)
    }

    /**
     * Set preferences given from HashMap with (String, String) pairs.
     */
    fun createFromHashMap(prefMap: HashMap<*, *>) {
        val themeVal = prefMap[THEME_KEY]
        val timerVal = prefMap[TIMER_KEY]
        val cookingStepVal = prefMap[COOKING_STEP_KEY]

        themeVal?.let {
            // output will be X.0, so direct cast to integer isn't possible
            setTheme((themeVal as Double).toInt())
        }
        timerVal?.let { setTimerInBackground(timerVal as Boolean) }
        cookingStepVal?.let { setShowCookingStepPreview(cookingStepVal as Boolean) }
    }

    /**
     * Remove all preference settings
     */
    fun clear() {
        editor.clear()
    }

    companion object {
        const val THEME_KEY = "THEME"
        const val TIMER_KEY = "TIMER_IN_BACKGROUND"
        const val COOKING_STEP_KEY = "SHOW_COOKING_STEP_PREVIEW"
    }
}
