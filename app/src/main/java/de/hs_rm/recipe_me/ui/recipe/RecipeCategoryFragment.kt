package de.hs_rm.recipe_me.ui.recipe

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import de.hs_rm.recipe_me.R
import de.hs_rm.recipe_me.databinding.RecipeCategoryFragmentBinding
import de.hs_rm.recipe_me.model.recipe.RecipeCategory

@AndroidEntryPoint
class RecipeCategoryFragment : Fragment() {

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

        return binding.root
    }

}
