package de.hs_rm.recipe_me.ui.recipe

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter

import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import de.hs_rm.recipe_me.R
import de.hs_rm.recipe_me.databinding.CategoryListitemBinding
import de.hs_rm.recipe_me.databinding.RecipeListitemBinding
import de.hs_rm.recipe_me.model.recipe.Recipe
import de.hs_rm.recipe_me.model.recipe.RecipeCategory

/**
 * Adapter for View of Recipes by RecipeCategory. Layout: recipe_listitem in ListView
 */
class RecipeListAdapter(
    context: Context,
    resource: Int,
    private val objects: Array<Recipe>
) :
    ArrayAdapter<Recipe>(context, resource, objects) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val holder: RecipeViewHolder

        if (convertView == null) {
            val viewBinding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.recipe_listitem,
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

        return holder.view
    }

    // https://www.spreys.com/view-holder-design-pattern-for-android/
    // https://stackoverflow.com/questions/43973490/how-to-do-android-data-binding-a-customadapter-inherited-from-baseadapter-for-sp
    private class RecipeViewHolder(val binding: RecipeListitemBinding) {
        val view: View = binding.root
    }
}