package de.hs_rm.recipe_me.ui.profile.settings

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.viewModels
import androidx.preference.PreferenceManager
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import de.hs_rm.recipe_me.R
import de.hs_rm.recipe_me.declaration.anyNotNull
import de.hs_rm.recipe_me.declaration.launchFragmentInHiltContainer
import de.hs_rm.recipe_me.di.Constants
import de.hs_rm.recipe_me.persistence.AppDatabase
import de.hs_rm.recipe_me.service.BackupService
import de.hs_rm.recipe_me.service.PreferenceService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.*
import test_shared.TempDir
import java.io.File
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Named

@HiltAndroidTest
class SettingsFragmentTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Inject
    @Named(Constants.TEST_NAME)
    lateinit var db: AppDatabase

    private lateinit var context: Context
    private lateinit var prefs: SharedPreferences
    private lateinit var viewModel: SettingsViewModel

    private lateinit var themeKey: String
    private lateinit var timerKey: String
    private lateinit var cookingStepKey: String

    private lateinit var themeRadioGroup: RadioGroup

    @Before
    fun beforeEach() {
        hiltRule.inject()
        assertThat(db.openHelper.databaseName).isEqualTo(AppDatabase.Environment.TEST.dbName)

        context = InstrumentationRegistry.getInstrumentation().targetContext
        db.clearAllTables()
        prefs = PreferenceManager.getDefaultSharedPreferences(context)
        prefs.edit().clear().commit()

        themeKey = PreferenceService.THEME_KEY
        timerKey = PreferenceService.TIMER_KEY
        cookingStepKey = PreferenceService.COOKING_STEP_KEY

        launchFragmentInHiltContainer<SettingsFragment> {
            themeRadioGroup = requireView().findViewById(R.id.radio_group)
            val tempViewModel: SettingsViewModel by viewModels()
            viewModel = tempViewModel
        }
    }

    /**
     * Test that on selecting radio button the night/day mode changes
     */
    @Test
    fun canSetTheme() {
        onView(withId(R.id.radio_light_mode)).perform(click())
        // check if pref was set successfully
        assertThat(getCurrentThemePref()).isEqualTo(AppCompatDelegate.MODE_NIGHT_NO)
        // check if app's theme mode changed
        var currentMode = AppCompatDelegate.getDefaultNightMode()
        assertThat(currentMode).isEqualTo(AppCompatDelegate.MODE_NIGHT_NO)

        onView(withId(R.id.radio_dark_mode)).perform(click())
        assertThat(getCurrentThemePref()).isEqualTo(AppCompatDelegate.MODE_NIGHT_YES)
        currentMode = AppCompatDelegate.getDefaultNightMode()
        assertThat(currentMode).isEqualTo(AppCompatDelegate.MODE_NIGHT_YES)

        onView(withId(R.id.radio_system_mode)).perform(click())
        assertThat(getCurrentThemePref()).isEqualTo(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        currentMode = AppCompatDelegate.getDefaultNightMode()
        assertThat(currentMode).isEqualTo(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    }

    /**
     * Test that a checked radio button is also checked on Fragment restart
     * and that its settings will still be active
     */
    @Test
    fun canPersistThemePreference() {
        // by default this one is not checked
        onView(withId(R.id.radio_dark_mode)).perform(click())

        // restart and check
        launchFragmentInHiltContainer<SettingsFragment>()
        onView(withId(R.id.radio_dark_mode)).check(matches(isChecked()))
        assertThat(getCurrentThemePref()).isEqualTo(AppCompatDelegate.MODE_NIGHT_YES)
        val currentMode = AppCompatDelegate.getDefaultNightMode()
        assertThat(currentMode).isEqualTo(AppCompatDelegate.MODE_NIGHT_YES)
    }

    /**
     * Test that switch changes preference of timer starting in background
     */
    @Test
    fun canSetTimerPreference() {
        // by default switch should not be checked
        onView(withId(R.id.timer_switch)).check(matches(isNotChecked()))
        assertThat(getCurrentTimerPref(false)).isEqualTo(false)

        onView(withId(R.id.timer_switch)).perform(click())
        onView(withId(R.id.timer_switch)).check(matches(isChecked()))
        assertThat(getCurrentTimerPref(false)).isEqualTo(true)

        onView(withId(R.id.timer_switch)).perform(click())
        onView(withId(R.id.timer_switch)).check(matches(isNotChecked()))
        assertThat(getCurrentTimerPref(true)).isEqualTo(false)
    }

    /**
     * Test that state of timer switch is set based on preference
     */
    @Test
    fun canPersistTimerPreference() {
        onView(withId(R.id.timer_switch)).check(matches(isNotChecked()))
        onView(withId(R.id.timer_switch)).perform(click())

        // restart and check
        launchFragmentInHiltContainer<SettingsFragment>()
        onView(withId(R.id.timer_switch)).check(matches(isChecked()))
        assertThat(getCurrentTimerPref(false)).isEqualTo(true)
    }

    /**
     * Test that switch changes preference of cooking step preview
     */
    @Test
    fun canSetCookingStepPreviewSetting() {
        // by default switch should be checked
        onView(withId(R.id.cooking_step_preview_switch)).check(matches(isChecked()))
        assertThat(getCurrentCookingStepPreviewPref(true)).isEqualTo(true)

        onView(withId(R.id.cooking_step_preview_switch)).perform(click())
        onView(withId(R.id.cooking_step_preview_switch)).check(matches(isNotChecked()))
        assertThat(getCurrentCookingStepPreviewPref(true)).isEqualTo(false)

        onView(withId(R.id.cooking_step_preview_switch)).perform(click())
        onView(withId(R.id.cooking_step_preview_switch)).check(matches(isChecked()))
        assertThat(getCurrentCookingStepPreviewPref(false)).isEqualTo(true)
    }

    /**
     * Test that state of cooking step preview switch is set based on preference
     */
    @Test
    fun canPersistCookingStepPreviewPreference() {
        onView(withId(R.id.cooking_step_preview_switch)).check(matches(isChecked()))
        onView(withId(R.id.cooking_step_preview_switch)).perform(click())

        // restart and check
        launchFragmentInHiltContainer<SettingsFragment>()
        onView(withId(R.id.cooking_step_preview_switch)).check(matches(isNotChecked()))
        assertThat(getCurrentCookingStepPreviewPref(true)).isEqualTo(false)
    }

    /**
     * Test that export dialog stays open when no directory or an invalid directory is selected
     * and that exportBackup() in service is not getting called
     */
    @Test
    fun doesNotExportBackupOnInvalidInputs() {
        viewModel.backupService = spy(viewModel.backupService)

        onView(withId(R.id.save_data_text)).perform(click())
        onView(withId(R.id.export_backup_dialog_layout)).check(matches(isDisplayed()))

        // dialog still open when no directory selected
        onView(withId(R.id.save_button)).perform(click())
        onView(withId(R.id.export_backup_dialog_layout)).check(matches(isDisplayed()))

        // dialog still open when invalid directory is selected
        GlobalScope.launch(Dispatchers.Main) {
            viewModel.selectedExportDir.value = DocumentFile.fromFile(File("invalid/dir"))
        }
        onView(withId(R.id.save_button)).perform(click())
        onView(withId(R.id.export_backup_dialog_layout)).check(matches(isDisplayed()))

        // be sad and close dialog via cancel button
        onView(withId(R.id.cancel_button)).perform(click())
        onView(withId(R.id.export_backup_dialog_layout)).check(doesNotExist())

        verify(viewModel.backupService, never()).exportBackup(anyNotNull(DocumentFile::class.java))

        // open dialog again and check if selected path is reset
        onView(withId(R.id.save_data_text)).perform(click())
        onView(withId(R.id.export_dir_text)).check(matches(withText(R.string.choose_destination)))
    }

    /**
     * Test that selected export directory is shown in dialog and that saving export file works
     */
    @Test
    fun canExportBackup() {
        val tempDir = TempDir()
        viewModel.backupService = spy(viewModel.backupService)

        onView(withId(R.id.save_data_text)).perform(click())
        onView(withId(R.id.export_backup_dialog_layout)).check(matches(isDisplayed()))

        // dialog still open when invalid directory is selected
        GlobalScope.launch(Dispatchers.Main) {
            viewModel.selectedExportDir.value = DocumentFile.fromFile(tempDir.getFile())
        }
        onView(withId(R.id.export_dir_text)).check(matches(withText(".../" + tempDir.getFile().name)))
        onView(withId(R.id.save_button)).perform(click())
        onView(withId(R.id.export_backup_dialog_layout)).check(doesNotExist())

        verify(viewModel.backupService, times(1)).exportBackup(anyNotNull(DocumentFile::class.java))
        assertThat(tempDir.listFiles()!!.size).isEqualTo(1)

        // open dialog again and check if selected path is reset
        onView(withId(R.id.save_data_text)).perform(click())
        onView(withId(R.id.export_dir_text)).check(matches(withText(R.string.choose_destination)))
    }

    /**
     * Test that import dialog stays open when no file or an invalid file is selected
     * and that exportBackup() in service is not getting called
     */
    @Test
    fun doesNotImportBackupOnInvalidInputs() {
        val tempDir = TempDir()
        val invalidBackupFile = File(tempDir.getFile(), "backup.zip").also { it.createNewFile() }
        viewModel.backupService = spy(viewModel.backupService)

        onView(withId(R.id.restore_data_text)).perform(click())
        onView(withId(R.id.import_backup_dialog_layout)).check(matches(isDisplayed()))

        // dialog still open when no directory selected
        onView(withId(R.id.save_button)).perform(click())
        onView(withId(R.id.import_backup_dialog_layout)).check(matches(isDisplayed()))

        // dialog still open when invalid uri (non existing file) is selected
        GlobalScope.launch(Dispatchers.Main) {
            viewModel.selectedImportFile.value = Uri.fromFile(File("invalid/dir/file.zip"))
        }
        onView(withId(R.id.save_button)).perform(click())
        onView(withId(R.id.import_backup_dialog_layout)).check(matches(isDisplayed()))

        // dialog still open when invalid file es selected (not a real backup file - verified in service)
        GlobalScope.launch(Dispatchers.Main) {
            viewModel.selectedImportFile.value = Uri.fromFile(invalidBackupFile)
        }
        onView(withId(R.id.save_button)).perform(click())
        onView(withId(R.id.import_backup_dialog_layout)).check(matches(isDisplayed()))

        // be sad and close dialog via cancel button
        onView(withId(R.id.cancel_button)).perform(click())
        onView(withId(R.id.import_backup_dialog_layout)).check(doesNotExist())

        // importBackup is just called once to verify invalid file (exception thrown)
        verify(viewModel.backupService, times(1)).importBackup(any(InputStream::class.java))
        assertThat(viewModel.selectedImportFile.value).isNull()

        // open dialog again and check if selected path is reset
        onView(withId(R.id.restore_data_text)).perform(click())
        onView(withId(R.id.import_dir_text)).check(matches(withText(R.string.select_file)))
    }

    /**
     * Test that importBackup() in BackupService is called when selecting an existing file
     */
    @Test
    fun canImportBackup() {
        val tempDir = TempDir()
        val backupFile = File(tempDir.getFile(), "backup.zip").also { it.createNewFile() }

        viewModel.backupService = mock(BackupService::class.java)

        onView(withId(R.id.restore_data_text)).perform(click())
        onView(withId(R.id.import_backup_dialog_layout)).check(matches(isDisplayed()))

        GlobalScope.launch(Dispatchers.Main) {
            viewModel.selectedImportFile.value = Uri.fromFile(backupFile)
        }
        onView(withId(R.id.save_button)).perform(click())

        onView(withId(R.id.import_backup_dialog_layout)).check(doesNotExist())
        verify(viewModel.backupService, times(1)).importBackup(anyNotNull(InputStream::class.java))

        // open dialog again and check if selected path is reset
        onView(withId(R.id.restore_data_text)).perform(click())
        onView(withId(R.id.import_dir_text)).check(matches(withText(R.string.select_file)))
    }

    /////

    /**
     * Returns value of current night theme preference
     *
     * @return current pref value or -100 (= MODE_NIGHT_UNSPECIFIED) if none defined
     */
    private fun getCurrentThemePref(): Int {
        val default = AppCompatDelegate.MODE_NIGHT_UNSPECIFIED
        return prefs.getInt(themeKey, default)
    }

    /**
     * Returns value of current "start timer in background" preference
     *
     * @param default return value if no value is defined
     */
    private fun getCurrentTimerPref(default: Boolean): Boolean {
        return prefs.getBoolean(timerKey, default)
    }

    /**
     * Returns value of current "show cooking steps in preview" preference
     *
     * @param default return value if no value is defined
     */
    private fun getCurrentCookingStepPreviewPref(default: Boolean): Boolean {
        return prefs.getBoolean(cookingStepKey, default)
    }
}
