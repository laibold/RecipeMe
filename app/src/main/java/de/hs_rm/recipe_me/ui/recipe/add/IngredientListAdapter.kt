package de.hs_rm.recipe_me.ui.recipe.add

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import de.hs_rm.recipe_me.R
import de.hs_rm.recipe_me.databinding.CategoryListitemBinding
import de.hs_rm.recipe_me.databinding.IngredientListitemBinding
import de.hs_rm.recipe_me.model.recipe.Ingredient
import de.hs_rm.recipe_me.model.recipe.IngredientUnit
import de.hs_rm.recipe_me.model.recipe.RecipeCategory
import de.hs_rm.recipe_me.ui.recipe.RecipeHomeFragmentDirections

class IngredientListAdapter(
    context: Context,
    resource: Int,
    private val objects: Array<Ingredient>
) :
    ArrayAdapter<Ingredient>(context, resource, objects) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val holder: IngredientViewHolder

        if (convertView == null) {
            val viewBinding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.ingredient_listitem,
                parent,
                false
            ) as IngredientListitemBinding

            holder = IngredientViewHolder(viewBinding)
            holder.view.tag = holder
        } else {
            holder = convertView.tag as IngredientViewHolder
        }

        val ingredient = objects[position]
        var unitText = ""

        if (ingredient.unit != IngredientUnit.NONE) {
            unitText = ingredient.quantity.toString() +
                    " " +
                    getNumberString(ingredient.quantity, ingredient.unit) +
                    " "
        }

        val text = unitText + ingredient.name

        holder.binding.ingredientNameView.text = text

        return holder.view
    }

    private fun getNumberString(amount: Double, unit: IngredientUnit): String {
        return if (amount == 1.0) {
            context.resources.getString(unit.getSingularId())
        } else {
            context.resources.getString(unit.getPluralId())
        }
    }

    // https://www.spreys.com/view-holder-design-pattern-for-android/
    // https://stackoverflow.com/questions/43973490/how-to-do-android-data-binding-a-customadapter-inherited-from-baseadapter-for-sp
    private class IngredientViewHolder(val binding: IngredientListitemBinding) {
        val view: View = binding.root
    }

}