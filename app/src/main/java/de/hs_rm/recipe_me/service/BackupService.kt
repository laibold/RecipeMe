package de.hs_rm.recipe_me.service

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import de.hs_rm.recipe_me.persistence.AppDatabase
import java.io.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.inject.Inject

/**
 * Service for backup and restoring database, images and preferences
 */
class BackupService @Inject constructor(val context: Context, val db: AppDatabase) {

    private val dbFiles = mutableListOf<File>()

    private val zipImageDir = "images/"
    private val zipDbDir = "database/"
    private val preferenceFileName = "preferences.json"

    init {
        val filenameExtensions = listOf("", "-shm", "-wal")
        val dbPath = context.getDatabasePath(AppDatabase.env.dbName).absolutePath

        for (filenameExt in filenameExtensions) {
            dbFiles.add(File(dbPath + filenameExt))
        }
    }

    ///// EXPORT

    /**
     * Creates zip file with database files, images in their original structure and preferences as .json
     *
     * @param uri Directory where zip file should be saved
     */
    fun exportBackup(uri: Uri?) {
        /*
         Structure in recipe-me-backup-202104021408.zip
         database/database(*)
         images/... (like local image structure)
         preferences.json (created from map)
         */

        // create file name
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy-HH-mm")
        val date = LocalDateTime.now().format(formatter)
        val filename = "recipe-me-backup-$date.zip"

        if (uri != null) {
            // Create zip file and stream files to be saved to it
            val documentFile = DocumentFile.fromTreeUri(context, uri)
            val zipFile = documentFile?.createFile("application/zip", filename)
            val out = zipFile?.let { context.contentResolver.openOutputStream(it.uri) }

            ZipOutputStream(BufferedOutputStream(out)).use { zipOut ->
                saveDatabase(zipOut)
                saveImages(zipOut)
                savePreferences(zipOut)

                zipOut.close()
            }
        }
    }

    /**
     * Copy database file in transaction to ZipOutputStream
     */
    private fun saveDatabase(zipOut: ZipOutputStream) {
        db.runInTransaction {
            for (file in dbFiles) {
                FileInputStream(file).use { fileIn ->
                    BufferedInputStream(fileIn).use { bufferedIn ->
                        zipOut.putNextEntry(ZipEntry(zipDbDir + file.name))
                        bufferedIn.copyTo(zipOut, 1024)
                        zipOut.closeEntry()

                        bufferedIn.close()
                    }
                    fileIn.close()
                }
            }
        }
    }

    /**
     * Copy image with directory structure to ZipOutputStream
     */
    private fun saveImages(zipOut: ZipOutputStream) {
        val imagesDir = ImageHandler.getImageDirPath(context)
        val imagesFile = File(imagesDir)
        val imagesFileUri = File(imagesDir).toURI()

        // https://stackoverflow.com/questions/1399126/java-util-zip-recreating-directory-structure
        val queue = LinkedList<File>()
        queue.push(imagesFile)

        while (!queue.isEmpty()) {
            val directory = queue.pop()

            for (file in directory.listFiles()) {
                val name = imagesFileUri.relativize(file.toURI()).path
                if (file.isDirectory) {
                    queue.push(file)
                    val dirName = if (name.endsWith("/")) name else "$name/"
                    zipOut.putNextEntry(ZipEntry(zipImageDir + dirName))
                } else {
                    zipOut.putNextEntry(ZipEntry(zipImageDir + name))
                    copy(file, zipOut)
                    zipOut.closeEntry()
                }
            }
        }
    }

    /**
     * Create HashMap from Preferences and copy them to ZipOutputStream as json
     */
    private fun savePreferences(zipOut: ZipOutputStream) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context.applicationContext)
        val prefsMap = prefs.all

        val prefsMapString: String = Gson().toJson(prefsMap)

        val file = File.createTempFile("temp", ".json")
        file.writeText(prefsMapString)

        zipOut.putNextEntry(ZipEntry(preferenceFileName))
        copy(file, zipOut)
        zipOut.closeEntry()
    }

    ///// TODO IMPORT

    ///// helper

    private fun copy(input: InputStream, out: OutputStream) {
        val buffer = ByteArray(1024)
        while (true) {
            val readCount: Int = input.read(buffer)
            if (readCount < 0) {
                break
            }
            out.write(buffer, 0, readCount)
        }
    }

    private fun copy(file: File, out: OutputStream) {
        val input: InputStream = FileInputStream(file)
        input.use {
            copy(it, out)
        }
    }

    private fun copy(input: InputStream, file: File) {
        val out: OutputStream = FileOutputStream(file)
        out.use {
            copy(input, it)
        }
    }

}
