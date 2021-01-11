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
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import de.hs_rm.recipe_me.R

import de.hs_rm.recipe_me.databinding.AddRecipeFragment3Binding
import de.hs_rm.recipe_me.declaration.CallbackAdapter
import de.hs_rm.recipe_me.model.SaveAction
import de.hs_rm.recipe_me.model.recipe.CookingStep
import de.hs_rm.recipe_me.model.recipe.TimeUnit

@AndroidEntryPoint
class AddRecipeFragment3 : Fragment(), CallbackAdapter {

    private lateinit var binding: AddRecipeFragment3Binding
    private val viewModel: AddRecipeViewModel by activityViewModels()
    private var adapter: AddCookingStepListAdapter? = null

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

        viewModel.cookingSteps.observe(viewLifecycleOwner, {
            adapter = viewModel.cookingSteps.value?.let { list -> cookingStepListAdapter(list) }
            binding.cookingStepListView.adapter = adapter

            adapter?.notifyDataSetChanged()
        })

        setTimeAdapter(null)

        binding.cookingStepTimeField.doAfterTextChanged { editable ->
            afterTimeTextChanged(editable)
        }

        viewModel.saveAction.value = SaveAction.ADD
        viewModel.saveAction.observe(viewLifecycleOwner, {
            if (it == SaveAction.ADD) {
                binding.addCookingStepButton.setOnClickListener { addCookingStep() }
                binding.addCookingStepButton.text = resources.getString(R.string.add)
                adapter?.editingEnabled = true
                adapter?.notifyDataSetChanged()
            } else if (it == SaveAction.UPDATE) {
                binding.addCookingStepButton.setOnClickListener { updateCookingStep() }
                binding.addCookingStepButton.text = resources.getString(R.string.update)
                adapter?.editingEnabled = false
                adapter?.notifyDataSetChanged()
            }
        })

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
     * @return CookingStepListAdapter for CookingStepListView
     */
    private fun cookingStepListAdapter(items: MutableList<CookingStep>): AddCookingStepListAdapter {
        return AddCookingStepListAdapter(requireContext(), R.layout.add_cooking_step_listitem, items, this)
    }

    /**
     * Add cooking step to ViewModel scope
     */
    private fun addCookingStep() {
        val success = viewModel.addCookingStep(
            binding.cookingStepField.text.toString(),
            binding.cookingStepTimeField.text.toString(),
            TimeUnit.values()[binding.cookingStepTimeSpinner.selectedItemPosition]
        )

        if (success) {
            binding.cookingStepField.text.clear()
            binding.cookingStepTimeField.text.clear()
            binding.cookingStepTimeSpinner.setSelection(0)
        }
    }

    private fun updateCookingStep() {
        val success = viewModel.updateCookingStep(
            binding.cookingStepField.text.toString(),
            binding.cookingStepTimeField.text.toString(),
            TimeUnit.values()[binding.cookingStepTimeSpinner.selectedItemPosition]
        )

        if (success) {
            binding.cookingStepField.text.clear()
            binding.cookingStepTimeField.text.clear()
            binding.cookingStepTimeSpinner.setSelection(0)
        }
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
        viewModel.persistEntities()
        Toast.makeText(context, "Fertig!", Toast.LENGTH_SHORT).show()
    }

    /**
     * Gets called from Adapter when edit was pressed for a CookingStep item
     */
    override fun onCallback(cookingStep: CookingStep, position: Int) {
        binding.cookingStepField.setText(cookingStep.text)
        if (cookingStep.timeUnit != TimeUnit.NONE) {
            binding.cookingStepTimeField.setText(cookingStep.time.toString())
            binding.cookingStepTimeSpinner.setSelection(cookingStep.timeUnit.ordinal)
        }
        viewModel.prepareCookingStepUpdate(position)
        //TODO highlight item
    }

}
