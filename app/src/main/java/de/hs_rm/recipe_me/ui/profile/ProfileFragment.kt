package de.hs_rm.recipe_me.ui.profile

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.kroegerama.imgpicker.BottomSheetImagePicker
import com.kroegerama.imgpicker.ButtonType
import dagger.hilt.android.AndroidEntryPoint
import de.hs_rm.recipe_me.BuildConfig
import de.hs_rm.recipe_me.R
import de.hs_rm.recipe_me.databinding.ProfileFragmentBinding
import de.hs_rm.recipe_me.declaration.ui.fragments.BottomSheetImageProvider
import de.hs_rm.recipe_me.model.user.User
import de.hs_rm.recipe_me.service.ImageHandler

@AndroidEntryPoint
class ProfileFragment : Fragment(), BottomSheetImagePicker.OnImagesSelectedListener,
    BottomSheetImageProvider {

    private val viewModel: ProfileViewModel by viewModels()
    private lateinit var binding: ProfileFragmentBinding

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.profile_fragment,
            container,
            false
        )

        viewModel.loadRecipeTotal()
        viewModel.loadUser()

        viewModel.profileImage.observe(viewLifecycleOwner, Observer { image ->
            if (image != null) {
                binding.profileImage.setImageBitmap(image)
            }
            binding.profileImage.visibility = View.VISIBLE
        })

        viewModel.total.observe(viewLifecycleOwner, Observer {
            binding.profileQuantityRecipesText.text = getRecipeTotalText(it)
        })

        viewModel.user.observe(viewLifecycleOwner, Observer { user ->
            binding.profileGreeting.text = getUserGreeting(user)
        })

        binding.editProfileButton.setOnClickListener {
            editProfileDialog().show()
        }

        binding.toSettingsElement.setOnClickListener {
            val direction = ProfileFragmentDirections.toSettingsFragment()
            findNavController().navigate(direction)
        }

        binding.toSiteNoticeElement.setOnClickListener {
            val direction = ProfileFragmentDirections.toSiteNoticeFragment()
            findNavController().navigate(direction)
        }

        val versionNumber = BuildConfig.VERSION_NAME

        binding.versionText.text = "${getString(R.string.version)}: $versionNumber"

        return binding.root
    }

    /**
     * @return Text for describing created recipes with message
     */
    @SuppressLint("SetTextI18n")
    private fun getRecipeTotalText(total: Int): String {
        val firstPart = getString(R.string.recipe_total_text_1)
        val mapStr = getString(R.string.recipe_total_text_map)
        return viewModel.getRecipeTotalText(firstPart, mapStr, total)
    }

    /**
     * Create edit dialog
     */
    private fun editProfileDialog(): EditProfileDialog {
        return EditProfileDialog(requireActivity(), viewModel, this)
    }

    /**
     * @return Greeting with name of the user or default greeting
     */
    private fun getUserGreeting(user: User?): String {
        if (user == null) {
            return getString(R.string.profile_greeting)
        }
        return getString(R.string.profile_greeting_name).format(user.name)
    }

    /**
     * This function will be called when an image is being selected in EditProfileDialog,
     * because it's using this Fragment's childFragmentManager
     */
    override fun onImagesSelected(uris: List<Uri>, tag: String?) {
        if (uris.isNotEmpty()) {
            val uri = uris[0]

            viewModel.setPickedRecipeImage(
                uri,
                ImageHandler.PROFILE_IMAGE_WIDTH,
                ImageHandler.PROFILE_IMAGE_HEIGHT
            )
        }
    }

    /**
     * Show BottomSheetImagePicker, called in EditProfileDialog
     */
    override fun onGetImage() {
        BottomSheetImagePicker.Builder(getString(R.string.file_provider))
            .cameraButton(ButtonType.Button)                 //style of the camera link (Button in header, Image tile, None)
            .galleryButton(ButtonType.Button)                //style of the gallery link
            .singleSelectTitle(R.string.pick_single)         //header text
            .peekHeight(R.dimen.peekHeight)                  //peek height of the bottom sheet
            .columnSize(R.dimen.columnSize)                  //size of the columns (will be changed a little to fit)
            .requestTag("single")                            //tag can be used if multiple pickers are used
            .show(childFragmentManager)
    }
}
