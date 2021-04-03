package de.hs_rm.recipe_me.ui.profile.settings

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import dagger.hilt.android.AndroidEntryPoint
import de.hs_rm.recipe_me.R
import de.hs_rm.recipe_me.databinding.SettingsFragmentBinding
import de.hs_rm.recipe_me.service.BackupService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    @Inject
    lateinit var backupService: BackupService

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
    private var selectDirectoryKey: Int = 0

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
        setKeys()

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

        val exportResultLauncher = registerExportLauncher()
        val importResultLauncher = registerImportLauncher()

        binding.saveDataText.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
            exportResultLauncher.launch(intent)
        }

        binding.restoreDataText.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/zip"
            }
            importResultLauncher.launch(intent)
            // TODO restart may be needed, because database is closed here
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
     * Returns Launcher that calls export backup function in BackupService
     */
    private fun registerExportLauncher(): ActivityResultLauncher<Intent> {
        return registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data.also { uri ->
                    CoroutineScope(Dispatchers.IO).launch {
                        backupService.exportBackup(uri)
                    }
                }
            }
        }
    }

    /**
     * Returns Launcher that calls import backup function in BackupService
     */
    private fun registerImportLauncher(): ActivityResultLauncher<Intent> {
        return registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data.also { uri ->
                    CoroutineScope(Dispatchers.IO).launch {
                        backupService.importBackup(uri)
                    }
                }
            }
        }
    }

    /**
     * Set keys used for preferences and intents
     */
    private fun setKeys() {
        themeKey = getString(R.string.theme_key)
        timerKey = getString(R.string.timer_in_background_key)
        cookingStepKey = getString(R.string.cooking_step_preview_key)
        selectDirectoryKey = requireContext().resources.getInteger(R.integer.select_directory_key)
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
