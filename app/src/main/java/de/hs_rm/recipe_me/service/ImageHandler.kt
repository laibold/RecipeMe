package de.hs_rm.recipe_me.service

import android.content.Context
import android.graphics.Bitmap
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

object ImageHandler {

    private const val IMAGES_PATH = "/images"
    private const val RECIPES_PATH = "/recipes"
    private const val PROFILE_PATH = "/profile"

    private const val RECIPE_IMAGE_NAME = "/recipe_image.jpg"

    private fun saveImage(
        image: Bitmap,
        context: Context,
        relativePath: String,
        filename: String
    ): String? {
        val path =
            context.getExternalFilesDir(null)?.absolutePath + IMAGES_PATH + "/" + relativePath

        var savedImagePath: String? = null
        val storageDir = File(path)
        var success = true
        if (!storageDir.exists()) {
            success = storageDir.mkdirs()
        }
        if (success) {
            val imageFile = File(storageDir, filename)
            savedImagePath = imageFile.absolutePath
            try {
                val fOut: OutputStream = FileOutputStream(imageFile)
                image.compress(Bitmap.CompressFormat.JPEG, 100, fOut)
                fOut.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
        return savedImagePath
    }

    fun saveRecipeImage(image: Bitmap, context: Context, id: Long): String? {
        val relativePath = "$RECIPES_PATH/$id"
        return saveImage(image, context, relativePath, RECIPE_IMAGE_NAME)
    }

}
