package de.hs_rm.recipe_me.ui.recipe.add.ingredient

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import de.hs_rm.recipe_me.databinding.AddIngredientListitemBinding
import de.hs_rm.recipe_me.declaration.ui.fragments.EditIngredientAdapter
import de.hs_rm.recipe_me.model.recipe.Ingredient
import de.hs_rm.recipe_me.service.Formatter

class AddIngredientListAdapter(
    context: Context,
    private val resource: Int,
    private val objects: MutableList<Ingredient>,
    private val callbackListener: EditIngredientAdapter
) : ArrayAdapter<Ingredient>(context, resource, objects) {

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

        holder.binding.removeButton.setOnClickListener {
            removeObject(position)
        }

        holder.binding.editButton.setOnClickListener {
            callbackListener.onCallback(objects[position], position)
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
