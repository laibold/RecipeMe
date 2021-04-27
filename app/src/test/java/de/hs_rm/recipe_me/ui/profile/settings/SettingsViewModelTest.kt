package de.hs_rm.recipe_me.ui.profile.settings;

import androidx.appcompat.app.AppCompatDelegate
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import de.hs_rm.recipe_me.R
import de.hs_rm.recipe_me.declaration.MainCoroutineRule
import de.hs_rm.recipe_me.service.BackupService
import de.hs_rm.recipe_me.service.PreferenceService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.*

class SettingsViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    @get:Rule
    val coroutineRule = MainCoroutineRule()

    @Test
    fun returnsThemeButtonToBeCheckedOnPreferenceSet() {
        val preferenceService: PreferenceService = mock {
            on { getTheme(any()) } doReturn AppCompatDelegate.MODE_NIGHT_NO
        }
        val viewModel = SettingsViewModel(mock(), preferenceService)

        val id = viewModel.getThemeButtonIdToBeChecked()

        assertThat(id).isEqualTo(R.id.radio_light_mode)
    }

    @Test
    fun returnsThemeButtonToBeCheckedWithoutPreferenceSet() {
        val backupService: BackupService = mock()
        val preferenceService: PreferenceService = mock {
            on { getTheme(any()) } doReturn AppCompatDelegate.MODE_NIGHT_UNSPECIFIED
        }
        val viewModel = SettingsViewModel(backupService, preferenceService)

        val id = viewModel.getThemeButtonIdToBeChecked()

        assertThat(id).isEqualTo(R.id.radio_system_mode)
    }

    @Test
    fun setsTheme() {
        val preferenceService: PreferenceService = mock {
            on { setTheme(any()) } doAnswer {}
        }
        val viewModel = SettingsViewModel(mock(), preferenceService)

        viewModel.setTheme(R.id.radio_light_mode)
        viewModel.setTheme(R.id.radio_dark_mode)
        viewModel.setTheme(R.id.radio_system_mode)

        verify(preferenceService, times(1)).setTheme(AppCompatDelegate.MODE_NIGHT_NO)
        verify(preferenceService, times(1)).setTheme(AppCompatDelegate.MODE_NIGHT_YES)
        verify(preferenceService, times(1)).setTheme(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    }

    @Test
    fun getsTimerInBackground() {
        val preferenceService: PreferenceService = mock {
            on { getTimerInBackground(any()) } doReturn true
        }
        val viewModel = SettingsViewModel(mock(), preferenceService)

        val pref = viewModel.getTimerInBackground()

        verify(preferenceService, times(1)).getTimerInBackground(false)
        assertThat(pref).isTrue()
    }

    @Test
    fun setsTimerInBackground() {
        val preferenceService: PreferenceService = mock {
            on { setTimerInBackground(any()) } doAnswer {}
        }
        val viewModel = SettingsViewModel(mock(), preferenceService)

        viewModel.setTimerInBackground(true)

        verify(preferenceService, times(1)).setTimerInBackground(true)
    }

    @Test
    fun getsShowCookingStepPreview() {
        val preferenceService: PreferenceService = mock {
            on { getShowCookingStepPreview(any()) } doReturn true
        }
        val viewModel = SettingsViewModel(mock(), preferenceService)

        val pref = viewModel.getShowCookingStepPreview()

        verify(preferenceService, times(1)).getShowCookingStepPreview(true)
        assertThat(pref).isTrue()
    }

    @Test
    fun setsShowCookingStepPreview() {
        val preferenceService: PreferenceService = mock {
            on { setShowCookingStepPreview(any()) } doAnswer {}
        }
        val viewModel = SettingsViewModel(mock(), preferenceService)

        viewModel.setShowCookingStepPreview(true)

        verify(preferenceService, times(1)).setShowCookingStepPreview(true)
    }

    @Test
    fun exportsBackup() {
        val backupService: BackupService = mock {
            on { exportBackup(any(), any()) } doReturn ""
        }
        val viewModel = SettingsViewModel(backupService, mock())

        viewModel.exportBackup(mock(), mock())

        verifyBlocking(backupService, times(1)) { exportBackup(any(), any()) }
    }

    @Test
    fun doesNotExportBackupOnNullDocumentFile() {
        val backupService: BackupService = mock()
        val viewModel = SettingsViewModel(backupService, mock())

        viewModel.exportBackup(null, mock())

        verify(backupService, never()).exportBackup(any(), any())
    }

    @Test
    fun importsBackup() {
        val backupService: BackupService = mock {
            on { importBackup(any(), any()) } doAnswer {}
        }
        val viewModel = SettingsViewModel(backupService, mock())

        viewModel.importBackup(mock(), mock())

        verify(backupService, times(1)).importBackup(any(), any())
    }

    @Test
    fun doesNotImportBackupOnNullDocumentFile() {
        val backupService: BackupService = mock()
        val viewModel = SettingsViewModel(backupService, mock())

        viewModel.importBackup(null, mock())

        verify(backupService, never()).importBackup(any(), any())
    }

}
