package de.hs_rm.recipe_me.ui.profile.settings

import android.annotation.SuppressLint
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
import java.security.acl.Group


@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private lateinit var binding: SettingsFragmentBinding
    private val radioModeMap = mapOf(
        R.id.radio_light_mode to AppCompatDelegate.MODE_NIGHT_NO,
        R.id.radio_dark_mode to AppCompatDelegate.MODE_NIGHT_YES,
        R.id.radio_system_mode to AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
    )
    private val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext().applicationContext)

    val themeKey = getString(R.string.theme_key)

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

        val editor = prefs.edit()

        // set theme preference on button selection
        binding.radioGroup.setOnCheckedChangeListener { _, checkedId ->
            val value = radioModeMap[checkedId]
            editor.putInt(themeKey, value!!).apply()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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

}
