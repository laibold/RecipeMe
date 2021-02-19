package de.hs_rm.recipe_me.service.repository

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import de.hs_rm.recipe_me.model.recipe.Recipe
import de.hs_rm.recipe_me.service.ImageHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Single Source of Truth for recipe images. Use it with Dependency Injection
 */
@Singleton
class RecipeImageRepository @Inject constructor(
    private val context: Context,
) {

    /**
     * Saves recipe image to file system
     */
    fun saveRecipeImage(image: Bitmap, recipeId: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            ImageHandler.saveRecipeImage(context, image, recipeId)
        }
    }

    /**
     * Returns Bitmap with recipe image if available, otherwise null
     */
    fun getRecipeImage(recipe: Recipe): Bitmap? {
        return ImageHandler.getRecipeImage(context, recipe)
    }

    /**
     * Returns image as Bitmap from given Uri
     * Must be called from Dispatchers.IO coroutine scope
     */
    fun getImageFromUri(uri: Uri, width: Int, height: Int): Bitmap {
        return ImageHandler.getImageFromUri(context, uri, width, height)
    }

    /**
     * Deletes recipe image from file system in a coroutine
     */
    fun deleteRecipeImage(recipeId: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            ImageHandler.deleteRecipeImage(context, recipeId)
        }
    }

}
