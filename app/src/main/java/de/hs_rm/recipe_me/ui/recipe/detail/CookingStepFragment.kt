package de.hs_rm.recipe_me.ui.recipe.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import dagger.hilt.android.AndroidEntryPoint
import de.hs_rm.recipe_me.R
import de.hs_rm.recipe_me.databinding.CookingStepFragmentBinding

@AndroidEntryPoint
class CookingStepFragment : Fragment() {

    private val viewModel: RecipeDetailViewModel by activityViewModels()
    private lateinit var binding: CookingStepFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.cooking_step_fragment,
            container,
            false
        )

        binding.recipeNameHeadline.text = viewModel.recipe.value!!.recipe.name

        setAdapter()

        return binding.root
    }

    /**
     * Set [CookingStepListAdapter] to cookingStepListView
     */
    private fun setAdapter() {
        val adapter = CookingStepListAdapter(
            requireContext(),
            R.layout.cooking_step_listitem,
            viewModel.recipe.value!!.cookingSteps
        )

        binding.cookingStepListView.adapter = adapter
    }

}
