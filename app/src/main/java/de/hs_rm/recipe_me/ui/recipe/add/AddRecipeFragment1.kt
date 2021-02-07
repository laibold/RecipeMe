package de.hs_rm.recipe_me.ui.recipe.add

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.kroegerama.imgpicker.BottomSheetImagePicker
import com.kroegerama.imgpicker.ButtonType
import dagger.hilt.android.AndroidEntryPoint
import de.hs_rm.recipe_me.R
import de.hs_rm.recipe_me.databinding.AddRecipeFragment1Binding
import de.hs_rm.recipe_me.model.recipe.RecipeCategory
import de.hs_rm.recipe_me.service.ImageHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AddRecipeFragment1 : Fragment(), BottomSheetImagePicker.OnImagesSelectedListener {

    private lateinit var binding: AddRecipeFragment1Binding
    private val args: AddRecipeFragment1Args by navArgs()
    private val viewModel: AddRecipeViewModel by activityViewModels()
    private var bitmap = MutableLiveData<Bitmap>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.add_recipe_fragment1,
            container,
            false
        )

        // Pre-set category in spinner depending on navigation source, default none
        viewModel.recipeCategory = args.recipeCategory
        binding.recipeCategorySpinner.adapter = spinnerAdapter()
        binding.recipeCategorySpinner.setSelection(viewModel.recipeCategory.ordinal)

        viewModel.initRecipe()

        // If user has already set a name and navigates back to this fragment, show name in field
        if (viewModel.recipe.value?.name != "") {
            binding.recipeNameField.setText(viewModel.recipe.value!!.name)
        }
        if (viewModel.recipe.value?.servings != 0) {
            binding.recipeServingsField.setText(viewModel.recipe.value!!.servings.toString())
        }

        bitmap.observe(viewLifecycleOwner, {
            binding.recipeImage.setImageBitmap(it)
        })

        binding.changeImageButton.setOnClickListener {
            getPicture()
        }

        binding.nextButton.setOnClickListener { onNext() }

        return binding.root
    }

    /**
     * Adapter for recipe category spinner
     */
    private fun spinnerAdapter(): ArrayAdapter<String> {
        val names = RecipeCategory.getStringList(resources)
        return ArrayAdapter(requireContext(), R.layout.support_simple_spinner_dropdown_item, names)
    }

    /**
     * Navigation on next button
     */
    private fun onNext() {
        val validationOk = validate()

        if (validationOk) {
            viewModel.setRecipeAttributes(
                binding.recipeNameField.text.toString().trim(),
                binding.recipeServingsField.text.toString(),
                RecipeCategory.values()[binding.recipeCategorySpinner.selectedItemPosition]
            )

            val direction = AddRecipeFragment1Directions.toAddRecipeFragment2()
            findNavController().navigate(direction)
        }
    }

    /**
     * Validate input fields
     * @return true if all fields are valid
     */
    private fun validate(): Boolean {
        val nameValid = viewModel.validateName(binding.recipeNameField.text)
        if (nameValid != 0) {
            binding.recipeNameField.error = requireContext().resources.getString(nameValid)
        }

        val servingsValid = viewModel.validateServings(binding.recipeServingsField.text)
        if (servingsValid != 0) {
            binding.recipeServingsField.error = requireContext().resources.getString(servingsValid)
        }

        return nameValid == 0 && servingsValid == 0
    }

    override fun onImagesSelected(uris: List<Uri>, tag: String?) {
        binding.imageContainer.removeAllViews()
        uris.forEach { uri ->
            val iv = LayoutInflater.from(context)
                .inflate(R.layout.scrollitem_image, binding.imageContainer, false) as ImageView
            binding.imageContainer.addView(iv)

            Glide.with(this).load(uri).into(binding.recipeImage)

            // Save image - TODO save it only when recipe is being saved. Bitmap should be hold in ViewModel until then
            CoroutineScope(Dispatchers.IO).launch {
                val bitmap = Glide.with(requireActivity())
                    .asBitmap()
                    .load(uri)
                    .placeholder(android.R.drawable.progress_indeterminate_horizontal) // need placeholder to avoid issue like glide annotations
                    .error(android.R.drawable.stat_notify_error) // need error to avoid issue like glide annotations
                    .centerCrop()
                    .submit(1000, 1000) // TODO auslagern
                    .get()
                ImageHandler.saveRecipeImage(bitmap, requireContext(), 1) // TODO replace with id when recipe is saved
            }
        }
    }

    private fun getPicture() {
        BottomSheetImagePicker.Builder(getString(R.string.file_provider))
            .cameraButton(ButtonType.Button)            //style of the camera link (Button in header, Image tile, None)
            .galleryButton(ButtonType.Button)           //style of the gallery link
            .singleSelectTitle(R.string.pick_single)    //header text
            .peekHeight(R.dimen.peekHeight)                          //peek height of the bottom sheet
            .columnSize(R.dimen.columnSize)                           //size of the columns (will be changed a little to fit)
            .requestTag("single")            //tag can be used if multiple pickers are used
            .show(childFragmentManager)
    }
}
