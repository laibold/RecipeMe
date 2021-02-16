package de.hs_rm.recipe_me.service

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import de.hs_rm.recipe_me.model.recipe.Recipe
import java.io.*

object ImageHandler {

    private const val IMAGES_PATH = "/images"
    private const val RECIPES_PATH = "/recipes"
    private const val PROFILE_PATH = "/profile"

    private const val RECIPE_IMAGE_NAME = "/recipe_image.jpg"
    private const val COOKING_STEP_PATTERN = "/step_%s.jpg"
    private const val PROFILE_IMAGE_NAME = "/profile_image.jpg"

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
                image.compress(Bitmap.CompressFormat.JPEG, 100, fOut)
                fOut.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
        return savedImagePath
    }

    /**
     * https://arkapp.medium.com/accessing-images-on-android-10-scoped-storage-bbe65160c3f4
     */
    private fun getBitmap(context: Context, imageUri: Uri): Bitmap? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.decodeBitmap(
                ImageDecoder.createSource(context.contentResolver, imageUri)
            )
        } else {
            context
                .contentResolver
                .openInputStream(imageUri)?.use { inputStream ->
                    BitmapFactory.decodeStream(inputStream)
                }
        }
    }

    fun saveRecipeImage(context: Context, image: Bitmap, id: Long): String? {
        return saveImage(image, getRecipeDirPath(context, id), RECIPE_IMAGE_NAME)
    }

    fun getRecipeImage(context: Context, recipe: Recipe): Bitmap? {
        val uri = Uri.fromFile(File(getRecipeDirPath(context, recipe.id) + RECIPE_IMAGE_NAME))

        return try {
            getBitmap(context, uri)
        } catch (ex: Exception) {
            when (ex) {
                is IOException,
                is FileNotFoundException,
                -> {
                    BitmapFactory.decodeResource(context.resources, recipe.category.drawableResId)
//                    null
                }
                else -> throw ex
            }
        }
    }

    /**
     * Returns absolute path to the image of the CookingStep of given recipe
     */
    fun getCookingStepPath(context: Context, recipeId: Long, cookingStepId: Long): String {
        return getRecipeDirPath(context,
            recipeId) + "/$cookingStepId" + COOKING_STEP_PATTERN.format(cookingStepId)
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

}
