package de.hs_rm.recipe_me.declaration.ui.fragments

/**
 * Interface to implement if a Fragment needs to serve as an ImagePicker provider for
 * another element that doesn't have a fragment manager
 */
interface BottomSheetImageProvider {
    fun onGetImage()
}
