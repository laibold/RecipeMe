package de.hs_rm.recipe_me.ui.recipe.add

import android.animation.AnimatorInflater
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
import de.hs_rm.recipe_me.model.recipe.CookingStep

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

        viewModel.cookingSteps.observe(viewLifecycleOwner, {
            adapter = viewModel.cookingSteps.value?.let { list -> cookingStepListAdapter(list) }
            binding.cookingStepListView.adapter = adapter

            adapter?.notifyDataSetChanged()
            // Scroll to bottom //TODO evaluieren
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
    private fun addCookingStepDialog(cookingStep: CookingStep? = null): AddCookingStepDialog {
        return AddCookingStepDialog(requireActivity(), viewModel, cookingStep)
    }


    /**
     * @return CookingStepListAdapter for CookingStepListView
     */
    private fun cookingStepListAdapter(items: MutableList<CookingStep>): AddCookingStepListAdapter {
        return AddCookingStepListAdapter(
            requireContext(),
            R.layout.add_cooking_step_listitem,
            items,
            this
        )
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
    private fun onNext() {
        val validationOk = viewModel.validateCookingSteps()

        if (validationOk) {
            val id = viewModel.persistEntities()

            id.observe(viewLifecycleOwner, {
                val direction = AddRecipeFragment3Directions.toRecipeDetailFragment(it)
                findNavController().navigate(direction)
            })
        } else {
            AnimatorInflater.loadAnimator(context, R.animator.jump)
                .apply {
                    setTarget(binding.addCookingStepFab)
                    start()
                }
        }
    }

    /**
     * Gets called from Adapter when edit was pressed for a CookingStep item
     */
    override fun onCallback(cookingStep: CookingStep, position: Int) {
        addCookingStepDialog(cookingStep).show()
        viewModel.prepareCookingStepUpdate(position)
    }

}
