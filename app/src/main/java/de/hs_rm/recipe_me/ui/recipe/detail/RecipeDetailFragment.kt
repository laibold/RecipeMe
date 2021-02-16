package de.hs_rm.recipe_me.ui.recipe.detail

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.databinding.DataBindingUtil
import androidx.databinding.Observable
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableInt
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import de.hs_rm.recipe_me.R
import de.hs_rm.recipe_me.databinding.RecipeDetailFragmentBinding
import de.hs_rm.recipe_me.model.relation.RecipeWithRelations
import de.hs_rm.recipe_me.service.ImageHandler

@AndroidEntryPoint
class RecipeDetailFragment : Fragment() {

    private lateinit var binding: RecipeDetailFragmentBinding
    private val args: RecipeDetailFragmentArgs by navArgs()
    private val viewModel: RecipeDetailViewModel by activityViewModels()
    private var adapter: IngredientListAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set action to back button: always navigate to CategoryFragment
        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    onBackPressed()
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.recipe_detail_fragment,
            container,
            false
        )

        val recipeId = args.recipeId
        viewModel.loadRecipe(recipeId)

        // Disable right padding for headline
        binding.recipeDetailName.headline.setPadding(
            binding.recipeDetailName.headline.paddingLeft,
            binding.recipeDetailName.headline.paddingTop,
            0,
            binding.recipeDetailName.headline.paddingBottom,
        )

        // Dispatch touch events from dummyView to topElementsWrapper
        // Subtract scroll position, because dummy view will move, but wrapper won't
        binding.dummyView.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    event.offsetLocation(
                        (-binding.scrollView.scrollX).toFloat(),
                        (-binding.scrollView.scrollY).toFloat()
                    )
                    binding.topElementsWrapper.dispatchTouchEvent(event)
                }
                MotionEvent.ACTION_UP -> {
                    view.performClick()
                }
            }
            true
        }

        // Navigate to edit recipe
        binding.editRecipeButton.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    val direction = RecipeDetailFragmentDirections.toAddRecipeNavGraph(
                        recipeId = viewModel.recipe.value?.recipe!!.id,
                        clearValues = true
                    )
                    view.findNavController().navigate(direction)
                }
                MotionEvent.ACTION_UP -> {
                    view.performClick()
                }
            }
            true
        }

        viewModel.recipe.observe(viewLifecycleOwner, { recipeWithRelations ->
            if (recipeWithRelations != null) {
                onRecipeChanged(recipeWithRelations)
                viewModel.servings.set(recipeWithRelations.recipe.servings)
            } else {
                Toast.makeText(context, getString(R.string.err_recipe_not_found), Toast.LENGTH_LONG)
                    .show()
                onBackPressed()
            }
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
            viewModel.ingredientSelectionActive.set(true)
        }

        binding.recipeInfo.toShoppingListAcceptButton.setOnClickListener {
            viewModel.addSelectedIngredientsToShoppingList()
            closeIngredientSelection()
        }

        binding.recipeInfo.toShoppingListCancelButton.setOnClickListener {
            closeIngredientSelection()
        }

        viewModel.ingredientSelectionActive.addOnPropertyChangedCallback(object :
            Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(observable: Observable, i: Int) {
                val active = observable as ObservableBoolean
                if (active.get()) {
                    setIngredientSelectionActive()
                } else {
                    setIngredientSelectionInactive()
                }
            }
        })

        binding.forwardButton.setOnClickListener {
            val direction = RecipeDetailFragmentDirections.toCookingStepFragment()
            findNavController().navigate(direction)
        }

        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.servings.set(RecipeDetailViewModel.NOT_INITIALIZED)
    }

    /**
     * Set recipe name, servings, ingredients and cooking steps to view
     * Hide cooking step elements when recipe has no cooking steps
     */
    private fun onRecipeChanged(recipeWithRelations: RecipeWithRelations) {
        binding.recipeDetailName.headlineText = recipeWithRelations.recipe.name
        setIngredientAdapter(recipeWithRelations)
        if (viewModel.servings.get() == RecipeDetailViewModel.NOT_INITIALIZED) {
            viewModel.servings.set(recipeWithRelations.recipe.servings)
        } else {
            onServingsChanged(viewModel.servings.get())
        }

        if (recipeWithRelations.cookingStepsWithIngredients.isEmpty()) {
            binding.forwardButton.visibility = View.GONE
            binding.recipeInfo.cookingStepsHeadline.visibility = View.GONE
        } else {
            setCookingSteps(recipeWithRelations)
        }
        setImage(recipeWithRelations)
        binding.recipeInfo.wrapper.visibility = View.VISIBLE
    }

    /**
     * Set background image
     */
    private fun setImage(recipeWithRelations: RecipeWithRelations) {
        val bitmap = ImageHandler.getRecipeImage(requireContext(), recipeWithRelations.recipe)
        binding.recipeDetailImage.setImageBitmap(bitmap)
    }

    /**
     * Set Ingredients to ListView
     */
    private fun setIngredientAdapter(recipeWithRelations: RecipeWithRelations) {
        val list = binding.recipeInfo.ingredientsListView
        adapter = IngredientListAdapter(
            requireContext(),
            R.layout.ingredient_listitem,
            recipeWithRelations.ingredients,
            viewModel.ingredientSelectionActive
        )
        list.adapter = adapter

        list.setOnItemClickListener { _, _, _, id ->
            if (viewModel.ingredientSelectionActive.get()) {
                val ingredient = viewModel.recipe.value!!.ingredients[id.toInt()]
                ingredient.checked = !ingredient.checked
                adapter!!.notifyDataSetChanged()
            }
        }
    }

    /**
     * Set CookingSteps to TextView
     */
    private fun setCookingSteps(recipeWithRelations: RecipeWithRelations) {
        var allCookingSteps = ""
        for (cookingStep in recipeWithRelations.cookingStepsWithIngredients)
            allCookingSteps += cookingStep.cookingStep.text + "\n\n"
        binding.recipeInfo.steps.text = allCookingSteps
    }

    /**
     * Set text of servings element and refresh ListView
     */
    private fun onServingsChanged(servings: Int) {
        binding.recipeInfo.servingsElement.servingsSize.text = servings.toString()

        if (servings > 1)
            binding.recipeInfo.servingsElement.servingsText.text = getString(R.string.servings)
        else
            binding.recipeInfo.servingsElement.servingsText.text = getString(R.string.serving)

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

    /**
     * Hide all selection elements and re-show the add-to-recipe button
     */
    private fun closeIngredientSelection() {
        viewModel.ingredientSelectionActive.set(false)
        viewModel.clearSelections()
        adapter?.notifyDataSetChanged()
    }

    /**
     * Hide add-to-recipe button and show accept and cancel buttons
     */
    private fun setIngredientSelectionActive() {
        binding.recipeInfo.addToShoppingListButton.visibility = View.GONE
        binding.recipeInfo.toShoppingListAcceptButton.visibility = View.VISIBLE
        binding.recipeInfo.toShoppingListCancelButton.visibility = View.VISIBLE
    }

    /**
     * Show add-to-recipe button and hide accept and cancel buttons
     */
    private fun setIngredientSelectionInactive() {
        binding.recipeInfo.addToShoppingListButton.visibility = View.VISIBLE
        binding.recipeInfo.toShoppingListAcceptButton.visibility = View.GONE
        binding.recipeInfo.toShoppingListCancelButton.visibility = View.GONE
    }

    private fun onBackPressed() {
        // to prevent servingsElement from showing -1
        binding.recipeInfo.wrapper.visibility = View.GONE
        viewModel.servings.set(RecipeDetailViewModel.NOT_INITIALIZED)
        val direction = if (args.navigateBackToHome || viewModel.recipe.value == null) {
            RecipeDetailFragmentDirections.toRecipeHomeFragment()
        } else {
            RecipeDetailFragmentDirections.toRecipeCategoryFragment(viewModel.recipe.value!!.recipe.category)
        }
        findNavController().navigate(direction)
    }

}
