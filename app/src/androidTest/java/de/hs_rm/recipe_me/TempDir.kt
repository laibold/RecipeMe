package de.hs_rm.recipe_me

import androidx.documentfile.provider.DocumentFile
import java.io.File

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
        file.mkdir()
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
        file.deleteRecursively()
    }

    /**
     * Create {@link DocumentFile} from TempDir
     */
    fun toDocumentFile(): DocumentFile {
        return DocumentFile.fromFile(file)
    }

    companion object {
        var lastPathStr = ""
    }

}
