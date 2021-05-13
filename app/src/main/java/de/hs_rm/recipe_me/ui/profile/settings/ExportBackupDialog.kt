package de.hs_rm.recipe_me.ui.profile.settings

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.LifecycleOwner
import de.hs_rm.recipe_me.R
import de.hs_rm.recipe_me.databinding.ExportBackupDialogBinding
import de.hs_rm.recipe_me.ui.component.CustomDialog
import java.io.IOException

class ExportBackupDialog(
    private val activity: Activity,
    private val viewModel: SettingsViewModel,
    private val exportResultLauncher: ActivityResultLauncher<Intent>
) : CustomDialog<ExportBackupDialogBinding>(activity, R.layout.export_backup_dialog) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.selectedExportDir.observe(activity as LifecycleOwner, { file ->
            file?.let {
                @SuppressLint("SetTextI18n")
                binding.exportDirText.text = ".../" + it.name
            }
        })

        binding.selectDirButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
            exportResultLauncher.launch(intent)
        }

        binding.saveButton.setOnClickListener {
            try {
                val documentFile = viewModel.selectedExportDir.value
                if (documentFile != null && documentFile.isDirectory) {
                    viewModel.exportBackup(documentFile)
                    dismiss()
                    val successText = activity.getString(R.string.saved_data_successfully)
                    Toast.makeText(context, successText, Toast.LENGTH_SHORT).show()
                } else {
                    val invalidFileError = activity.getString(R.string.invalid_file_path)
                    Toast.makeText(context, invalidFileError, Toast.LENGTH_SHORT).show()
                }
            } catch (e: IOException) {
                val exportError = activity.getString(R.string.error_exporting_backup)
                Toast.makeText(context, exportError, Toast.LENGTH_SHORT).show()
            }
        }

        binding.cancelButton.setOnClickListener {
            dismiss()
        }
    }

    override fun dismiss() {
        viewModel.selectedExportDir.value = null
        super.dismiss()
    }

}
