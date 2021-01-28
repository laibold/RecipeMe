package de.hs_rm.recipe_me.ui.recipe.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import de.hs_rm.recipe_me.R
import de.hs_rm.recipe_me.databinding.RecipeHomeFragmentBinding
import de.hs_rm.recipe_me.model.recipe.RecipeCategory
import de.hs_rm.recipe_me.ui.recipe.category.RecipeCategoryViewModel

@AndroidEntryPoint
class RecipeHomeFragment : Fragment() {

    private val viewModel: RecipeCategoryViewModel by viewModels()
    private lateinit var binding: RecipeHomeFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.recipe_home_fragment,
            container,
            false
        )

        binding.addButton.setOnClickListener {
            val direction = RecipeHomeFragmentDirections.toAddRecipeNavGraph()
            findNavController().navigate(direction)
        }

        val list = binding.homeScrollview.list
        val categories = RecipeCategory.values()
        list.adapter = CategoryListAdapter(requireContext(), R.layout.category_listitem, categories)

        return binding.root
    }

}
