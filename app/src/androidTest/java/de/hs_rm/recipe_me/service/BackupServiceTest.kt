package de.hs_rm.recipe_me.service

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import de.hs_rm.recipe_me.Constants
import de.hs_rm.recipe_me.TempDir
import de.hs_rm.recipe_me.TestDataProvider
import de.hs_rm.recipe_me.persistence.AppDatabase
import kotlinx.coroutines.runBlocking
import org.junit.After
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
        db.clearAllTables()
        context = InstrumentationRegistry.getInstrumentation().targetContext

        backupService = BackupService(context, db, preferenceService)
    }

    @After
    fun cleanup() {
        context.cacheDir.deleteRecursively()
    }

    @Test
    fun testDatabaseFiles() {
        assertThat(backupService.dbFiles).hasSize(3)
    }

    @Test
    fun testExportBackup() {
        val tempExportDir = TempDir()
        val tempImageDir = TempDir()
        val uri = tempExportDir.toDocumentFile()

        val filename = backupService.exportBackup(uri, tempImageDir.getFile().toString())

        val files = tempExportDir.listFiles()
        assertThat(files).hasLength(1)
        assertThat(files!![0].name).isEqualTo(filename)

        tempExportDir.destroy()
        tempImageDir.destroy()
    }

    @Test
    fun testImportBackup() {
        // insert recipe to database
        runBlocking { db.recipeDao().insert(TestDataProvider.getRandomRecipe()) }

        // mock image dir
        val tempImageDir = TempDir()
        val recipesDir = File(tempImageDir.getFile(), "recipes").apply { mkdir() }
        val recipeDir = File(recipesDir, "1").apply { mkdir() }
        File(recipeDir, "recipe_image.jpg").apply { createNewFile() }

        // preferences
        preferenceService.clear()
        preferenceService.setTimerInBackground(true)

        // mock export dir
        val tempExportDir = TempDir()
        val exportUri = tempExportDir.toDocumentFile()

        // export
        val filename = backupService.exportBackup(exportUri, tempImageDir.getFile().toString())

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
        backupService.importBackup(exportedFile!!.inputStream(), tempImportImageDir.toString())

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
    }
}
