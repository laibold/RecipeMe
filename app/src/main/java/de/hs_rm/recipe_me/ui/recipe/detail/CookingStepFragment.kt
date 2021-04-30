package de.hs_rm.recipe_me.ui.recipe.detail

import android.content.Intent
import android.os.Bundle
import android.provider.AlarmClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.Observable
import androidx.databinding.ObservableInt
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import de.hs_rm.recipe_me.R
import de.hs_rm.recipe_me.databinding.CookingStepFragmentBinding
import de.hs_rm.recipe_me.declaration.ui.fragments.CookingStepCallbackAdapter
import de.hs_rm.recipe_me.model.recipe.CookingStep
import de.hs_rm.recipe_me.model.relation.RecipeWithRelations
import de.hs_rm.recipe_me.service.PreferenceService
import javax.inject.Inject

@AndroidEntryPoint
class CookingStepFragment : Fragment(), CookingStepCallbackAdapter {

    @Inject
    lateinit var preferenceService: PreferenceService

    private val args: RecipeDetailFragmentArgs by navArgs()
    private val viewModel: RecipeDetailViewModel by activityViewModels()
    private lateinit var binding: CookingStepFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.cooking_step_fragment,
            container,
            false
        )

        if (viewModel.recipe.value == null) {
            viewModel.loadRecipe(args.recipeId)
        }

        viewModel.recipe.observe(viewLifecycleOwner, { value ->
            value?.let { recipeWithRelations ->
                binding.header.headlineText = recipeWithRelations.recipe.name
                setAdapter(recipeWithRelations)
            }
        })

        viewModel.servings.addOnPropertyChangedCallback(object :
            Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(observable: Observable, i: Int) {
                viewModel.recipe.value?.let { setAdapter(it) }
            }
        })

        return binding.root
    }

    /**
     * Set [CookingStepListAdapter] to cookingStepListView
     */
    private fun setAdapter(recipeWithRelations: RecipeWithRelations) {
        val adapter = CookingStepListAdapter(
            requireContext(),
            R.layout.cooking_step_listitem,
            recipeWithRelations.cookingStepsWithIngredients,
            viewModel.getServingsMultiplier(),
            this
        )

        binding.cookingStepListView.adapter = adapter
    }

    /**
     * Start a timer
     * @param seconds time in seconds
     * @param message message displayed on timer and when ending
     */
    private fun startTimer(message: String, seconds: Int) {
        val skipUi = preferenceService.getTimerInBackground(false)

        val intent = Intent(AlarmClock.ACTION_SET_TIMER).apply {
            putExtra(AlarmClock.EXTRA_MESSAGE, message)
            putExtra(AlarmClock.EXTRA_LENGTH, seconds)
            putExtra(AlarmClock.EXTRA_SKIP_UI, skipUi)
        }

        requireContext().startActivity(intent)

        // show snackbar with link to timer (for API > 25) if user set timer to run in background
        if (skipUi) {
            val snackbar =
                Snackbar.make(binding.root, getString(R.string.timer_started), Snackbar.LENGTH_LONG)

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                snackbar.setAction(getString(R.string.open)) {
                    requireContext().startActivity(Intent(AlarmClock.ACTION_SHOW_TIMERS))
                }
            }

            snackbar.show()
        }
    }

    /**
     * On callback from CookingStepListAdapter start the timer
     */
    override fun onCallback(cookingStep: CookingStep) {
        startTimer(cookingStep.text, cookingStep.getTimeInSeconds())
    }

}
