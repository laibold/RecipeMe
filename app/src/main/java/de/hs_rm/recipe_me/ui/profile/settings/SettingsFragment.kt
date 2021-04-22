package de.hs_rm.recipe_me.ui.profile.settings

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import de.hs_rm.recipe_me.R
import de.hs_rm.recipe_me.databinding.SettingsFragmentBinding
import de.hs_rm.recipe_me.model.exception.InvalidBackupFileException

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private lateinit var binding: SettingsFragmentBinding
    private val viewModel: SettingsViewModel by viewModels()

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

        selectDirectoryKey = requireContext().resources.getInteger(R.integer.select_directory_key)

        // set theme preference on button selection
        binding.radioGroup.setOnCheckedChangeListener { _, checkedId ->
            viewModel.setTheme(checkedId)
        }

        binding.timerSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setTimerInBackground(isChecked)
        }

        binding.cookingStepPreviewSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setShowCookingStepPreview(isChecked)
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
                    val documentFile = uri?.let { DocumentFile.fromTreeUri(requireContext(), it) }
                    documentFile?.let { viewModel.exportBackup(documentFile) }
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
                    // TODO block UI here
                    try {
                        viewModel.importBackup(uri)
                    } catch (e: InvalidBackupFileException) {
                        val error = getString(R.string.invalid_file)
                        Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                    }

                    // set selections depending on new values (listeners would create infinite loops here)
                    setThemeButtonSelection(binding.root)
                    setTimerSwitch()
                    setCookingStepSwitch()
                }
            }
        }
    }

    /**
     * Set theme selection depending on current preference
     */
    private fun setThemeButtonSelection(view: View) {
        val viewId = viewModel.getThemeButtonIdToBeChecked()
        if (viewId != null) {
            val radioButton = view.findViewById<RadioButton>(viewId)
            radioButton.isChecked = true
        }
    }

    /**
     * Set timer switch depending on current preference
     */
    private fun setTimerSwitch() {
        // get theme from Preferences if existing, otherwise default theme
        val prefSelection = viewModel.getTimerInBackground()
        binding.timerSwitch.isChecked = prefSelection
    }

    /**
     * Set cooking step preview switch depending on current preference
     */
    private fun setCookingStepSwitch() {
        // get theme from Preferences if existing, otherwise default theme
        val prefSelection = viewModel.getShowCookingStepPreview()
        binding.cookingStepPreviewSwitch.isChecked = prefSelection
    }
}
