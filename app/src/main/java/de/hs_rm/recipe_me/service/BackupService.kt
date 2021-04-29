package de.hs_rm.recipe_me.service

import android.content.Context
import androidx.documentfile.provider.DocumentFile
import com.google.gson.Gson
import de.hs_rm.recipe_me.model.exception.InvalidBackupFileException
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
    private val context: Context,
    private val db: AppDatabase,
    private val preferenceService: PreferenceService,
    private val imageHandler: ImageHandler
) {

    val dbFiles = mutableListOf<File>()

    private val zipImageDir = "images/"
    private val zipDbDir = "database/"
    private val preferenceFileName = "preferences.json"

    init {
        val filenameExtensions = listOf("", "-shm", "-wal")
        val dbPath = context.getDatabasePath(db.openHelper.databaseName).absolutePath

        for (filenameExt in filenameExtensions) {
            dbFiles.add(File(dbPath + filenameExt))
        }
    }

    ///// EXPORT

    /**
     * Creates zip file with database files, images in their original structure and preferences as .json
     *
     * @param documentFile Directory where zip file should be saved
     *
     * @return Name of the zip file or null on failure
     */
    @Throws(IOException::class)
    fun exportBackup(documentFile: DocumentFile): String? {
        /*
         Structure in recipe-me-backup-202104021408.zip
         database/database(*)
         images/... (like local image structure)
         preferences.json (created from map)
         */

        // create file name
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy-HH-mm")
        val date = LocalDateTime.now().format(formatter)
        val filename = "recipe-me-backup-$date"

        // Create zip file and stream files to be saved to it
        val zipFile = documentFile.createFile("application/zip", filename)

        if (zipFile != null) {
            val out = context.contentResolver.openOutputStream(zipFile.uri)
            ZipOutputStream(BufferedOutputStream(out)).use { zipOut ->
                exportDatabase(zipOut)
                exportImages(zipOut, imageHandler.getImageDirPath())
                exportPreferences(zipOut)
            }
        } else {
            throw IOException("Error while creating zip file for backup export")
        }

        return zipFile.name
    }

    /**
     * Copy database file in transaction to ZipOutputStream
     */
    @Throws(IOException::class)
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
    @Throws(IOException::class)
    private fun exportImages(zipOut: ZipOutputStream, imageDirPath: String) {
        val imagesFile = File(imageDirPath)
        val imagesFileUri = File(imageDirPath).toURI()

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
    @Throws(IOException::class)
    private fun exportPreferences(zipOut: ZipOutputStream) {
        val prefsMapString = preferenceService.preferencesToJsonString()

        val file = File.createTempFile("temp", ".json")
        file.writeText(prefsMapString)

        zipOut.putNextEntry(ZipEntry(preferenceFileName))
        copy(file, zipOut)
        zipOut.closeEntry()
        file.delete()
    }

    ///// IMPORT

    /**
     * Import data from backup zip file
     * @throws InvalidBackupFileException if File at uri is not valid
     */
    @Throws(IOException::class)
    fun importBackup(inputStream: InputStream?) {
        // copy selected file to app's cache dir to handle it as ZipFile
        // https://stackoverflow.com/questions/58425517/how-to-get-file-path-from-the-content-uri-for-zip-file
        val tempFile = File(context.cacheDir, "backup_import_temp.zip")

        if (inputStream != null) {
            inputStream.use { copy(inputStream, tempFile) }
        } else {
            throw IOException("Error while creating inputStream for temporary zip file at importing backup")
        }

        val zipFile = ZipFile(tempFile)
        if (validateImportFile(zipFile)) {
            val entries = zipFile.entries().toList()

            val imageEntries = entries.filter { it.name.startsWith(zipImageDir) }
            val preferenceEntry = zipFile.getEntry(preferenceFileName)

            runBlocking {
                importDatabase(zipFile)
            }
            importPreferences(preferenceEntry, zipFile)
            importImages(imageEntries, zipFile, imageHandler.getImageDirPath())
        } else {
            throw InvalidBackupFileException()
        }

        tempFile.delete()
    }

    /**
     * Validates that given zipFile contains all database files and preference file
     * @return true if file is valid
     */
    private fun validateImportFile(zipFile: ZipFile): Boolean {
        val entries = zipFile.entries().toList().map { it.name }

        // database files in database/
        for (dbFile in dbFiles) {
            val fileName = dbFile.toString().split("/").last()
            val entryName = zipDbDir + fileName
            if (!entries.contains(entryName)) {
                return false
            }
        }

        // file preferences.json
        if (!entries.contains(preferenceFileName)) {
            return false
        }

        // images are optional, their format will be checked while copying
        return true
    }

    /**
     * Copy files from database directory to internal database directory (clean before)
     */
    @Throws(IOException::class)
    private fun importDatabase(file: ZipFile) {
        val dbPath = context.getDatabasePath(db.openHelper.databaseName).absolutePath
        val dbPathDirectory = dbPath.split("/").dropLast(1).joinToString("/")

        // clear directory TODO maybe better solution is possible
        val dirOut = FileOutputStream(dbPath)
        dirOut.channel.truncate(0)

        for (dbFile in dbFiles) {
            val fileName = dbFile.toString().split("/").last()
            FileOutputStream("$dbPathDirectory/$fileName").use { fOut ->
                val entry = file.getEntry(zipDbDir + fileName)
                copy(file.getInputStream(entry), fOut)
            }
        }
    }

    /**
     * Import preferences from ZipEntry in ZipFile
     */
    @Throws(IOException::class)
    private fun importPreferences(entry: ZipEntry?, zipFile: ZipFile) {
        if (entry != null) {
            zipFile.getInputStream(entry).use { fileIn ->
                InputStreamReader(fileIn).use { reader ->
                    val prefMap = Gson().fromJson(reader, HashMap::class.java)
                    preferenceService.createFromHashMap(prefMap)
                }
            }
        } else {
            throw IOException("Given zip entry for preferences was null")
        }
    }

    /**
     * Import images from backup zip file
     */
    @Throws(IOException::class)
    private fun importImages(entries: List<ZipEntry>, zipFile: ZipFile, imageDirPath: String) {
        // https://stackoverflow.com/questions/1399126/java-util-zip-recreating-directory-structure
        val imagesFile = File(imageDirPath)

        imagesFile.deleteRecursively()

        val recipePattern = ImageHandler.RECIPE_PATTERN.toRegex()
        val profilePattern = ImageHandler.PROFILE_PATTERN.toRegex()

        for (entry in entries) {
            val nameWithoutDir = entry.name.removePrefix(zipImageDir)

            // only copy files that match the pattern for recipe or profile images
            if (nameWithoutDir.matches(recipePattern) || nameWithoutDir.matches(profilePattern)) {
                val file = File(imagesFile, nameWithoutDir)
                if (entry.isDirectory) {
                    file.mkdirs()
                } else {
                    file.parentFile?.mkdirs()
                    zipFile.getInputStream(entry).use { fileIn ->
                        copy(fileIn, file)
                    }
                }
            }
        }
    }

    ///// helper

    /**
     * Copy from InputStream to OutputStream
     */
    @Throws(IOException::class)
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
    @Throws(IOException::class)
    private fun copy(file: File, out: OutputStream) {
        val input: InputStream = FileInputStream(file)
        input.use {
            copy(it, out)
        }
    }

    /**
     * Copy from InputStream to File
     */
    @Throws(IOException::class)
    private fun copy(input: InputStream, file: File) {
        val out: OutputStream = FileOutputStream(file)
        out.use {
            copy(input, it)
        }
    }
}
