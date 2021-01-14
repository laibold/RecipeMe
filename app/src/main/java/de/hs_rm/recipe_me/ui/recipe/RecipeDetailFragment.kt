package de.hs_rm.recipe_me.ui.recipe

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.Observable
import androidx.databinding.ObservableInt
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import de.hs_rm.recipe_me.R
import de.hs_rm.recipe_me.databinding.RecipeDetailFragmentBinding

@AndroidEntryPoint
class RecipeDetailFragment : Fragment() {

    private lateinit var binding: RecipeDetailFragmentBinding
    private val args: RecipeDetailFragmentArgs by navArgs()
    private val viewModel: RecipeDetailViewModel by viewModels()
    private lateinit var adapter: IngredientListAdapter

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.recipe_detail_fragment,
            container,
            false
        )

        val recipeId = args.recipeId
        viewModel.loadRecipe(recipeId)
        viewModel.recipe.observe(viewLifecycleOwner, { recipeWithRelations ->
            viewModel.servings.set(recipeWithRelations.recipe.servings)
            val list = binding.recipeInfo.ingredientsListView
            adapter = IngredientListAdapter(requireContext(), R.layout.ingredient_listitem, recipeWithRelations.ingredients)
            list.adapter = adapter

            binding.recipeDetailName.text = recipeWithRelations.recipe.name

            var allCookingSteps = ""
            for (cookingStep in recipeWithRelations.cookingSteps)
                allCookingSteps += cookingStep.text + "\n"
            binding.recipeInfo.steps.text = allCookingSteps
        })

        viewModel.servings.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(observable: Observable, i: Int) {
                val servings = observable as ObservableInt
                binding.recipeInfo.servingsElement.servingsSize.text = servings.get().toString()

                if (servings.get() > 1)
                    binding.recipeInfo.servingsElement.servingsText.text = requireContext().resources.getString(R.string.servings)
                else
                    binding.recipeInfo.servingsElement.servingsText.text = requireContext().resources.getString(R.string.serving)

                if (::adapter.isInitialized) {
                    adapter.multiplier = viewModel.getServingsMultiplier()
                    adapter.notifyDataSetChanged()
                }
            }
        })

        binding.recipeInfo.servingsElement.minusButton.setOnClickListener {
            viewModel.decreaseServings()
        }

        binding.recipeInfo.servingsElement.plusButton.setOnClickListener {
            viewModel.increaseServings()
        }

        return binding.root
    }
}