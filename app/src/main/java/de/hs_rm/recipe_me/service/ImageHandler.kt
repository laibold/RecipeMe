package de.hs_rm.recipe_me.service

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import de.hs_rm.recipe_me.model.recipe.Recipe
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

object ImageHandler {

    private const val IMAGES_PATH = "/images"
    private const val RECIPES_PATH = "/recipes"
    private const val PROFILE_PATH = "/profile"

    private const val RECIPE_IMAGE_NAME = "/recipe_image.jpg"
    private const val COOKING_STEP_PATTERN = "/step_%s.jpg"
    private const val PROFILE_IMAGE_NAME = "/profile_image.jpg"

    private const val JPEG_QUALITY = 50
    const val RECIPE_IMAGE_WIDTH = 1000
    const val RECIPE_IMAGE_HEIGHT = 1000

    private fun saveImage(
        image: Bitmap,
        absolutePath: String,
        filename: String,
    ): String? {
        var savedImagePath: String? = null
        val storageDir = File(absolutePath)
        var success = true
        if (!storageDir.exists()) {
            success = storageDir.mkdirs()
        }
        if (success) {
            val imageFile = File(storageDir, filename)
            savedImagePath = imageFile.absolutePath
            try {
                val fOut: OutputStream = FileOutputStream(imageFile)
                image.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, fOut)
                fOut.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
        return savedImagePath
    }

    /**
     * Deletes image from file system
     * @param absolutePath Absolute path to folder where image is located
     * @param filename Filename with leading "/"
     */
    private fun deleteImage(absolutePath: String, filename: String) {
        val directory = File(absolutePath).absoluteFile
        File(absolutePath + filename).absoluteFile.delete()

        // If directory is now empty, also delete it
        if (directory.list() != null && directory.list().isEmpty()) {
            directory.delete()
        }
    }

    /**
     * Load recipe image and set it to imageView.
     * If no custom image is available, the image of the recipe category will be used.
     */
    fun setRecipeImageToView(context: Context, imageView: ImageView, recipe: Recipe) {
        ImageLoader().setRecipeImageToView(context, imageView, recipe)
    }

    /**
     * Returns Bitmap with recipe image if available, otherwise null
     */
    fun getRecipeImage(context: Context, recipe: Recipe): Bitmap? {
        val file = File(getRecipeDirPath(context, recipe.id) + RECIPE_IMAGE_NAME)
        return if (file.exists()) {
            ImageLoader().getImageFromUri(
                context,
                Uri.fromFile(file),
                RECIPE_IMAGE_WIDTH,
                RECIPE_IMAGE_HEIGHT
            )
        } else {
            null
        }
    }

    /**
     * Returns image as Bitmap from given Uri
     * Must be called from Dispatchers.IO coroutine scope
     */
    fun getImageFromUri(context: Context, uri: Uri, width: Int, height: Int): Bitmap {
        return ImageLoader().getImageFromUri(context, uri, width, height)
    }

    /**
     * Saves recipe image to file system
     * Must be called from Dispatchers.IO coroutine scope
     */
    fun saveRecipeImage(context: Context, image: Bitmap, id: Long): String? {
        return saveImage(image, getRecipeDirPath(context, id), RECIPE_IMAGE_NAME)
    }

    /**
     * Deletes recipe image from file system. If it's the only file in the directory,
     * the directory also gets deleted
     */
    fun deleteRecipeImage(context: Context, id: Long) {
        deleteImage(getRecipeDirPath(context, id), RECIPE_IMAGE_NAME)
    }

    /**
     * Returns absolute path to the image of the CookingStep of given recipe
     */
    fun getCookingStepPath(context: Context, recipeId: Long, cookingStepId: Long): String {
        return getRecipeDirPath(context, recipeId) +
                "/$cookingStepId" +
                COOKING_STEP_PATTERN.format(cookingStepId)
    }

    /**
     * Returns absolute path to the directory where images of the given recipe are stored
     */
    private fun getRecipeDirPath(context: Context, id: Long): String {
        return getImageDirPath(context) + "$RECIPES_PATH/$id"
    }

    /**
     * Returns absolute path to the profile image
     */
    fun getProfileImagePath(context: Context): String {
        return getImageDirPath(context) + PROFILE_PATH + PROFILE_IMAGE_NAME
    }

    /**
     * Returns absolute path to the device where the image resources of the app will be stored
     */
    private fun getImageDirPath(context: Context): String {
        return context.getExternalFilesDir(null)?.absolutePath + IMAGES_PATH
    }

    /**
     * Private class to create an object instance for functions setRecipeImageToView() and getImageFromUri().
     * Glide seems to have problems with loading images in a static/singleton based method.
     */
    private class ImageLoader {

        /**
         * Load recipe image and set it to imageView.
         * If no custom image is available, the image of the recipe category will be used.
         */
        fun setRecipeImageToView(context: Context, imageView: ImageView, recipe: Recipe) {
            val file = File(getRecipeDirPath(context, recipe.id) + RECIPE_IMAGE_NAME)
            if (file.exists()) {
                GlideApp.with(context)
                    .setDefaultRequestOptions(
                        RequestOptions()
                            .error(recipe.category.drawableResId)
                    )
                    .load(Uri.fromFile(file))
                    .signature(ObjectKey(System.currentTimeMillis())) // use timestamp to prevent problems with caching
                    .into(imageView)
            } else {
                imageView.setImageResource(recipe.category.drawableResId)
            }
        }

        /**
         * Load recipe image to bitmap, return null if image doesn't exist.
         * Must be called from Dispatchers.IO coroutine scope.
         */
        fun getImageFromUri(context: Context, uri: Uri, width: Int, height: Int): Bitmap {
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

}
