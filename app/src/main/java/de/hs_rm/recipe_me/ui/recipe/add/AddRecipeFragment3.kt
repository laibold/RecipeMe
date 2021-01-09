package de.hs_rm.recipe_me.ui.recipe.add

import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import de.hs_rm.recipe_me.R

import de.hs_rm.recipe_me.databinding.AddRecipeFragment3Binding
import de.hs_rm.recipe_me.model.recipe.CookingStep
import de.hs_rm.recipe_me.model.recipe.TimeUnit

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

        // TODO in ViewModel
        setTimeAdapter(null)

        //TODO remove
        val items = arrayOf(
            CookingStep(
                0,
                "boerex-step1.jpg",
                "Teig ausrollen. Dabei beachten, dass er möglichst dünn ist. Anschließend mit Öl bestreichen",
                0
            ),
            CookingStep(
                1,
                "boerex-step2.jpg",
                "Den Teig dünn mit Spinat bestreichen und zusammenrollen.",
                0
            )
        )

        binding.cookingStepTimeField.doAfterTextChanged { editable ->
            afterTimeTextChanged(editable)
        }

        binding.cookingStepListView.adapter = cookingStepListAdapter(items)

        binding.backButton.setOnClickListener { onBack() }
        binding.nextButton.setOnClickListener { onNext() }

        return binding.root
    }

    /**
     * Format content of time field and check for invalid input,
     * fill time unit spinner (singular/plural) if input is valid
     */
    private fun afterTimeTextChanged(editable: Editable?) {
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

    /**
     * Refill adapter for time spinner with time units in singular or plural depending on number
     * in time field
     */
    private fun setTimeAdapter(number: Int?) {
        val names = TimeUnit.getNumberStringList(resources, number)

        val adapter =
            ArrayAdapter(requireContext(), R.layout.support_simple_spinner_dropdown_item, names)
        binding.cookingStepTimeSpinner.adapter = adapter
    }

    /**
     * @return Adapter for cooking step ListView
     */
    private fun cookingStepListAdapter(items: Array<CookingStep>): CookingStepListAdapter {
        return CookingStepListAdapter(requireContext(), R.layout.cooking_step_listitem, items)
    }

    /**
     * Navigation on back button
     */
    private fun onBack() {
        val direction = AddRecipeFragment3Directions.toAddRecipeFragment2()
        findNavController().navigate(direction)
    }

    /**
     * Navigation on next button
     */
    private fun onNext() {
        //TODO navigate to recipe view
        Toast.makeText(context, "Fertig!", Toast.LENGTH_SHORT).show()
    }

}
