package de.hs_rm.recipe_me.service.repository

import android.graphics.Bitmap
import android.net.Uri
import de.hs_rm.recipe_me.service.ImageHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Single Source of Truth for user's profile images. Use it with Dependency Injection
 */
@Singleton
class UserImageRepository @Inject constructor(
    private val imageHandler: ImageHandler
) {

    /**
     * Returns image as Bitmap from given Uri
     * Must be called from Dispatchers.IO coroutine scope
     */
    fun getImageFromUri(uri: Uri, width: Int, height: Int): Bitmap {
        return imageHandler.getImageFromUri(uri, width, height)
    }

    /**
     * Returns Bitmap with recipe image if available, otherwise null
     */
    fun getProfileImage(): Bitmap? {
        return imageHandler.getProfileImage()
    }

    /**
     * Saves user image to file system
     */
    fun saveProfileImage(image: Bitmap) {
        CoroutineScope(Dispatchers.IO).launch {
            imageHandler.saveProfileImage(image)
        }
    }

}
