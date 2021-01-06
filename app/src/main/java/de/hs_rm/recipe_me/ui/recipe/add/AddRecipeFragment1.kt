package de.hs_rm.recipe_me.ui.recipe.add

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import de.hs_rm.recipe_me.R
import de.hs_rm.recipe_me.databinding.AddRecipeFragment1Binding
import de.hs_rm.recipe_me.model.recipe.RecipeCategory

@AndroidEntryPoint
class AddRecipeFragment1 : Fragment() {

    private lateinit var binding: AddRecipeFragment1Binding

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

        val names = RecipeCategory.values().map { resources.getString(it.nameResId) }
        val adapter = context?.let {
            ArrayAdapter(it, R.layout.support_simple_spinner_dropdown_item, names)
        }
        binding.categorySpinner.adapter = adapter

        return binding.root
    }

}
