package de.hs_rm.recipe_me.ui.recipe.detail

import android.content.Intent
import android.os.Bundle
import android.provider.AlarmClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import dagger.hilt.android.AndroidEntryPoint
import de.hs_rm.recipe_me.R
import de.hs_rm.recipe_me.databinding.CookingStepFragmentBinding
import de.hs_rm.recipe_me.declaration.ui.fragments.CookingStepCallbackAdapter
import de.hs_rm.recipe_me.model.recipe.CookingStep

@AndroidEntryPoint
class CookingStepFragment : Fragment(), CookingStepCallbackAdapter {

    private val viewModel: RecipeDetailViewModel by activityViewModels()
    private lateinit var binding: CookingStepFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.cooking_step_fragment,
            container,
            false
        )

        binding.recipeNameHeadline.text = viewModel.recipe.value!!.recipe.name

        setAdapter()

        return binding.root
    }

    /**
     * Set [CookingStepListAdapter] to cookingStepListView
     */
    private fun setAdapter() {
        val adapter = CookingStepListAdapter(
            requireContext(),
            R.layout.cooking_step_listitem,
            viewModel.recipe.value!!.cookingSteps,
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
        val intent = Intent(AlarmClock.ACTION_SET_TIMER).apply {
            putExtra(AlarmClock.EXTRA_MESSAGE, message)
            putExtra(AlarmClock.EXTRA_LENGTH, seconds)
            putExtra(AlarmClock.EXTRA_SKIP_UI, false)
        }
        if (context?.let { intent.resolveActivity(it.packageManager) } != null) {
            requireContext().startActivity(intent)
        }
    }

    /**
     * On callback from CookingStepListAdapter start the timer
     */
    override fun onCallback(cookingStep: CookingStep) {
        startTimer(cookingStep.text, cookingStep.getTimeInSeconds())
    }

}
