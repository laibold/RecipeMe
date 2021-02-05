package de.hs_rm.recipe_me.ui.recipe.add

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import androidx.core.widget.doAfterTextChanged
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import de.hs_rm.recipe_me.R
import de.hs_rm.recipe_me.databinding.AddCookingStepDialogBinding
import de.hs_rm.recipe_me.declaration.notifyObservers
import de.hs_rm.recipe_me.declaration.ui.focusAndOpenKeyboard
import de.hs_rm.recipe_me.model.recipe.CookingStep
import de.hs_rm.recipe_me.model.recipe.TimeUnit
import de.hs_rm.recipe_me.service.Formatter

class AddCookingStepDialog constructor(
    private val activity: Activity,
    private var recipeViewModel: AddRecipeViewModel,
    private val cookingStep: CookingStep? = null
) : Dialog(activity) {

    lateinit var binding: AddCookingStepDialogBinding
    private val cookingStepViewModel = AddCookingStepViewModel(recipeViewModel.ingredients.value!!)

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

        ////////////////////////////

        setTimeAdapter(null)
        setButtons()
        setIngredientAdapter()

        cookingStepViewModel.ingredients.observe(activity as LifecycleOwner, { list ->
            val checkedItems = list.filter { it.checked }
            if (checkedItems.isNotEmpty()) {
                binding.formContent.ingredientsTextView.text =
                    Formatter.formatIngredientList(activity, checkedItems)
            } else {
                binding.formContent.ingredientsTextView.text =
                    activity.getString(R.string.add_via_button)
            }
        })

        binding.formContent.cookingStepTimeField.doAfterTextChanged { editable ->
            afterTimeTextChanged(editable)
        }

        binding.formContent.editIngredientsButton.setOnClickListener {
            switchToSelectIngredients()
        }

        if (cookingStep != null) {
            // Update
            //TODO set selected ingredients
            binding.formContent.cookingStepField.setText(cookingStep.text)
            if (cookingStep.time != CookingStep.DEFAULT_TIME) {
                binding.formContent.cookingStepTimeField.setText(cookingStep.time.toString())
                binding.formContent.cookingStepTimeSpinner.setSelection(cookingStep.timeUnit.ordinal)
            }
        }

        binding.formContent.cookingStepField.focusAndOpenKeyboard()
    }

    /**
     * Set default actions and names to bottom buttons
     */
    private fun setButtons() {
        if (cookingStep != null) {
            // Update
            binding.addButton.text = activity.resources.getString(R.string.update)
            binding.addButton.setOnClickListener { updateCookingStepAndClose() }
        } else {
            // Add
            binding.addButton.text = activity.resources.getString(R.string.add)
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

        val adapter = IngredientListAdapter(
            activity,
            R.layout.ingredient_listitem,
            cookingStepViewModel.ingredients.value!!
        )
        list.adapter = adapter

        list.setOnItemClickListener { _, _, _, id ->
            val ingredient = cookingStepViewModel.ingredients.value!![id.toInt()]
            ingredient.checked = !ingredient.checked
            adapter.notifyDataSetChanged()
            cookingStepViewModel.ingredients.notifyObservers()
        }
    }

    /**
     * Refill adapter for time spinner with time units in singular or plural depending on number
     * in time field
     */
    private fun setTimeAdapter(number: Int?) {
        val names = TimeUnit.getNumberStringList(activity.resources, number)

        val adapter = ArrayAdapter(context, R.layout.support_simple_spinner_dropdown_item, names)
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
        // TODO save CookingStep and Ingredients
        val success = recipeViewModel.addCookingStep(
            binding.formContent.cookingStepField.text,
            binding.formContent.cookingStepTimeField.text,
            TimeUnit.values()[binding.formContent.cookingStepTimeSpinner.selectedItemPosition]
        )

        if (success) {
            dismiss()
        } else {
            binding.formContent.cookingStepField.error =
                activity.resources.getString(R.string.err_enter_description)
        }
    }

    /**
     * Update cooking step that is already in ViewModel.
     */
    private fun updateCookingStepAndClose() {
        val success = recipeViewModel.updateCookingStep(
            binding.formContent.cookingStepField.text,
            binding.formContent.cookingStepTimeField.text,
            TimeUnit.values()[binding.formContent.cookingStepTimeSpinner.selectedItemPosition]
        )

        if (success) {
            dismiss()
        } else {
            binding.formContent.cookingStepField.error =
                activity.resources.getString(R.string.err_enter_description)
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
            //save
            switchToCookingStepForm()
        }

        binding.cancelButton.text = activity.getString(R.string.back)
        binding.cancelButton.setOnClickListener { switchToCookingStepForm() }
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
