package de.hs_rm.recipe_me.service

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import com.google.gson.Gson
import de.hs_rm.recipe_me.persistence.AppDatabase
import kotlinx.coroutines.runBlocking
import java.io.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import kotlin.collections.HashMap

/**
 * Service for backup and restoring database, images and preferences
 */
class BackupService @Inject constructor(
    val context: Context,
    val db: AppDatabase,
    val preferenceService: PreferenceService
) {

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
    fun exportBackup(uri: Uri) {
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

        // Create zip file and stream files to be saved to it
        val documentFile = DocumentFile.fromTreeUri(context, uri)
        val zipFile = documentFile?.createFile("application/zip", filename)

        if (zipFile != null) {
            val out = context.contentResolver.openOutputStream(zipFile.uri)
            ZipOutputStream(BufferedOutputStream(out)).use { zipOut ->
                exportDatabase(zipOut)
                exportImages(zipOut)
                exportPreferences(zipOut)
            }
        }
    }

    /**
     * Copy database file in transaction to ZipOutputStream
     */
    private fun exportDatabase(zipOut: ZipOutputStream) {
        db.runInTransaction {
            for (file in dbFiles) {
                FileInputStream(file).use { fileIn ->
                    BufferedInputStream(fileIn).use { bufferedIn ->
                        zipOut.putNextEntry(ZipEntry(zipDbDir + file.name))
                        bufferedIn.copyTo(zipOut, 1024)
                        zipOut.closeEntry()
                    }
                }
            }
        }
    }

    /**
     * Copy image with directory structure to ZipOutputStream
     */
    private fun exportImages(zipOut: ZipOutputStream) {
        val imagesDir = ImageHandler.getImageDirPath(context)
        val imagesFile = File(imagesDir)
        val imagesFileUri = File(imagesDir).toURI()

        // https://stackoverflow.com/questions/1399126/java-util-zip-recreating-directory-structure
        val queue = LinkedList<File>()
        queue.push(imagesFile)

        while (!queue.isEmpty()) {
            val directory = queue.pop()

            directory.listFiles()?.let { files ->
                for (file in files) {
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
    }

    /**
     * Create HashMap from Preferences and copy them to ZipOutputStream as json
     */
    private fun exportPreferences(zipOut: ZipOutputStream) {
        val prefsMapString = preferenceService.preferencesToJsonString()

        val file = File.createTempFile("temp", ".json")
        file.writeText(prefsMapString)

        zipOut.putNextEntry(ZipEntry(preferenceFileName))
        copy(file, zipOut)
        zipOut.closeEntry()
    }

    ///// IMPORT

    /**
     * Import data from backup zip file
     */
    fun importBackup(uri: Uri) {
        // copy selected file to app's cache dir to handle it as ZipFile
        // https://stackoverflow.com/questions/58425517/how-to-get-file-path-from-the-content-uri-for-zip-file
        val selectedFile = File(context.cacheDir, "backup_import_temp.zip")
        val fileIn = context.contentResolver.openInputStream(uri)

        if (fileIn != null) {
            fileIn.use { copy(fileIn, selectedFile) }
        } else {
            throw IOException() //TODO
        }

        val zipFile = ZipFile(selectedFile)
        val entries = zipFile.entries().toList()

        for (entry in entries) {
            Log.i("entry", entry.name)
        }

        val imageList = entries.filter { it.name.startsWith(zipImageDir) }
        val preferenceFile = zipFile.getEntry(preferenceFileName)

        runBlocking {
            importDatabase(zipFile)
        }
        importPreferences(preferenceFile, zipFile)
    }

    /**
     * Import preferences from ZipEntry in ZipFile
     */
    private fun importPreferences(entry: ZipEntry?, zipFile: ZipFile) {
        if (entry != null) {
            zipFile.getInputStream(entry).use { fileIn ->
                InputStreamReader(fileIn).use { reader ->
                    val prefMap = Gson().fromJson(reader, HashMap::class.java)
                    preferenceService.createFromHashMap(prefMap)
                }
            }
        }
    }

    /**
     * Copy files from database directory to internal database directory (clean before)
     */
    private fun importDatabase(file: ZipFile) {
        db.close()

        val dbPath = context.getDatabasePath(AppDatabase.env.dbName).absolutePath
        val fileOut = FileOutputStream(dbPath)
        fileOut.channel.truncate(0) // clear directory TODO maybe better solution is possible
        fileOut.use { fOut ->
            for (dbFile in dbFiles) {
                val fileName = dbFile.toString().split("/").last()
                val entry = file.getEntry(zipDbDir + fileName)
                copy(file.getInputStream(entry), fOut)
            }
        }
    }

    ///// helper

    /**
     * Copy from InputStream to OutputStream
     */
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

    /**
     * Copy file to OutputStream
     */
    private fun copy(file: File, out: OutputStream) {
        val input: InputStream = FileInputStream(file)
        input.use {
            copy(it, out)
        }
    }

    /**
     * Copy from InputStream to File
     */
    private fun copy(input: InputStream, file: File) {
        val out: OutputStream = FileOutputStream(file)
        out.use {
            copy(input, it)
        }
    }

}
