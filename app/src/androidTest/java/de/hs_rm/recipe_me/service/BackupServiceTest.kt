package de.hs_rm.recipe_me.service

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import de.hs_rm.recipe_me.declaration.anyNotNull
import de.hs_rm.recipe_me.di.Constants
import de.hs_rm.recipe_me.model.exception.InvalidBackupFileException
import de.hs_rm.recipe_me.persistence.AppDatabase
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.*
import test_shared.TempDir
import test_shared.TestDataProvider
import java.io.BufferedOutputStream
import java.io.File
import java.io.IOException
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream
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

    private lateinit var preferenceService: PreferenceService

    private lateinit var imageHandler: ImageHandler

    private lateinit var appContext: Context

    @Before
    fun init() {
        hiltRule.inject()
        assertThat(db.openHelper.databaseName).isEqualTo(AppDatabase.Environment.TEST.dbName)
        db.clearAllTables()

        appContext = InstrumentationRegistry.getInstrumentation().targetContext

        preferenceService = PreferenceService(appContext)
        imageHandler = mock(ImageHandler::class.java)
    }

    @After
    fun cleanup() {
        appContext.cacheDir.listFiles()?.let { files ->
            for (file in files) {
                file.deleteRecursively()
            }
        }
        // sleep to prevent tests from catching wrong files
        Thread.sleep(200)
    }

    @Test
    fun testDatabaseFiles() {
        val backupService = BackupService(appContext, db, preferenceService, imageHandler)
        assertThat(backupService.dbFiles).hasSize(3)
    }

    @Test
    fun canExportBackup() {
        val tempExportDir = TempDir()
        val tempImageDir = TempDir()
        val uri = tempExportDir.toDocumentFile()
        `when`(imageHandler.getImageDirPath()).thenReturn(tempImageDir.getFile().toString())
        val backupService = BackupService(appContext, db, preferenceService, imageHandler)

        val filename = backupService.exportBackup(uri)

        val files = tempExportDir.listFiles()
        assertThat(files).hasLength(1)
        assertThat(files!![0].name).isEqualTo(filename)

        tempExportDir.destroy()
        tempImageDir.destroy()
    }

    @Test
    fun canImportBackup() {
        // insert recipe to database
        runBlocking { db.recipeDao().insert(TestDataProvider.getRandomRecipe()) }

        // mock image dir
        val tempImageDir = TempDir()
        val recipesDir = File(tempImageDir.getFile(), "recipes").apply { mkdir() }
        val recipeDir = File(recipesDir, "1").apply { mkdir() }
        File(recipeDir, "recipe_image.jpg").apply { createNewFile() }
        `when`(imageHandler.getImageDirPath()).thenReturn(tempImageDir.getFile().toString())

        // preferences
        preferenceService.clear()
        preferenceService.setTimerInBackground(true)

        // mock export dir
        val tempExportDir = TempDir()
        val exportUri = tempExportDir.toDocumentFile()

        val backupService = BackupService(appContext, db, preferenceService, imageHandler)

        // export
        val filename = backupService.exportBackup(exportUri)

        val exportedFile = filename?.let { File(tempExportDir.getFile(), filename) }
        assertThat(exportedFile).isNotNull()

        val files = tempExportDir.listFiles()
        assertThat(files).hasLength(1)
        assertThat(files!![0].name).isEqualTo(filename)

        // reset db, image dir and preferences
        db.clearAllTables()
        tempImageDir.destroy() // gets destroyed while importing as well, but just to be sure (throws exception on error)
        preferenceService.clear()

        val tempImportImageDir = TempDir()
        `when`(imageHandler.getImageDirPath()).thenReturn(tempImportImageDir.getFile().toString())
        backupService.importBackup(exportedFile!!.inputStream())

        // check db
        runBlocking { assertThat(db.recipeDao().getRecipeCount()).isEqualTo(1) }

        // check image dirs and file
        val importedFile = File(
            tempImportImageDir.getFile(),
            "recipes" + File.separator + "1" + File.separator + "recipe_image.jpg"
        )
        assertThat(importedFile.exists()).isTrue()

        // check preferences
        assertThat(preferenceService.getTimerInBackground(false)).isTrue()

        tempImageDir.destroy()
        tempExportDir.destroy()
        tempImportImageDir.destroy()
    }

    /**
     * Test that backupService throws IOException if InputStream is null
     */
    @Test
    fun throwsIOExceptionOnNullInputStream() {
        val backupService = spy(BackupService(appContext, db, preferenceService, imageHandler))

        val exception = assertThrows(IOException::class.java) {
            backupService.importBackup(null)
        }

        assertThat(exception.message).contains("Error while creating inputStream")
        verify(backupService, never()).importDatabase(anyNotNull(ZipFile::class.java))
        verify(backupService, never()).importImages(
            anyList(),
            anyNotNull(ZipFile::class.java),
            anyString()
        )
        verify(backupService, never()).importPreferences(any(), anyNotNull(ZipFile::class.java))
    }

    /**
     * Test that backupService throws InvalidBackupFileException when File from InputStream is an empty zip file
     */
    @Test
    fun throwsInvalidBackupFileExceptionOnEmptyBackupFile() {
        val tempDir = TempDir()
        val file = tempDir.createChildFile("backup.zip")

        val backupService = spy(BackupService(appContext, db, preferenceService, imageHandler))

        val exception = assertThrows(InvalidBackupFileException::class.java) {
            backupService.importBackup(file.inputStream())
        }

        assertThat(exception.message).contains("Zip error for backup file")
        verify(backupService, never()).importDatabase(anyNotNull(ZipFile::class.java))
        verify(backupService, never()).importImages(
            anyList(),
            anyNotNull(ZipFile::class.java),
            anyString()
        )
        verify(backupService, never()).importPreferences(any(), anyNotNull(ZipFile::class.java))
    }

    /**
     * Test that backupService throws InvalidBackupFileException when File from InputStream is an invalid backup file
     */
    @Test
    fun throwsInvalidBackupFileExceptionOnInvalidBackupFile() {
        val tempDir = TempDir()
        val file = tempDir.createChildFile("backup.zip")

        val out = file.outputStream()
        ZipOutputStream(BufferedOutputStream(out)).use { zipOut ->
            zipOut.putNextEntry(ZipEntry("zipEntry.jpg"))
            zipOut.closeEntry()
        }

        val backupService = spy(BackupService(appContext, db, preferenceService, imageHandler))

        val exception = assertThrows(InvalidBackupFileException::class.java) {
            backupService.importBackup(file.inputStream())
        }

        assertThat(exception.message).contains("Backup file is not valid")
        verify(backupService, never()).importDatabase(anyNotNull(ZipFile::class.java))
        verify(backupService, never()).importImages(
            anyList(),
            anyNotNull(ZipFile::class.java),
            anyString()
        )
        verify(backupService, never()).importPreferences(any(), anyNotNull(ZipFile::class.java))
    }

}
