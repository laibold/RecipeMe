package de.hs_rm.recipe_me.ui.recipe.add;

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import de.hs_rm.recipe_me.R

import de.hs_rm.recipe_me.databinding.AddRecipeFragment3Binding;
import de.hs_rm.recipe_me.model.recipe.TimeUnit
import de.hs_rm.recipe_me.service.NumberResolver

@AndroidEntryPoint
class AddRecipeFragment3 : Fragment() {

    private lateinit var binding: AddRecipeFragment3Binding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.add_recipe_fragment3,
            container,
            false
        )

        binding.backButton.setOnClickListener {
            val direction = AddRecipeFragment3Directions.toAddRecipeFragment2()
            findNavController().navigate(direction)
        }

        binding.nextButton.setOnClickListener {
            //TODO navigate to recipe view
            Toast.makeText(context, "Fertig!", Toast.LENGTH_SHORT).show()
        }

        // TODO in ViewModel
        setTimeAdapter(null)

        binding.cookingStepTimeField.doAfterTextChanged { editable ->
            // spinner will be reset by refill, so save and set selected item here
            val selectedSpinnerItemId = binding.cookingStepTimeSpinner.selectedItemId

            if (editable != null && !TextUtils.isEmpty(editable)) {
                try {
                    // allow comma as separator
                    val number = editable.toString().replace(',', '.').toInt()
                    setTimeAdapter(number)
                } catch (e: NumberFormatException) {
                    // clear if editable cannot be parsed to double
                    editable.clear()
                }
            }

            binding.cookingStepTimeSpinner.setSelection(selectedSpinnerItemId.toInt())
        }

        return binding.root
    }

    /**
     * Refill adapter for time spinner with time units in singular or plural depending on number
     * in time field
     */
    private fun setTimeAdapter(number: Int?) {
        val names = NumberResolver.getNumberResourceId<TimeUnit>(resources, number)

        val adapter =
            ArrayAdapter(requireContext(), R.layout.support_simple_spinner_dropdown_item, names)
        binding.cookingStepTimeSpinner.adapter = adapter
    }

}
