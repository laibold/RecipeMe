package de.hs_rm.recipe_me.ui.recipe.add

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.Window
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.widget.ListPopupWindow
import androidx.core.widget.doAfterTextChanged
import androidx.databinding.DataBindingUtil
import de.hs_rm.recipe_me.R
import de.hs_rm.recipe_me.databinding.AddIngredientDialogBinding
import de.hs_rm.recipe_me.declaration.ui.focusAndOpenKeyboard
import de.hs_rm.recipe_me.model.recipe.IngredientUnit

class AddIngredientDialog constructor(
    private val activity: Activity,
    private var viewModel: AddRecipeViewModel
) : Dialog(activity) {

    lateinit var binding: AddIngredientDialogBinding
    private val spinnerHeight = 1000

    @SuppressLint("ClickableViewAccessibility") // is handled in dismissOnMotionUp()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        binding = DataBindingUtil.inflate(
            LayoutInflater.from(context), R.layout.add_ingredient_dialog, null, false
        )
        setContentView(binding.root)

        val width = (activity.resources.displayMetrics.widthPixels * 0.90).toInt()
        window?.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT)

        setUnitAdapter(null)
        setUnitSpinnerPopupHeight(binding.ingredientUnitSpinner) //FIXME restrict size

        binding.ingredientQuantityField.doAfterTextChanged { editable ->
            afterAmountTextChanged(editable)
        }

//        viewModel.ingredientSaveAction.observe(viewLifecycleOwner, {
//            if (it == SaveAction.ADD) {
//                binding.addIngredientButton.setOnClickListener { addIngredient() }
//                binding.addIngredientButton.text = resources.getString(R.string.add)
//                adapter?.editingEnabled = true
//                adapter?.notifyDataSetChanged()
//            } else if (it == SaveAction.UPDATE) {
//                binding.addIngredientButton.setOnClickListener { updateIngredient() }
//                binding.addIngredientButton.text = resources.getString(R.string.update)
//                adapter?.editingEnabled = false
//                adapter?.notifyDataSetChanged()
//            }
//        })

        binding.cancelButton.setOnClickListener {
            dismiss()

        }
        binding.addButton.setOnClickListener {
            addIngredientAndClose()
        }

        binding.ingredientQuantityField.focusAndOpenKeyboard()
    }

    /**
     * Refill adapter for unit spinner with units in singular or plural depending on amount
     */
    private fun setUnitAdapter(amount: Double?) {
        val names = IngredientUnit.getNumberStringList(context.resources, amount)

        val adapter = ArrayAdapter(context, R.layout.support_simple_spinner_dropdown_item, names)
        binding.ingredientUnitSpinner.adapter = adapter
    }

    /**
     * Set height of spinner to given pixels
     * https://readyandroid.wordpress.com/2020/04/13/limit-the-height-of-spinner-drop-down-view-android
     */
    private fun setUnitSpinnerPopupHeight(spinner: Spinner) {
        try {
            val popup = Spinner::class.java.getDeclaredField("mPopup")
            popup.isAccessible = true
            val popupWindow: ListPopupWindow = popup.get(spinner) as ListPopupWindow
            popupWindow.height = spinnerHeight
        } catch (ex: Exception) {
            Log.e("AddRecipeFragment2", "Error at resizing spinner popupWindow")
        }
    }

    /**
     * Format content of amount field and check for invalid input,
     * fill unit spinner (singular/plural) if input is valid
     */
    private fun afterAmountTextChanged(editable: Editable?) {
        // spinner will be reset by refill, so save and set selected item here
        val selectedSpinnerItemId = binding.ingredientUnitSpinner.selectedItemId

        if (editable != null && !TextUtils.isEmpty(editable)) {
            try {
                // allow comma as separator
                val amount = editable.toString().replace(',', '.').toDouble()
                setUnitAdapter(amount)
            } catch (e: NumberFormatException) {
                // clear if editable cannot be parsed to double
                editable.clear()
            }
        }

        binding.ingredientUnitSpinner.setSelection(selectedSpinnerItemId.toInt())
    }

    /**
     * Get values from form fields and send them to viewModel to add an Ingredient,
     * clear fields afterwards
     */
    private fun addIngredientAndClose() {
        val success = viewModel.addIngredient(
            binding.ingredientNameField.text,
            binding.ingredientQuantityField.text,
            IngredientUnit.values()[binding.ingredientUnitSpinner.selectedItemPosition]
        )

        if (success) {
            binding.ingredientNameField.text.clear()
            binding.ingredientQuantityField.text.clear()
            binding.ingredientUnitSpinner.setSelection(0)
            dismiss()
        }
    }

}
