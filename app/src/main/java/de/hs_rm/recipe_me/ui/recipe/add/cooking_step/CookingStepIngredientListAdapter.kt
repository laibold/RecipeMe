package de.hs_rm.recipe_me.ui.recipe.add.cooking_step

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import de.hs_rm.recipe_me.databinding.IngredientListitemBinding
import de.hs_rm.recipe_me.model.recipe.Ingredient
import de.hs_rm.recipe_me.service.Formatter

/**
 * Adapter for Ingredients in AddCookingStepDialog
 */
class CookingStepIngredientListAdapter(
    context: Context,
    private val resource: Int,
    private val objects: List<Ingredient>,
    private val objectsToCheck: List<Ingredient>
) :
    ArrayAdapter<Ingredient>(context, resource, objects) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val holder: IngredientViewHolder

        if (convertView == null) {
            val viewBinding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                resource,
                parent,
                false
            ) as IngredientListitemBinding

            holder = IngredientViewHolder(viewBinding)
            holder.view.tag = holder
        } else {
            holder = convertView.tag as IngredientViewHolder
        }

        val ingredient = objects[position]

        holder.binding.itemCheckbox.visibility = View.VISIBLE
        holder.binding.ingredientTextView.text = getIngredientText(ingredient)
        holder.binding.itemCheckbox.isChecked = objectsToCheck.contains(ingredient)

        return holder.view
    }

    /**
     * Create text for ingredient in form of "(quantity unit) name"
     */
    private fun getIngredientText(ingredient: Ingredient): CharSequence {
        return Formatter.formatIngredient(context, ingredient, 1.0)
    }

    // https://www.spreys.com/view-holder-design-pattern-for-android/
    // https://stackoverflow.com/questions/43973490/how-to-do-android-data-binding-a-customadapter-inherited-from-baseadapter-for-sp
    private class IngredientViewHolder(val binding: IngredientListitemBinding) {
        val view: View = binding.root
    }
}
