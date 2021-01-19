package de.hs_rm.recipe_me.ui.recipe.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.databinding.Observable
import androidx.databinding.ObservableInt
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import de.hs_rm.recipe_me.R
import de.hs_rm.recipe_me.databinding.RecipeDetailFragmentBinding
import de.hs_rm.recipe_me.model.relation.RecipeWithRelations

@AndroidEntryPoint
class RecipeDetailFragment : Fragment() {

    private lateinit var binding: RecipeDetailFragmentBinding
    private val args: RecipeDetailFragmentArgs by navArgs()
    private val viewModel: RecipeDetailViewModel by activityViewModels()
    private var adapter: IngredientListAdapter? = null

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
            onRecipeChanged(recipeWithRelations)
            viewModel.servings.set(recipeWithRelations.recipe.servings)
        })

        viewModel.servings.addOnPropertyChangedCallback(object :
            Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(observable: Observable, i: Int) {
                val servings = observable as ObservableInt
                onServingsChanged(servings.get())
            }
        })

        binding.recipeInfo.servingsElement.minusButton.setOnClickListener {
            viewModel.decreaseServings()
        }

        binding.recipeInfo.servingsElement.plusButton.setOnClickListener {
            viewModel.increaseServings()
        }

        binding.scrollView.setOnScrollChangeListener { _, _, scrollY, _, _ ->
            onScroll(scrollY)
        }

        binding.recipeInfo.addToShoppingListButton.setOnClickListener {
            Toast.makeText(
                context,
                "Hier kannst du bald Zutaten zur Einkaufsliste hinzufügen",
                Toast.LENGTH_LONG
            ).show()
        }

        binding.forwardButton.setOnClickListener {
            val direction = RecipeDetailFragmentDirections.toCookingStepFragment()
            findNavController().navigate(direction)
        }

        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.clearRecipe()
    }

    /**
     * Set recipe name, servings, ingredients and cooking steps to view
     */
    private fun onRecipeChanged(recipeWithRelations: RecipeWithRelations) {
        binding.recipeDetailName.text = recipeWithRelations.recipe.name
        onServingsChanged(recipeWithRelations.recipe.servings)
        setIngredientAdapter(recipeWithRelations)
        setCookingSteps(recipeWithRelations)
    }

    /**
     * Set Ingredients to ListView
     */
    private fun setIngredientAdapter(recipeWithRelations: RecipeWithRelations) {
        val list = binding.recipeInfo.ingredientsListView
        adapter = IngredientListAdapter(
            requireContext(),
            R.layout.ingredient_listitem,
            recipeWithRelations.ingredients
        )
        list.adapter = adapter
    }

    /**
     * Set CookingSteps to TextView
     */
    private fun setCookingSteps(recipeWithRelations: RecipeWithRelations) {
        var allCookingSteps = ""
        for (cookingStep in recipeWithRelations.cookingSteps)
            allCookingSteps += cookingStep.text + "\n\n"
        binding.recipeInfo.steps.text = allCookingSteps
    }

    /**
     * Set text of servings element and refresh ListView
     */
    private fun onServingsChanged(servings: Int) {
        binding.recipeInfo.servingsElement.servingsSize.text = servings.toString()

        if (servings > 1)
            binding.recipeInfo.servingsElement.servingsText.text =
                requireContext().resources.getString(R.string.servings)
        else
            binding.recipeInfo.servingsElement.servingsText.text =
                requireContext().resources.getString(R.string.serving)

        if (adapter != null) {
            adapter!!.multiplier = viewModel.getServingsMultiplier()
            adapter!!.notifyDataSetChanged()
        }
    }

    /**
     * Zooms into image when ScrollView gets moved upwards and the other way round
     */
    private fun onScroll(scrollY: Int) {
        if (scrollY < 1500) { // TODO check on tablet
            val scaleVal = (1 + (scrollY.toFloat() / 9000))
            binding.recipeDetailImage.scaleX = scaleVal
            binding.recipeDetailImage.scaleY = scaleVal
        }
    }

}
