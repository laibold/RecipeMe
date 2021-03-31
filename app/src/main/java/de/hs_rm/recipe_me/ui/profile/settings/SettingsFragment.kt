package de.hs_rm.recipe_me.ui.profile.settings

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import dagger.hilt.android.AndroidEntryPoint
import de.hs_rm.recipe_me.R
import de.hs_rm.recipe_me.databinding.SettingsFragmentBinding


@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private lateinit var binding: SettingsFragmentBinding
    private val radioModeMap = mapOf(
        R.id.radio_light_mode to AppCompatDelegate.MODE_NIGHT_NO,
        R.id.radio_dark_mode to AppCompatDelegate.MODE_NIGHT_YES,
        R.id.radio_system_mode to AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
    )
    private lateinit var prefs: SharedPreferences

    private lateinit var themeKey: String
    private lateinit var timerKey: String
    private lateinit var cookingStepKey: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.settings_fragment,
            container,
            false
        )

        prefs = PreferenceManager.getDefaultSharedPreferences(requireContext().applicationContext)
        themeKey = getString(R.string.theme_key)
        timerKey = getString(R.string.timer_in_background_key)
        cookingStepKey = getString(R.string.cooking_step_preview_key)

        val editor = prefs.edit()

        // set theme preference on button selection
        binding.radioGroup.setOnCheckedChangeListener { _, checkedId ->
            val value = radioModeMap[checkedId]
            editor.putInt(themeKey, value!!).apply()
        }

        binding.timerSwitch.setOnCheckedChangeListener { _, isChecked ->
            editor.putBoolean(timerKey, isChecked).apply()
        }

        binding.cookingStepPreviewSwitch.setOnCheckedChangeListener { _, isChecked ->
            editor.putBoolean(cookingStepKey, isChecked).apply()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setThemeButtonSelection(view)
        setTimerSwitch()
        setCookingStepSwitch()
    }

    /**
     * Set theme selection depending on current preference
     */
    private fun setThemeButtonSelection(view: View) {
        // get theme from Preferences if existing, otherwise default theme
        val prefTheme = prefs.getInt(themeKey, AppCompatDelegate.getDefaultNightMode())

        // check radio button with belonging value if possible, otherwise system radio button
        if (radioModeMap.values.contains(prefTheme)) {
            val viewId = radioModeMap.entries.firstOrNull { it.value == prefTheme }?.key
            if (viewId != null) {
                val radioButton = view.findViewById<RadioButton>(viewId)
                radioButton.isChecked = true
            }
        } else {
            binding.radioSystemMode.isChecked = true
        }
    }

    /**
     * Set timer switch depending on current preference
     */
    private fun setTimerSwitch() {
        // get theme from Preferences if existing, otherwise default theme
        val prefSelection = prefs.getBoolean(timerKey, false)
        binding.timerSwitch.isChecked = prefSelection
    }

    /**
     * Set cooking step preview switch depending on current preference
     */
    private fun setCookingStepSwitch() {
        // get theme from Preferences if existing, otherwise default theme
        val prefSelection = prefs.getBoolean(cookingStepKey, true)
        binding.cookingStepPreviewSwitch.isChecked = prefSelection
    }

}
