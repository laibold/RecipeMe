package de.hs_rm.recipe_me.ui.recipe

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import de.hs_rm.recipe_me.R
import de.hs_rm.recipe_me.databinding.RecipeCategoryFragmentBinding

@AndroidEntryPoint
class RecipeCategoryFragment : Fragment() {

    private val viewModel: RecipeViewModel by viewModels()
    private val args: RecipeCategoryFragmentArgs by navArgs()
    private lateinit var binding: RecipeCategoryFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.recipe_category_fragment,
            container,
            false
        )

        val category = args.recipeCategory
        val name = context?.resources?.getString(category.nameResId)
        binding.categoryHeadline.text = name

        binding.addButton.setOnClickListener {
            val direction =
                RecipeCategoryFragmentDirections.actionRecipeCategoryFragmentToAddRecipeFragment(
                    category
                )
            findNavController().navigate(direction)
        }

        val list = binding.recipeList
        viewModel.getRecipesByCategory(category).observe(this.viewLifecycleOwner, {
            val adapter =
                RecipeListAdapter(requireContext(), R.layout.recipe_listitem, it.toTypedArray())
            list.adapter = adapter
            adapter.notifyDataSetChanged()
        })

        return binding.root
    }

}
