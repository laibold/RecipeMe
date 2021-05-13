package de.hs_rm.recipe_me.ui.profile

import android.app.Activity
import android.os.Bundle
import androidx.lifecycle.LifecycleOwner
import de.hs_rm.recipe_me.R
import de.hs_rm.recipe_me.databinding.EditProfileDialogBinding
import de.hs_rm.recipe_me.declaration.ui.fragments.BottomSheetImageProvider
import de.hs_rm.recipe_me.ui.component.CustomDialog

class EditProfileDialog(
    private val activity: Activity,
    private val viewModel: ProfileViewModel,
    private val bottomSheetImageProvider: BottomSheetImageProvider
) : CustomDialog<EditProfileDialogBinding>(activity, R.layout.edit_profile_dialog) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.editProfileImage.observe(activity as LifecycleOwner, { image ->
            if (image != null) {
                binding.profileImage.setImageBitmap(image)
            }
        })

        viewModel.profileImage.observe(activity as LifecycleOwner, { image ->
            if (image != null && viewModel.editProfileImage.value == null) {
                binding.profileImage.setImageBitmap(image)
            }
        })

        binding.changeProfilePicButton.setOnClickListener { bottomSheetImageProvider.onGetImage() }

        binding.editNameField.setText(viewModel.user.value?.name)

        binding.saveButton.setOnClickListener {
            viewModel.saveUser(binding.editNameField.text.toString())
            dismiss()
        }

        binding.cancelButton.setOnClickListener {
            dismiss()
        }
    }

    override fun dismiss() {
        viewModel.clearEditProfileImage()
        super.dismiss()
    }

}
