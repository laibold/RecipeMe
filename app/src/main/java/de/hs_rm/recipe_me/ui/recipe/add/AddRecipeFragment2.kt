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
import de.hs_rm.recipe_me.databinding.AddRecipeFragment2Binding
import de.hs_rm.recipe_me.declaration.ui.fragments.EditIngredientAdapter
import de.hs_rm.recipe_me.model.recipe.Ingredient

@AndroidEntryPoint
class AddRecipeFragment2 : Fragment(), EditIngredientAdapter {

    private lateinit var binding: AddRecipeFragment2Binding
    private val viewModel: AddRecipeViewModel by activityViewModels()
    private var adapter: AddIngredientListAdapter? = null

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

        binding.addIngredientFab.setOnClickListener {
            addIngredientDialog().show()
        }

        viewModel.ingredients.observe(viewLifecycleOwner, {
            adapter = viewModel.ingredients.value?.let { list -> ingredientListAdapter(list) }
            binding.ingredientsListView.adapter = adapter
            adapter?.notifyDataSetChanged()
        })

        binding.ingredientsListView.emptyView = binding.addHintText

        binding.backButton.setOnClickListener { onBack() }
        binding.nextButton.setOnClickListener { onNext() }

        return binding.root
    }

    /**
     * Create add dialog
     */
    private fun addIngredientDialog(ingredient: Ingredient? = null): AddIngredientDialog {
        return AddIngredientDialog(requireActivity(), viewModel, ingredient)
    }

    /**
     * @return IngredientListAdapter for IngredientListView
     */
    private fun ingredientListAdapter(items: MutableList<Ingredient>): AddIngredientListAdapter {
        return AddIngredientListAdapter(
            requireContext(),
            R.layout.add_ingredient_listitem,
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
        val validationOk = validate()

        if (validationOk) {
            val direction = AddRecipeFragment2Directions.toAddRecipeFragment3()
            findNavController().navigate(direction)
        }
    }

    /**
     * Validate ingredients
     * @return true if all fields are valid
     */
    private fun validate(): Boolean {
        val ingredientsValid = viewModel.validateIngredients()
        if (ingredientsValid != 0) {
            AnimatorInflater.loadAnimator(context, R.animator.jump)
                .apply {
                    setTarget(binding.addIngredientFab)
                    start()
                }
        }
        return ingredientsValid == 0
    }

    /**
     * Gets called from Adapter when edit was pressed for an Ingredient item
     */
    override fun onCallback(ingredient: Ingredient, position: Int) {
        addIngredientDialog(ingredient).show()
        viewModel.prepareIngredientUpdate(position)
    }

}
