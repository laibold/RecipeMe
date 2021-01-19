package de.hs_rm.recipe_me.ui.recipe.add

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import de.hs_rm.recipe_me.R
import de.hs_rm.recipe_me.databinding.AddIngredientListitemBinding
import de.hs_rm.recipe_me.declaration.EditIngredientAdapter
import de.hs_rm.recipe_me.model.recipe.Ingredient
import de.hs_rm.recipe_me.model.recipe.IngredientUnit
import de.hs_rm.recipe_me.service.Formatter

class AddIngredientListAdapter(
    context: Context,
    private val resource: Int,
    private val objects: MutableList<Ingredient>,
    private val callbackListener: EditIngredientAdapter
) :
    ArrayAdapter<Ingredient>(context, resource, objects) {

    var editingEnabled = true

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val holder: IngredientViewHolder

        if (convertView == null) {
            val viewBinding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                resource,
                parent,
                false
            ) as AddIngredientListitemBinding

            holder = IngredientViewHolder(viewBinding)
            holder.view.tag = holder
        } else {
            holder = convertView.tag as IngredientViewHolder
        }

        val ingredient = objects[position]

        holder.binding.ingredientTextView.text = getIngredientText(ingredient)

        if (editingEnabled) {
            enableButtons(holder, position)
        } else {
            disableButtons(holder)
        }

        return holder.view
    }

    /**
     * Create text for ingredient in form of "(quantity (unit)) name"
     */
    private fun getIngredientText(ingredient: Ingredient): CharSequence {
        return Formatter.formatIngredient(context, ingredient)
    }

    /**
     * Enable remove and edit buttons in each element and set listener
     */
    private fun enableButtons(holder: IngredientViewHolder, position: Int) {
        holder.binding.removeButton.visibility = View.VISIBLE
        holder.binding.removeButton.setOnClickListener { removeObject(position) }

        holder.binding.editButton.visibility = View.VISIBLE
        holder.binding.editButton.setOnClickListener {
            callbackListener.onCallback(objects[position], position)

            // highlight element
            holder.binding.ingredientTextView.setTextColor(
                context.resources.getColor(
                    R.color.dark_red,
                    null
                )
            )
            holder.binding.ingredientTextView.setTypeface(null, Typeface.BOLD)
        }
    }

    /**
     * Disable remove and edit buttons in each element
     */
    private fun disableButtons(holder: IngredientViewHolder) {
        holder.binding.removeButton.visibility = View.GONE
        holder.binding.editButton.visibility = View.GONE
    }

    /**
     * Remove object from list and update list
     */
    private fun removeObject(position: Int) {
        objects.removeAt(position)
        notifyDataSetChanged()
    }

    // https://www.spreys.com/view-holder-design-pattern-for-android/
    // https://stackoverflow.com/questions/43973490/how-to-do-android-data-binding-a-customadapter-inherited-from-baseadapter-for-sp
    private class IngredientViewHolder(val binding: AddIngredientListitemBinding) {
        val view: View = binding.root
    }

}
