package de.hs_rm.recipe_me.ui.recipe.add.cooking_step

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.LifecycleOwner
import de.hs_rm.recipe_me.R
import de.hs_rm.recipe_me.databinding.AddCookingStepDialogBinding
import de.hs_rm.recipe_me.declaration.ui.focusAndOpenKeyboard
import de.hs_rm.recipe_me.model.recipe.CookingStep
import de.hs_rm.recipe_me.model.recipe.TimeUnit
import de.hs_rm.recipe_me.model.relation.CookingStepWithIngredients
import de.hs_rm.recipe_me.service.Formatter
import de.hs_rm.recipe_me.ui.component.CustomDialog
import de.hs_rm.recipe_me.ui.recipe.add.AddRecipeViewModel

class AddCookingStepDialog constructor(
    private val activity: Activity,
    private var recipeViewModel: AddRecipeViewModel,
    private val cookingStepWithIngredients: CookingStepWithIngredients? = null
) : CustomDialog<AddCookingStepDialogBinding>(activity, R.layout.add_cooking_step_dialog) {

    private val cookingStepViewModel = AddCookingStepViewModel()
    private lateinit var adapter: CookingStepIngredientListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setTimeAdapter(null)
        setButtons()
        setIngredientAdapter()

        cookingStepViewModel.assignedIngredients.observe(
            activity as LifecycleOwner, { ingredients ->
                if (ingredients.isNotEmpty()) {
                    binding.formContent.ingredientsTextView.text =
                        Formatter.formatIngredientList(activity, ingredients)
                } else {
                    binding.formContent.ingredientsTextView.text =
                        activity.getString(R.string.add_via_button)
                }
            }
        )

        binding.formContent.cookingStepTimeField.doAfterTextChanged { editable ->
            afterTimeTextChanged(editable)
        }

        binding.formContent.editIngredientsButton.setOnClickListener {
            switchToSelectIngredients()
        }

        if (cookingStepWithIngredients != null) {
            // Cooking Step has been reached in and should be updated, set values to fields
            cookingStepViewModel.addAssignedIngredients(cookingStepWithIngredients.ingredients)

            binding.formContent.cookingStepField.setText(cookingStepWithIngredients.cookingStep.text)
            if (cookingStepWithIngredients.cookingStep.time != CookingStep.DEFAULT_TIME) {
                binding.formContent.cookingStepTimeField.setText(cookingStepWithIngredients.cookingStep.time.toString())
                binding.formContent.cookingStepTimeSpinner.setSelection(cookingStepWithIngredients.cookingStep.timeUnit.ordinal)
            }
        }

        binding.formContent.cookingStepField.focusAndOpenKeyboard()
    }

    /**
     * Set default actions and names to bottom buttons
     */
    private fun setButtons() {
        if (cookingStepWithIngredients != null) {
            // Update
            binding.addButton.text = activity.getString(R.string.update)
            binding.addButton.setOnClickListener { updateCookingStepAndClose() }
        } else {
            // Add
            binding.addButton.text = activity.getString(R.string.add)
            binding.addButton.setOnClickListener { addCookingStepAndClose() }
        }

        binding.cancelButton.text = activity.getString(R.string.cancel)
        binding.cancelButton.setOnClickListener { dismiss() }
    }

    /**
     * Set Ingredients to ListView
     */
    private fun setIngredientAdapter() {
        val list = binding.ingredientListContent.ingredientsListView

        adapter = CookingStepIngredientListAdapter(
            activity,
            R.layout.ingredient_listitem,
            recipeViewModel.ingredients.value!!,
            cookingStepViewModel.assignedIngredients.value!!
        )
        list.adapter = adapter

        list.setOnItemClickListener { _, _, _, id ->
            val ingredient = recipeViewModel.ingredients.value!![id.toInt()]
            cookingStepViewModel.toggleCheckedState(ingredient)
            adapter.notifyDataSetChanged()
        }
    }

    /**
     * Refill adapter for time spinner with time units in singular or plural depending on number
     * in time field
     */
    private fun setTimeAdapter(number: Int?) {
        val names = TimeUnit.getNumberStringList(activity.resources, number)

        val adapter = ArrayAdapter(context, R.layout.spinner_item, names)
        binding.formContent.cookingStepTimeSpinner.adapter = adapter
    }

    /**
     * Format content of time field and check for invalid input,
     * fill time unit spinner (singular/plural) if input is valid
     */
    private fun afterTimeTextChanged(editable: Editable?) {
        // spinner will be reset by refill, so save and set selected item here
        val selectedSpinnerItemId = binding.formContent.cookingStepTimeSpinner.selectedItemId

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

        binding.formContent.cookingStepTimeSpinner.setSelection(selectedSpinnerItemId.toInt())
    }

    /**
     * Add cooking step to ViewModel scope
     */
    private fun addCookingStepAndClose() {
        val success = recipeViewModel.addCookingStepWithIngredients(
            binding.formContent.cookingStepField.text,
            binding.formContent.cookingStepTimeField.text,
            TimeUnit.values()[binding.formContent.cookingStepTimeSpinner.selectedItemPosition],
            cookingStepViewModel.assignedIngredients.value!!
        )

        if (success) {
            dismiss()
        } else {
            binding.formContent.cookingStepField.error =
                activity.getString(R.string.err_enter_description)
        }
    }

    /**
     * Update cooking step that is already in ViewModel.
     */
    private fun updateCookingStepAndClose() {
        val success = recipeViewModel.updateCookingStepWithIngredients(
            binding.formContent.cookingStepField.text,
            binding.formContent.cookingStepTimeField.text,
            TimeUnit.values()[binding.formContent.cookingStepTimeSpinner.selectedItemPosition],
            cookingStepViewModel.assignedIngredients.value!!
        )

        if (success) {
            dismiss()
        } else {
            binding.formContent.cookingStepField.error =
                activity.getString(R.string.err_enter_description)
        }
    }

    /**
     * Switch view to ingredient list and set button actions and names
     */
    private fun switchToSelectIngredients() {
        // close keyboard
        val imm: InputMethodManager =
            activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.root.windowToken, 0)

        binding.formContent.root.visibility = View.GONE
        binding.ingredientListContent.root.visibility = View.VISIBLE

        binding.addButton.text = activity.getString(R.string.save)
        binding.addButton.setOnClickListener {
            switchToCookingStepForm()
        }

        binding.cancelButton.text = activity.getString(R.string.cancel_reset)
        binding.cancelButton.setOnClickListener {
            switchToCookingStepForm()
            cookingStepViewModel.resetCheckedStates()
            adapter.notifyDataSetChanged()
        }
    }

    /**
     * Switch to default cooking step view and reset button values
     */
    private fun switchToCookingStepForm() {
        binding.formContent.root.visibility = View.VISIBLE
        binding.ingredientListContent.root.visibility = View.GONE

        setButtons()
    }
}
