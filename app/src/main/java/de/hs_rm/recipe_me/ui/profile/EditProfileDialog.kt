package de.hs_rm.recipe_me.ui.profile

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Window
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import de.hs_rm.recipe_me.R
import de.hs_rm.recipe_me.databinding.EditProfileDialogBinding
import de.hs_rm.recipe_me.declaration.ui.fragments.BottomSheetImageProvider

class EditProfileDialog constructor(
    private val activity: Activity,
    private val viewModel: ProfileViewModel,
    private val bottomSheetImageProvider: BottomSheetImageProvider
) : Dialog(activity) {

    lateinit var binding: EditProfileDialogBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        binding = DataBindingUtil.inflate(
            LayoutInflater.from(context), R.layout.edit_profile_dialog, null, false
        )
        setContentView(binding.root)

        // Set width to 90% of screen
        val width = (activity.resources.displayMetrics.widthPixels * 0.90).toInt()
        window?.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT)

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
