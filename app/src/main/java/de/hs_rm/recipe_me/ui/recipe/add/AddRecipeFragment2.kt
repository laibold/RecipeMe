package de.hs_rm.recipe_me.ui.recipe.add

import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.widget.ListPopupWindow
import androidx.core.widget.doAfterTextChanged
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import de.hs_rm.recipe_me.R
import de.hs_rm.recipe_me.databinding.AddRecipeFragment2Binding
import de.hs_rm.recipe_me.model.recipe.Ingredient
import de.hs_rm.recipe_me.model.recipe.IngredientUnit

@AndroidEntryPoint
class AddRecipeFragment2 : Fragment() {

    private lateinit var binding: AddRecipeFragment2Binding
    private val spinnerHeight = 1000

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.add_recipe_fragment2,
            container,
            false
        )

        //TODO remove
        val items = arrayOf(
            Ingredient(0, "Teig", 200.0, IngredientUnit.GRAM),
            Ingredient(0, "Spinat", 2.5, IngredientUnit.PACKAGE)
        )

        binding.ingredientListView.adapter = ingredientListAdapter(items)
        setUnitAdapter(null)
        setUnitSpinnerPopupHeight(binding.ingredientUnitSpinner)

        binding.ingredientAmountField.doAfterTextChanged { editable ->
            afterAmountTextChanged(editable)
        }

        binding.backButton.setOnClickListener { onBack() }
        binding.nextButton.setOnClickListener { onNext() }

        return binding.root
    }

    /**
     * @return IngredientListAdapter for IngredientListView
     */
    private fun ingredientListAdapter(items: Array<Ingredient>): IngredientListAdapter {
        return IngredientListAdapter(requireContext(), R.layout.ingredient_listitem, items)
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
     * Refill adapter for unit spinner with units in singular or plural depending on amount
     */
    private fun setUnitAdapter(amount: Double?) {
        val names = IngredientUnit.getNumberStringList(resources, amount)

        val adapter =
            ArrayAdapter(requireContext(), R.layout.support_simple_spinner_dropdown_item, names)
        binding.ingredientUnitSpinner.adapter = adapter
    }

    /**
     * Navigation on back button
     */
    private fun onBack() {
        val direction =
            AddRecipeFragment2Directions.actionAddRecipeFragment2ToAddRecipeFragment1()
        findNavController().navigate(direction)
    }

    /**
     * Navigation on next button
     */
    private fun onNext() {
        val direction = AddRecipeFragment2Directions.toAddRecipeFragment3()
        findNavController().navigate(direction)
    }

}
