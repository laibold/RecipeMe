package de.hs_rm.recipe_me.ui.recipe.add

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import de.hs_rm.recipe_me.R
import de.hs_rm.recipe_me.databinding.AddRecipeFragment1Binding
import de.hs_rm.recipe_me.model.recipe.RecipeCategory

@AndroidEntryPoint
class AddRecipeFragment1 : Fragment() {

    private lateinit var binding: AddRecipeFragment1Binding
    private val args: AddRecipeFragment1Args by navArgs()
    private val viewModel: AddRecipeViewModel by activityViewModels()

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
                binding.recipeNameField.text.toString(),
                binding.recipeServingsField.text.toString(),
                RecipeCategory.values()[binding.recipeCategorySpinner.selectedItemPosition]
            )

            val direction =
                AddRecipeFragment1Directions.actionAddRecipeFragment1ToAddRecipeFragment2()
            findNavController().navigate(direction)
        }
    }

    /**
     * Validate input fields
     * @return true if all fields are valid
     */
    private fun validate(): Boolean {
        val nameValid = viewModel.validateName(binding.recipeNameField.text.toString())
        if (nameValid != 0) {
            binding.recipeNameField.error = requireContext().resources.getString(nameValid)
        }

        val servingsValid = viewModel.validateServings(binding.recipeServingsField.text.toString())
        if (servingsValid != 0) {
            binding.recipeServingsField.error = requireContext().resources.getString(servingsValid)
        }

        return nameValid == 0 && servingsValid == 0
    }

}
