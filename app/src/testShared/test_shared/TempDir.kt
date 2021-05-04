package test_shared

import androidx.documentfile.provider.DocumentFile
import java.io.File
import java.io.IOException

/**
 * TempDir can be used to create a temporary and unique directory for test cases.
 * The directories will be created in java.io.tmpdir.
 * If they should be deleted, use .destroy() after the test case.
 */
class TempDir {

    private var file: File

    init {
        var pathStr =
            System.getProperty("java.io.tmpdir")!! + File.separator + System.currentTimeMillis()
        while (pathStr == lastPathStr) {
            pathStr += "0"
        }
        lastPathStr = pathStr

        file = File(pathStr)
        if (!file.mkdir()) {
            throw IOException("TempDir file creation failed!")
        }
    }

    /**
     * List all files in TempDir
     */
    fun listFiles(): Array<File>? {
        return file.listFiles()
    }

    /**
     * Delete directory and its children
     */
    fun destroy() {
        if (!file.deleteRecursively()) {
            throw IOException("TempDir file deletion failed!")
        }
    }

    /**
     * Create {@link DocumentFile} from TempDir
     */
    fun toDocumentFile(): DocumentFile {
        return DocumentFile.fromFile(file)
    }

    /**
     * Create file with given name in directory and return it
     */
    fun createChildFile(filename: String): File {
        return File(file, filename).apply { createNewFile() }
    }

    /**
     * Create directories in path and file with filename at the end of the path and return the file (including path)
     */
    fun createChildDirsWithFile(path: String, filename: String): File {
        path.trimEnd('/')
        val newFile = File(file, "$path/$filename")
        newFile.mkdirs()
        newFile.createNewFile()
        return newFile
    }

    /**
     * Return File of TempDir
     */
    fun getFile(): File {
        return file
    }

    override fun toString(): String {
        return file.toString()
    }

    companion object {
        /** for internal use to assure that every directory has a unique name */
        var lastPathStr = ""
    }

}
