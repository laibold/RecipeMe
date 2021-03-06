package de.hs_rm.recipe_me.ui.recipe.add.cooking_step

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import de.hs_rm.recipe_me.R
import de.hs_rm.recipe_me.databinding.AddRecipeFragment3Binding
import de.hs_rm.recipe_me.declaration.ui.fragments.EditCookingStepAdapter
import de.hs_rm.recipe_me.model.relation.CookingStepWithIngredients
import de.hs_rm.recipe_me.ui.recipe.add.AddRecipeViewModel

@AndroidEntryPoint
class AddRecipeFragment3 : Fragment(), EditCookingStepAdapter {

    private lateinit var binding: AddRecipeFragment3Binding
    private val viewModel: AddRecipeViewModel by activityViewModels()
    private var adapter: AddCookingStepListAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.add_recipe_fragment3,
            container,
            false
        )

        binding.addCookingStepFab.setOnClickListener {
            addCookingStepDialog().show()
        }

        viewModel.cookingStepsWithIngredients.observe(viewLifecycleOwner, { list ->
            adapter = AddCookingStepListAdapter(
                requireContext(),
                R.layout.add_cooking_step_listitem,
                list,
                this
            )
            binding.cookingStepListView.adapter = adapter

            adapter?.notifyDataSetChanged()
            // Scroll to bottom
            binding.cookingStepListView.post {
                binding.cookingStepListView.setSelection(adapter!!.count - 1)
            }
        })

        binding.cookingStepListView.emptyView = binding.addHintText

        binding.backButton.setOnClickListener { onBack() }
        binding.nextButton.setOnClickListener { onNext() }

        return binding.root
    }

    /**
     * Create add dialog
     */
    private fun addCookingStepDialog(cookingStep: CookingStepWithIngredients? = null): AddCookingStepDialog {
        return AddCookingStepDialog(requireActivity(), viewModel, cookingStep)
    }

    /**
     * Navigation on back button
     */
    private fun onBack() {
        requireActivity().onBackPressed()
    }

    /**
     * Navigation on next button
     */
    @SuppressLint("ClickableViewAccessibility")
    private fun onNext() {
        binding.loadingSpinner.root.visibility = View.VISIBLE

        val id = viewModel.persistEntities()
        // Remove observer to pretend a wrong list to be shown for a little while
        viewModel.cookingStepsWithIngredients.removeObservers(viewLifecycleOwner)

        id.observe(viewLifecycleOwner, {
            val direction = AddRecipeFragment3Directions.toRecipeDetailFragment(it)
            findNavController().navigate(direction)
        })
    }

    /**
     * Gets called from Adapter when edit was pressed for a CookingStep item
     */
    override fun onCallback(cookingStepWithIngredients: CookingStepWithIngredients, position: Int) {
        viewModel.prepareCookingStepUpdate(position)
        addCookingStepDialog(cookingStepWithIngredients).show()
    }

}
