package de.hs_rm.recipe_me.ui.profile.settings

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.LifecycleOwner
import de.hs_rm.recipe_me.R
import de.hs_rm.recipe_me.databinding.ImportBackupDialogBinding
import de.hs_rm.recipe_me.model.exception.InvalidBackupFileException
import de.hs_rm.recipe_me.ui.component.CustomDialog
import java.io.FileNotFoundException
import java.io.IOException

class ImportBackupDialog(
    private val activity: Activity,
    private val viewModel: SettingsViewModel,
    private val importResultLauncher: ActivityResultLauncher<Intent>,
    private val settingsFragment: SettingsFragment
) : CustomDialog<ImportBackupDialogBinding>(activity, R.layout.import_backup_dialog) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.selectedImportFile.observe(activity as LifecycleOwner, { uri ->
            uri?.let {
                val filename = it.path?.split("/")?.last()
                @SuppressLint("SetTextI18n")
                binding.importDirText.text = ".../$filename"
            }
        })

        binding.selectFileButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/zip"
            }
            importResultLauncher.launch(intent)
        }

        binding.saveButton.setOnClickListener {
            val uri = viewModel.selectedImportFile.value
            val fileError = activity.getString(R.string.invalid_file)

            try {
                if (uri != null) {
                    val selectedFileIn = activity.contentResolver.openInputStream(uri)
                    viewModel.importBackup(selectedFileIn)

                    // set selections depending on new values (listeners would create infinite loops here)
                    settingsFragment.setThemeButtonSelection()
                    settingsFragment.setTimerSwitch()
                    settingsFragment.setCookingStepSwitch()
                    dismiss()

                    val successText = activity.getString(R.string.imported_data_success)
                    Toast.makeText(context, successText, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, fileError, Toast.LENGTH_SHORT).show()
                }
            } catch (ex: Exception) {
                when (ex) {
                    is InvalidBackupFileException, is FileNotFoundException -> {
                        // InvalidBackupFileException when file is invalid, FileNotFoundException when file not found (obviously)
                        Toast.makeText(context, fileError, Toast.LENGTH_SHORT).show()
                    }
                    is IOException -> {
                        val importError = activity.getString(R.string.error_importing_backup)
                        Toast.makeText(context, importError, Toast.LENGTH_SHORT).show()

                    }
                    else -> throw ex
                }
            }

        }

        binding.cancelButton.setOnClickListener {
            dismiss()
        }
    }

    override fun dismiss() {
        viewModel.selectedImportFile.value = null
        super.dismiss()
    }

}
