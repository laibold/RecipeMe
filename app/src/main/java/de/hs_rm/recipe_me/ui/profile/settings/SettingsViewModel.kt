package de.hs_rm.recipe_me.ui.profile.settings

import android.net.Uri
import androidx.appcompat.app.AppCompatDelegate
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import de.hs_rm.recipe_me.R
import de.hs_rm.recipe_me.service.BackupService
import de.hs_rm.recipe_me.service.PreferenceService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import java.io.InputStream
import javax.inject.Inject

/**
 * ViewModel for [SettingsFragment]
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    internal var backupService: BackupService,
    private val preferenceService: PreferenceService
) : ViewModel() {

    var selectedExportDir = MutableLiveData<DocumentFile?>()
    var selectedImportFile = MutableLiveData<Uri?>()

    // this is not really about the view but about ids that may be checked, so it belongs to the ViewModel
    private val radioModeMap = mapOf(
        R.id.radio_light_mode to AppCompatDelegate.MODE_NIGHT_NO,
        R.id.radio_dark_mode to AppCompatDelegate.MODE_NIGHT_YES,
        R.id.radio_system_mode to AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
    )

    /**
     * Get id of button to be checked depending on current theme
     */
    fun getThemeButtonIdToBeChecked(): Int? {
        // get theme from Preferences if existing, otherwise default theme
        val prefTheme = preferenceService.getTheme(AppCompatDelegate.getDefaultNightMode())

        // check radio button with belonging value if possible, otherwise system radio button
        return if (radioModeMap.values.contains(prefTheme)) {
            radioModeMap.entries.firstOrNull { it.value == prefTheme }?.key
        } else {
            R.id.radio_system_mode
        }
    }

    /**
     * Set preference for theme by button id
     */
    fun setTheme(checkedId: Int) {
        val value = radioModeMap[checkedId]
        preferenceService.setTheme(value!!)
    }

    /**
     * Get preference value for timer in background
     */
    fun getTimerInBackground(): Boolean {
        return preferenceService.getTimerInBackground(false)
    }

    /**
     * Set preference for timer in background
     */
    fun setTimerInBackground(value: Boolean) {
        preferenceService.setTimerInBackground(value)
    }

    /**
     * Get preference for show cooking step preview
     */
    fun getShowCookingStepPreview(): Boolean {
        return preferenceService.getShowCookingStepPreview(true)
    }

    /**
     * Set preference for cooking step preview
     */
    fun setShowCookingStepPreview(value: Boolean) {
        preferenceService.setShowCookingStepPreview(value)
    }

    /**
     * Export backup to uri
     */
    @Throws(IOException::class)
    fun exportBackup(documentFile: DocumentFile?) {
        if (documentFile != null) {
            CoroutineScope(Dispatchers.IO).launch {
                backupService.exportBackup(documentFile)
            }
        }
    }

    /**
     * Import backup from uri
     */
    @Throws(IOException::class)
    fun importBackup(uri: InputStream?) {
        if (uri != null) {
            backupService.importBackup(uri)
        }
    }
}
