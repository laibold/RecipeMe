package de.hs_rm.recipe_me.ui.recipe.category

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import androidx.databinding.Observable
import androidx.databinding.ObservableBoolean
import androidx.navigation.findNavController
import de.hs_rm.recipe_me.databinding.RecipeListitemBinding
import de.hs_rm.recipe_me.declaration.ui.fragments.DeleteRecipeCallbackAdapter
import de.hs_rm.recipe_me.model.recipe.Recipe
import java.util.*

/**
 * Adapter for View of Recipes by RecipeCategory. Layout: recipe_listitem in ListView
 */
class RecipeListAdapter(
    context: Context,
    private val resource: Int,
    private val objects: List<Recipe>,
    private val callbackListener: DeleteRecipeCallbackAdapter
) :
    ArrayAdapter<Recipe>(context, resource, objects) {

    /**
     * Indicates whether a recipe list item is selected. Other items will observe this value
     * and unset their own selection if value switches to false. This happens when any item is
     * single-clicked (selection disappears completely) or when an item gets long-clicked
     * (selection will "move" to this item).
     */
    var itemSelected = ObservableBoolean(false)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val holder: RecipeViewHolder

        if (convertView == null) {
            val viewBinding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                resource,
                parent,
                false
            ) as RecipeListitemBinding

            holder = RecipeViewHolder(viewBinding)
            holder.view.tag = holder
        } else {
            holder = convertView.tag as RecipeViewHolder
        }

        val recipe = objects[position]
        holder.binding.recipeName.text = recipe.name
        holder.binding.recipeImageView.setImageResource(recipe.category.drawableResId)

        // on long click remove other selection by setting itemSelected and set selection to this item
        holder.binding.itemWrapper.setOnLongClickListener {
            showOverlay(holder.binding)
            return@setOnLongClickListener true
        }

        // remove selection if itemSelected is set to false
        itemSelected.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(observable: Observable, i: Int) {
                val itemSelected = observable as ObservableBoolean
                if (!itemSelected.get()) {
                    hideOverlay(holder.binding)
                }
            }
        })

        // If item is single-clicked remove selection if present, otherwise navigate to recipe
        holder.binding.itemWrapper.setOnClickListener {
            if (itemSelected.get()) {
                itemSelected.set(false)
            } else {
                val direction = RecipeCategoryFragmentDirections.toRecipeDetailFragment(recipe.id)
                it.findNavController().navigate(direction)
            }
        }

        // Call fragment if delete button is clicked. It will remove the recipe and refresh the list
        holder.binding.editOverlay.deleteButton.setOnClickListener {
            callbackListener.onCallback(objects[position])
        }

        return holder.view
    }

    /**
     * Remove other selection, vibrate and show new selection
     */
    private fun showOverlay(binding: RecipeListitemBinding) {
        itemSelected.set(false)
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrator.vibrate(
            VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE)
        )

        itemSelected.set(true)
        binding.editOverlay.overlayWrapper.visibility = View.VISIBLE
    }

    /**
     * Remove selection
     */
    private fun hideOverlay(binding: RecipeListitemBinding) {
        binding.editOverlay.overlayWrapper.visibility = View.GONE
        itemSelected.set(false)
    }

    /**
     * Remove selection overlay
     */
    fun removeSelection() {
        itemSelected.set(false)
    }

    // https://www.spreys.com/view-holder-design-pattern-for-android/
    // https://stackoverflow.com/questions/43973490/how-to-do-android-data-binding-a-customadapter-inherited-from-baseadapter-for-sp
    private class RecipeViewHolder(val binding: RecipeListitemBinding) {
        val view: View = binding.root
    }
}
