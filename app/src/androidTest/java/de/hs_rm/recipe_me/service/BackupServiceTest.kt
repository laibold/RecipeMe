package de.hs_rm.recipe_me.service

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import de.hs_rm.recipe_me.Constants
import de.hs_rm.recipe_me.TempDir
import de.hs_rm.recipe_me.persistence.AppDatabase
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File
import javax.inject.Inject
import javax.inject.Named

@HiltAndroidTest
class BackupServiceTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Inject
    @Named(Constants.TEST_NAME)
    lateinit var db: AppDatabase

    @Inject
    lateinit var preferenceService: PreferenceService

    @Inject
    lateinit var backupService: BackupService

    lateinit var context: Context

    @Before
    fun init() {
        hiltRule.inject()
        assertThat(db.openHelper.databaseName).isEqualTo(AppDatabase.Environment.TEST.dbName)
        context = InstrumentationRegistry.getInstrumentation().targetContext

        backupService = BackupService(context, db, preferenceService)
    }

    @Test
    fun testDatabaseFiles() {
        assertThat(backupService.dbFiles).hasSize(3)
    }

    @Test
    fun testExportBackup() {
        val tempDir = TempDir()
        val uri = tempDir.toDocumentFile()

        val filename = backupService.exportBackup(uri)

        val files = tempDir.listFiles()
        assertThat(files).hasLength(1)
        assertThat(files!![0].name).isEqualTo(filename)

        tempDir.destroy()
    }

    @Test
    fun testImportBackup() {
        val tempDir = TempDir()
        val uri = tempDir.toDocumentFile()

        val filename = backupService.exportBackup(uri)
        val exportedFile = filename?.let { File(tempDir.getFile(), filename) }
        assertThat(exportedFile).isNotNull()

        backupService.importBackup(exportedFile?.inputStream())
    }
}
