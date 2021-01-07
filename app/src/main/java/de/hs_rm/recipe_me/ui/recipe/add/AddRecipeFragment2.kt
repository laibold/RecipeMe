package de.hs_rm.recipe_me.ui.recipe.add

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import de.hs_rm.recipe_me.R
import de.hs_rm.recipe_me.databinding.AddRecipeFragment2Binding
import de.hs_rm.recipe_me.model.recipe.IngredientUnit
import de.hs_rm.recipe_me.model.recipe.RecipeCategory

@AndroidEntryPoint
class AddRecipeFragment2 : Fragment() {

    private lateinit var binding: AddRecipeFragment2Binding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.add_recipe_fragment2,
            container,
            false
        )

        binding.backButton.setOnClickListener {
            val direction =
                AddRecipeFragment2Directions.actionAddRecipeFragment2ToAddRecipeFragment1()
            findNavController().navigate(direction)
        }

        // TODO plural
        val names = IngredientUnit.values().map { resources.getString(it.singularRedId) }
        val adapter =
            ArrayAdapter(requireContext(), R.layout.support_simple_spinner_dropdown_item, names)
        binding.ingredientUnitSpinner.adapter = adapter

        return binding.root
    }

}
