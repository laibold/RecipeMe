package de.hs_rm.recipe_me.ui.recipe.add

import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListPopupWindow
import android.widget.Spinner
import androidx.core.widget.doAfterTextChanged
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import de.hs_rm.recipe_me.R
import de.hs_rm.recipe_me.databinding.AddRecipeFragment2Binding
import de.hs_rm.recipe_me.declaration.EditIngredientAdapter
import de.hs_rm.recipe_me.declaration.closeKeyboard
import de.hs_rm.recipe_me.model.SaveAction
import de.hs_rm.recipe_me.model.recipe.Ingredient
import de.hs_rm.recipe_me.model.recipe.IngredientUnit
import de.hs_rm.recipe_me.service.Formatter

@AndroidEntryPoint
class AddRecipeFragment2 : Fragment(), EditIngredientAdapter {

    private lateinit var binding: AddRecipeFragment2Binding
    private val spinnerHeight = 1000
    private val viewModel: AddRecipeViewModel by activityViewModels()
    private var adapter: AddIngredientListAdapter? = null

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

        viewModel.ingredientSaveAction.value = SaveAction.ADD
        viewModel.ingredients.observe(viewLifecycleOwner, {
            adapter = viewModel.ingredients.value?.let { list -> ingredientListAdapter(list) }
            binding.ingredientsListView.adapter = adapter
            adapter?.notifyDataSetChanged()
        })

        setUnitAdapter(null)
        setUnitSpinnerPopupHeight(binding.ingredientUnitSpinner) //FIXME restrict size

        binding.ingredientQuantityField.doAfterTextChanged { editable ->
            afterAmountTextChanged(editable)
        }

        binding.addIngredientButton.setOnClickListener { addIngredient() }

        viewModel.ingredientSaveAction.observe(viewLifecycleOwner, {
            if (it == SaveAction.ADD) {
                binding.addIngredientButton.setOnClickListener { addIngredient() }
                binding.addIngredientButton.text = resources.getString(R.string.add)
                adapter?.editingEnabled = true
                adapter?.notifyDataSetChanged()
            } else if (it == SaveAction.UPDATE) {
                binding.addIngredientButton.setOnClickListener { updateIngredient() }
                binding.addIngredientButton.text = resources.getString(R.string.update)
                adapter?.editingEnabled = false
                adapter?.notifyDataSetChanged()
            }
        })

        binding.backButton.setOnClickListener { onBack() }
        binding.nextButton.setOnClickListener { onNext() }

        return binding.root
    }

    /**
     * @return IngredientListAdapter for IngredientListView
     */
    private fun ingredientListAdapter(items: MutableList<Ingredient>): AddIngredientListAdapter {
        return AddIngredientListAdapter(
            requireContext(),
            R.layout.add_ingredient_listitem,
            items,
            this
        )
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
     * Get values from form fields and send them to viewModel to add an [Ingredient],
     * clear fields afterwards
     */
    private fun addIngredient() {
        val success = viewModel.addIngredient(
            binding.ingredientNameField.text.toString(),
            binding.ingredientQuantityField.text.toString(),
            IngredientUnit.values()[binding.ingredientUnitSpinner.selectedItemPosition]
        )

        if (success) {
            binding.ingredientNameField.text.clear()
            binding.ingredientQuantityField.text.clear()
            binding.ingredientUnitSpinner.setSelection(0)
            activity.closeKeyboard()
        }
    }

    /**
     * Update ingredient that is already in ViewModel. Clear form if updating succeeds
     */
    private fun updateIngredient() {
        val success = viewModel.updateIngredient(
            binding.ingredientNameField.text.toString(),
            binding.ingredientQuantityField.text.toString(),
            IngredientUnit.values()[binding.ingredientUnitSpinner.selectedItemPosition]
        )

        if (success) {
            binding.ingredientNameField.text.clear()
            binding.ingredientQuantityField.text.clear()
            binding.ingredientUnitSpinner.setSelection(0)
            activity.closeKeyboard()
        }
    }

    /**
     * Navigation on back button
     */
    private fun onBack() {
        val direction = AddRecipeFragment2Directions.toAddRecipeFragment1()
        findNavController().navigate(direction)
    }

    /**
     * Navigation on next button
     */
    private fun onNext() {
        val validationOk = validate()

        if (validationOk) {
            val direction = AddRecipeFragment2Directions.toAddRecipeFragment3()
            findNavController().navigate(direction)
        }
    }

    /**
     * Validate ingredients
     * @return true if all fields are valid
     */
    private fun validate(): Boolean {
        val ingredientsValid = viewModel.validateIngredients()
        if (ingredientsValid != 0) {
            binding.ingredientNameField.error =
                requireContext().resources.getString(ingredientsValid)
        }
        return ingredientsValid == 0
    }

    /**
     * Gets called from Adapter when edit was pressed for an Ingredient item
     */
    override fun onCallback(ingredient: Ingredient, position: Int) {
        binding.ingredientNameField.setText(ingredient.name)
        if (ingredient.quantity != Ingredient.DEFAULT_QUANTITY) {
            binding.ingredientQuantityField.setText(Formatter.formatIngredientQuantity(ingredient.quantity))
            binding.ingredientUnitSpinner.setSelection(ingredient.unit.ordinal)
        }
        viewModel.prepareIngredientUpdate(position)
    }

}
