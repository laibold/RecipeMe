package de.hs_rm.recipe_me.ui.recipe.add;

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.Window
import android.view.WindowManager
import android.widget.ArrayAdapter
import androidx.core.widget.doAfterTextChanged
import androidx.databinding.DataBindingUtil
import de.hs_rm.recipe_me.R
import de.hs_rm.recipe_me.databinding.AddCookingStepDialogBinding
import de.hs_rm.recipe_me.declaration.ui.focusAndOpenKeyboard
import de.hs_rm.recipe_me.model.recipe.CookingStep
import de.hs_rm.recipe_me.model.recipe.TimeUnit

class AddCookingStepDialog constructor(
    private val activity: Activity,
    private var viewModel: AddRecipeViewModel,
    private val cookingStep: CookingStep? = null
) : Dialog(activity) {

    lateinit var binding: AddCookingStepDialogBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        binding = DataBindingUtil.inflate(
            LayoutInflater.from(context), R.layout.add_cooking_step_dialog, null, false
        )
        setContentView(binding.root)

        // Set width to 90% of screen
        val width = (activity.resources.displayMetrics.widthPixels * 0.90).toInt()
        window?.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT)

        setTimeAdapter(null)

        binding.cookingStepTimeField.doAfterTextChanged { editable ->
            afterTimeTextChanged(editable)
        }

        if (cookingStep != null) {
            // Update
            binding.cookingStepField.setText(cookingStep.text)
            if (cookingStep.time != CookingStep.DEFAULT_TIME) {
                binding.cookingStepTimeField.setText(cookingStep.time.toString())
                binding.cookingStepTimeSpinner.setSelection(cookingStep.timeUnit.ordinal)
            }

            binding.addButton.text = activity.getString(R.string.update)
            binding.addButton.setOnClickListener { updateCookingStepAndClose() }
        } else {
            // Add
            binding.addButton.text = activity.getString(R.string.add)
            binding.addButton.setOnClickListener { addCookingStepAndClose() }
        }

        binding.cancelButton.setOnClickListener { dismiss() }

        binding.cookingStepField.focusAndOpenKeyboard()
    }

    /**
     * Refill adapter for time spinner with time units in singular or plural depending on number
     * in time field
     */
    private fun setTimeAdapter(number: Int?) {
        val names = TimeUnit.getNumberStringList(activity.resources, number)

        val adapter = ArrayAdapter(context, R.layout.support_simple_spinner_dropdown_item, names)
        binding.cookingStepTimeSpinner.adapter = adapter
    }

    /**
     * Format content of time field and check for invalid input,
     * fill time unit spinner (singular/plural) if input is valid
     */
    private fun afterTimeTextChanged(editable: Editable?) {
        // spinner will be reset by refill, so save and set selected item here
        val selectedSpinnerItemId = binding.cookingStepTimeSpinner.selectedItemId

        if (editable != null && editable.isNotBlank()) {
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
     * Add cooking step to ViewModel scope
     */
    private fun addCookingStepAndClose() {
        val success = viewModel.addCookingStep(
            binding.cookingStepField.text,
            binding.cookingStepTimeField.text,
            TimeUnit.values()[binding.cookingStepTimeSpinner.selectedItemPosition]
        )

        if (success) {
            dismiss()
        } else {
            binding.cookingStepField.error = activity.getString(R.string.err_enter_description)
        }
    }

    /**
     * Update cooking step that is already in ViewModel.
     */
    private fun updateCookingStepAndClose() {
        val success = viewModel.updateCookingStep(
            binding.cookingStepField.text,
            binding.cookingStepTimeField.text,
            TimeUnit.values()[binding.cookingStepTimeSpinner.selectedItemPosition]
        )

        if (success) {
            dismiss()
        } else {
            binding.cookingStepField.error = activity.getString(R.string.err_enter_description)
        }
    }

}
