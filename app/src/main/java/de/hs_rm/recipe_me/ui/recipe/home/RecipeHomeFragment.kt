package de.hs_rm.recipe_me.ui.recipe.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import de.hs_rm.recipe_me.R
import de.hs_rm.recipe_me.databinding.RecipeHomeFragmentBinding
import de.hs_rm.recipe_me.model.recipe.Recipe
import de.hs_rm.recipe_me.model.recipe.RecipeCategory

@AndroidEntryPoint
class RecipeHomeFragment : Fragment() {

    private lateinit var binding: RecipeHomeFragmentBinding
    private val viewModel: RecipeHomeViewModel by viewModels()

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

        setAdapter()

        viewModel.loadRecipeOfTheDay()

        // Dispatch Touch event to underlying wrapper.
        // Subtract scroll position, because dummy view will move, but wrapper won't
        binding.dummyView.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    event.offsetLocation(
                        (-binding.scrollView.scrollX).toFloat(),
                        (-binding.scrollView.scrollY).toFloat()
                    )
                    binding.recipeOfTheDayWrapper.dispatchTouchEvent(event)
                }
                MotionEvent.ACTION_UP -> {
                    view.performClick()
                }
            }
            true
        }

        viewModel.recipeOfTheDay.observe(viewLifecycleOwner, { recipe ->
            onRecipeOfTheDayChanged(recipe)
        })

        binding.scrollView.setOnScrollChangeListener { _, _, scrollY, _, _ ->
            onScroll(scrollY)
        }

        binding.addButton.setOnClickListener {
            val direction = RecipeHomeFragmentDirections.toAddRecipeNavGraph(clearValues = true)
            findNavController().navigate(direction)
        }

        return binding.root
    }

    /**
     * When recipe of the day changes, set text, image and clicklistener for navigation.
     * If there's no recipe available, set default image and text.
     */
    private fun onRecipeOfTheDayChanged(recipe: Recipe?) {
        if (recipe == null) {
            binding.recipeOfTheDayName.text = resources.getString(R.string.no_recipe_otd)
            binding.recipeOfTheDayButton.visibility = View.GONE
            binding.recipeOfTheDayImage.setImageResource(R.drawable.cooking_default)
            binding.gradientOverlay.setBackgroundResource(R.drawable.gradient_overlay)
        } else {
            binding.recipeOfTheDayName.text = recipe.name
            binding.recipeOfTheDayImage.setImageResource(recipe.category.drawableResId)

            // Touch event gets dispatched from ScrollViews dummy view
            binding.recipeOfTheDayButton.setOnTouchListener { view, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        val direction =
                            RecipeHomeFragmentDirections.toRecipeDetailFragment(recipe.id, true)
                        findNavController().navigate(direction)
                    }
                    MotionEvent.ACTION_UP -> {
                        view.performClick()
                    }
                }
                true
            }
        }
    }

    /**
     * Set categories to CategoryListAdapter
     */
    private fun setAdapter() {
        val list = binding.homeScrollview.list
        val categories = RecipeCategory.values()
        list.adapter = CategoryListAdapter(requireContext(), R.layout.category_listitem, categories)
    }

    /**
     * Zooms into image when ScrollView gets moved upwards and the other way round
     */
    private fun onScroll(scrollY: Int) {
        if (scrollY < 1100) {
            val scaleVal = (1 + (scrollY.toFloat() / 9000))
            binding.recipeOfTheDayImage.scaleX = scaleVal
            binding.recipeOfTheDayImage.scaleY = scaleVal
        }
    }

}
