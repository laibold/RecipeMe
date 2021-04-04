package de.hs_rm.recipe_me.service

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import javax.inject.Inject

class PreferenceService @Inject constructor(val context: Context) {

    private var prefs: SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(context.applicationContext)
    private var editor: SharedPreferences.Editor = prefs.edit()

    /**
     * Get current theme (Value from AppCompatDelegate) or default if not set
     */
    fun getTheme(default: Int): Int {
        return prefs.getInt(THEME_KEY, default)
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
        return prefs.getBoolean(TIMER_KEY, default)
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
        return prefs.getBoolean(COOKING_STEP_KEY, default)
    }

    /**
     * Set value for showing cooking step preview
     */
    fun setShowCookingStepPreview(value: Boolean) {
        editor.putBoolean(COOKING_STEP_KEY, value).apply()
    }

    fun preferencesToJsonString(): String {
        val prefsMap = prefs.all
        return Gson().toJson(prefsMap)
    }

    fun createFromHashMap(jsonMap: HashMap<String, String>) {

    }

    companion object {
        const val THEME_KEY = "THEME"
        const val TIMER_KEY = "TIMER_IN_BACKGROUND"
        const val COOKING_STEP_KEY = "SHOW_COOKING_STEP_PREVIEW"
    }
}
