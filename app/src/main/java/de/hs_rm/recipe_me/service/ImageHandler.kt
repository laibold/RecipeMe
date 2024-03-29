package de.hs_rm.recipe_me.service

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.bumptech.glide.Glide
import com.bumptech.glide.signature.ObjectKey
import de.hs_rm.recipe_me.OpenForTesting
import de.hs_rm.recipe_me.model.recipe.Recipe
import java.io.File
import java.io.FileOutputStream

@OpenForTesting
class ImageHandler(private val context: Context) {

    companion object {
        private const val IMAGES_DIR = "images"
        private const val RECIPES_DIR = "recipes"
        private const val PROFILE_DIR = "profile"

        private const val RECIPE_IMAGE_NAME = "recipe_image.jpg"
        private const val PROFILE_IMAGE_NAME = "profile_image.jpg"

        // patterns for matching files that belong to the image directory
        const val RECIPE_PATTERN = "^$RECIPES_DIR/(\\d*/)?($RECIPE_IMAGE_NAME)?$"
        const val PROFILE_PATTERN = "^$PROFILE_DIR/($PROFILE_IMAGE_NAME)?$"

        private const val JPEG_QUALITY = 50
        const val RECIPE_IMAGE_WIDTH = 1000
        const val RECIPE_IMAGE_HEIGHT = 1000
        const val PROFILE_IMAGE_WIDTH = 700
        const val PROFILE_IMAGE_HEIGHT = 700
    }

    /**
     * Saves image to file system
     * @param image Image to save
     * @param absolutePath Absolute path to directory where image should be stored
     * @param filename Filename with leading '/' and filename extension (e.g. /file.jpg)
     *
     * @return On success path to image as String, else null
     */
    private fun saveImage(image: Bitmap, absolutePath: String, filename: String): String? {
        var savedImagePath: String? = null
        val storageDir = File(absolutePath)
        var success = true
        if (!storageDir.exists()) {
            success = storageDir.mkdirs()
        }
        if (success) {
            val imageFile = File(storageDir, filename)
            savedImagePath = imageFile.absolutePath
            FileOutputStream(imageFile).use { out ->
                image.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)
            }
        }
        return savedImagePath
    }

    /**
     * Deletes image from file system if it exists.
     * @param absolutePath Absolute path to folder where image is located
     * @param filename Filename with leading "/"
     */
    private fun deleteImage(absolutePath: String, filename: String) {
        val file = File(absolutePath, filename).absoluteFile
        val directory = File(absolutePath).absoluteFile

        if (file.exists()) {
            file.delete()
        }

        // If directory is now empty, also delete it
        directory.list()?.let { dir ->
            if (dir.isEmpty()) {
                directory.delete()
            }
        }
    }

    /**
     * Returns Bitmap with recipe image if available, otherwise null.
     * This function should be called from Dispatchers.IO coroutine scope in a repository.
     */
    fun getRecipeImage(recipe: Recipe): Bitmap? {
        val file = File(getRecipeDirPath(recipe.id), RECIPE_IMAGE_NAME)
        return if (file.exists()) {
            getImageFromFile(file, RECIPE_IMAGE_WIDTH, RECIPE_IMAGE_HEIGHT)
        } else {
            null
        }
    }

    /**
     * Returns file for image of recipe with given id if existing, else null
     */
    fun getRecipeImageFile(recipeId: Long): File? {
        val file = File(getRecipeDirPath(recipeId), RECIPE_IMAGE_NAME)
        return if (file.exists()) {
            file
        } else {
            null
        }
    }

    /**
     * Saves recipe image to file system
     * Must be called from Dispatchers.IO coroutine scope
     */
    fun saveRecipeImage(image: Bitmap, id: Long): String? {
        return saveImage(image, getRecipeDirPath(id), RECIPE_IMAGE_NAME)
    }

    /**
     * Deletes recipe image from file system. If it's the only file in the directory,
     * the directory also gets deleted
     */
    fun deleteRecipeImage(id: Long) {
        deleteImage(getRecipeDirPath(id), RECIPE_IMAGE_NAME)
    }

    /**
     * Returns Bitmap with profile image if available, otherwise null.
     * This function should be called from a repository.
     */
    fun getProfileImage(): Bitmap? {
        val file = File(getProfileDirPath(), PROFILE_IMAGE_NAME)
        return if (file.exists()) {
            getImageFromFile(file, PROFILE_IMAGE_WIDTH, PROFILE_IMAGE_HEIGHT)
        } else {
            null
        }
    }

    /**
     * Saves recipe image to file system
     * Must be called from Dispatchers.IO coroutine scope
     */
    fun saveProfileImage(image: Bitmap): String? {
        return saveImage(image, getProfileDirPath(), PROFILE_IMAGE_NAME)
    }

    /**
     * Returns absolute path to the directory where images of the given recipe are stored
     */
    private fun getRecipeDirPath(id: Long): String {
        return getImageDirPath() + "/$RECIPES_DIR/$id"
    }

    /**
     * Returns absolute path to the profile image directory
     */
    private fun getProfileDirPath(): String {
        return getImageDirPath() + "/$PROFILE_DIR"
    }

    /**
     * Returns absolute path to the device where the image resources of the app will be stored
     */
    fun getImageDirPath(): String {
        return context.getExternalFilesDir(null)?.absolutePath + "/$IMAGES_DIR"
    }

    /**
     * Returns image as Bitmap from given File
     * Must be called from Dispatchers.IO coroutine scope
     */
    fun getImageFromFile(file: File, width: Int, height: Int): Bitmap {
        return getImageFromUri(Uri.fromFile(file), width, height)
    }

    /**
     * Returns image as Bitmap from given Uri
     * Must be called from Dispatchers.IO coroutine scope
     */
    fun getImageFromUri(uri: Uri, width: Int, height: Int): Bitmap {
        return Glide.with(context)
            .asBitmap()
            .load(uri)
            .signature(ObjectKey(System.currentTimeMillis())) // use timestamp to prevent problems with caching
            .placeholder(android.R.drawable.progress_indeterminate_horizontal) // need placeholder to avoid issue like glide annotations
            .error(android.R.drawable.stat_notify_error) // need error to avoid issue like glide annotations
            .centerCrop()
            .submit(width, height)
            .get()
    }

}
