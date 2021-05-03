package de.hs_rm.recipe_me.ui.recipe.add.ingredient

import android.app.Activity
import android.os.Bundle
import android.text.Editable
import android.widget.ArrayAdapter
import androidx.core.widget.doAfterTextChanged
import de.hs_rm.recipe_me.R
import de.hs_rm.recipe_me.databinding.AddIngredientDialogBinding
import de.hs_rm.recipe_me.declaration.ui.focusAndOpenKeyboard
import de.hs_rm.recipe_me.model.recipe.Ingredient
import de.hs_rm.recipe_me.model.recipe.IngredientUnit
import de.hs_rm.recipe_me.service.Formatter
import de.hs_rm.recipe_me.ui.component.CustomDialog
import de.hs_rm.recipe_me.ui.recipe.add.AddRecipeViewModel
import java.text.DecimalFormatSymbols
import java.util.*

class AddIngredientDialog constructor(
    private val activity: Activity,
    private var viewModel: AddRecipeViewModel,
    private val ingredient: Ingredient? = null
) : CustomDialog<AddIngredientDialogBinding>(activity, R.layout.add_ingredient_dialog) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setUnitAdapter(null)

        // Format quantity text
        binding.ingredientQuantityField.doAfterTextChanged { editable ->
            afterQuantityTextChanged(editable)
        }

        if (ingredient != null) {
            // Update
            binding.ingredientNameField.setText(ingredient.name)

            if (ingredient.quantity != Ingredient.DEFAULT_QUANTITY) {
                binding.ingredientQuantityField.setText(
                    Formatter.formatIngredientQuantity(ingredient.quantity)
                )
                binding.ingredientUnitSpinner.setSelection(ingredient.unit.ordinal)
            }

            binding.addButton.text = activity.getString(R.string.update)
            binding.addButton.setOnClickListener { updateIngredientAndClose() }
        } else {
            // Add
            binding.addButton.text = activity.getString(R.string.add)
            binding.addButton.setOnClickListener { addIngredientAndClose() }
        }

        binding.cancelButton.setOnClickListener { dismiss() }

        binding.ingredientQuantityField.focusAndOpenKeyboard()
    }

    /**
     * Refill adapter for unit spinner with units in singular or plural depending on amount
     */
    private fun setUnitAdapter(amount: Double?) {
        val names = IngredientUnit.getNumberStringList(context.resources, amount)

        val adapter = ArrayAdapter(context, R.layout.spinner_item, names)
        binding.ingredientUnitSpinner.adapter = adapter
    }

    /**
     * Format content of amount field and check for invalid input,
     * fill unit spinner (singular/plural) if input is valid
     * Ignore formatting if locale decimal separator is '.' and editable is '.'
     */
    private fun afterQuantityTextChanged(editable: Editable?) {
        // spinner will be reset by refill, so save and set selected item here
        val selectedSpinnerItemId = binding.ingredientUnitSpinner.selectedItemId

        if (editable != null && editable.isNotBlank()) {
            val separator = DecimalFormatSymbols(Locale.getDefault()).decimalSeparator
            if (!(separator == '.' && editable.toString() == ".")) {
                try {
                    // allow comma as separator
                    val amount = editable.toString().replace(',', '.').toDouble()
                    setUnitAdapter(amount)
                } catch (e: NumberFormatException) {
                    // clear if editable cannot be parsed to double
                    editable.clear()
                }
            }
        }

        binding.ingredientUnitSpinner.setSelection(selectedSpinnerItemId.toInt())
    }

    /**
     * Get values from form fields and send them to viewModel to add an Ingredient,
     * dismiss on success
     */
    private fun addIngredientAndClose() {
        val success = viewModel.addIngredient(
            binding.ingredientNameField.text,
            binding.ingredientQuantityField.text,
            IngredientUnit.values()[binding.ingredientUnitSpinner.selectedItemPosition]
        )

        if (success) {
            dismiss()
        } else {
            binding.ingredientNameField.error = activity.getString(R.string.err_enter_name)
        }
    }

    /**
     * Get values from form fields and send them to viewModel to update an Ingredient,
     * dismiss on success
     */
    private fun updateIngredientAndClose() {
        val success = viewModel.updateIngredient(
            binding.ingredientNameField.text,
            binding.ingredientQuantityField.text,
            IngredientUnit.values()[binding.ingredientUnitSpinner.selectedItemPosition]
        )

        if (success) {
            dismiss()
        } else {
            binding.ingredientNameField.error = activity.getString(R.string.err_enter_name)
        }
    }

}
