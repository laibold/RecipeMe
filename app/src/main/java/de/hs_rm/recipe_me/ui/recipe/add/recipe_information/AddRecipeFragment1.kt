package de.hs_rm.recipe_me.ui.recipe.add.recipe_information

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.kroegerama.imgpicker.BottomSheetImagePicker
import com.kroegerama.imgpicker.ButtonType
import dagger.hilt.android.AndroidEntryPoint
import de.hs_rm.recipe_me.R
import de.hs_rm.recipe_me.databinding.AddRecipeFragment1Binding
import de.hs_rm.recipe_me.model.recipe.Recipe
import de.hs_rm.recipe_me.model.recipe.RecipeCategory
import de.hs_rm.recipe_me.service.ImageHandler
import de.hs_rm.recipe_me.ui.recipe.add.AddRecipeViewModel

@AndroidEntryPoint
class AddRecipeFragment1 : Fragment(), BottomSheetImagePicker.OnImagesSelectedListener {

    private lateinit var binding: AddRecipeFragment1Binding
    private val args: AddRecipeFragment1Args by navArgs()
    private val viewModel: AddRecipeViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.add_recipe_fragment1,
            container,
            false
        )

        // Pre-set category in spinner depending on navigation source, default none
        viewModel.setCategory(args.recipeCategory)
        binding.recipeCategorySpinner.adapter = spinnerAdapter()

        if (args.clearValues) {
            // when user navigates to fragment to create or edit a recipe
            viewModel.initRecipe(args.recipeId)
        }

        // observe recipe in viewModel except user is navigating to fragment to create a new recipe
        // means: user is editing a recipe or is moving back from fragment 2
        if (!(args.clearValues && args.recipeId == Recipe.DEFAULT_ID)) {
            viewModel.recipe.observe(viewLifecycleOwner, Observer { recipe ->
                if (recipe != null) {
                    if (recipe.name != "") {
                        binding.recipeNameField.setText(viewModel.recipe.value!!.name)
                    }
                    if (recipe.servings != 0) {
                        binding.recipeServingsField.setText(viewModel.recipe.value!!.servings.toString())
                    }
                    binding.recipeCategorySpinner.setSelection(recipe.category.ordinal)
                }
            })
        }

        // Observe category and set spinner selection and image if no custom image has been set
        viewModel.category.observe(viewLifecycleOwner, Observer { category ->
            val ordinal = category.ordinal
            if (binding.recipeCategorySpinner.selectedItemPosition != ordinal) {
                binding.recipeCategorySpinner.setSelection(ordinal)
            }

            setRecipeImageToView(category)
        })

        viewModel.recipeImage.observe(viewLifecycleOwner, Observer { image ->
            if (image != null) {
                binding.recipeImage.setImageBitmap(image)
            }
        })

        // On selection changed in spinner set category in viewModel
        binding.recipeCategorySpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    p: AdapterView<*>?, v: View?, position: Int, i: Long
                ) {
                    val category = RecipeCategory.values()[position]
                    viewModel.setCategory(category)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

        if (args.recipeId != Recipe.DEFAULT_ID) {
            binding.header.headlineText = getString(R.string.edit_recipe)
        } else {
            binding.header.headlineText = getString(R.string.new_recipe)
        }

        binding.changeImageButton.setOnClickListener { getImage() }

        binding.nextButton.setOnClickListener { onNext() }

        return binding.root
    }

    /**
     * Set bitmap to imageView.
     * If no custom image is available, the image of the recipe category will be used.
     */
    private fun setRecipeImageToView(category: RecipeCategory) {
        if (viewModel.recipeImage.value == null) {
            binding.recipeImage.setImageResource(category.drawableResId)
        }
    }

    /**
     * Adapter for recipe category spinner
     */
    private fun spinnerAdapter(): ArrayAdapter<String> {
        val names = RecipeCategory.getStringList(resources)
        return ArrayAdapter(requireContext(), R.layout.spinner_item, names)
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
            binding.recipeNameField.error = getString(nameValid)
        }

        val servingsValid = viewModel.validateServings(binding.recipeServingsField.text)
        if (servingsValid != 0) {
            binding.recipeServingsField.error = getString(servingsValid)
        }

        return nameValid == 0 && servingsValid == 0
    }

    /**
     * Callback when Image is selected in BottomSheetListener
     */
    override fun onImagesSelected(uris: List<Uri>, tag: String?) {
        if (uris.isNotEmpty()) {
            val uri = uris[0]

            viewModel.setPickedRecipeImage(
                uri,
                ImageHandler.RECIPE_IMAGE_WIDTH,
                ImageHandler.RECIPE_IMAGE_HEIGHT
            )
        }
    }

    /**
     * Show BottomSheetImagePicker
     */
    private fun getImage() {
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
