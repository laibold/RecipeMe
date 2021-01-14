package de.hs_rm.recipe_me.ui.recipe

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import androidx.databinding.Observable
import androidx.databinding.ObservableDouble
import de.hs_rm.recipe_me.databinding.IngredientListitemBinding
import de.hs_rm.recipe_me.model.recipe.Ingredient

class IngredientListAdapter(
    context: Context,
    private val resource: Int,
    private val objects: List<Ingredient>
) :
    ArrayAdapter<Ingredient>(context, resource, objects) {

    var multiplier = 1.0

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

        holder.binding.ingredientTextView.text = getIngredientText(ingredient)

        return holder.view
    }

    /**
     * Create text for ingredient in form of "(quantity unit) name"
     */
    private fun getIngredientText(ingredient: Ingredient): CharSequence {
        //TODO: add unit
        val calculatedQuantity = ingredient.quantity * multiplier

        return calculatedQuantity.toString() + " " + ingredient.name
    }

    // https://www.spreys.com/view-holder-design-pattern-for-android/
    // https://stackoverflow.com/questions/43973490/how-to-do-android-data-binding-a-customadapter-inherited-from-baseadapter-for-sp
    private class IngredientViewHolder(val binding: IngredientListitemBinding) {
        val view: View = binding.root
    }

}