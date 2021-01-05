package de.hs_rm.recipe_me.ui.recipe

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import de.hs_rm.recipe_me.R
import de.hs_rm.recipe_me.databinding.CategoryListitemBinding
import de.hs_rm.recipe_me.model.recipe.RecipeCategory

/**
 * Adapter for View of RecipeCategories. Layout: category_listitem in ListView
 */
class CategoryListAdapter(
    context: Context,
    resource: Int,
    private val objects: Array<RecipeCategory>
) :
    ArrayAdapter<RecipeCategory>(context, resource, objects) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val holder: CategoryViewHolder

        if (convertView == null) {
            val viewBinding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.category_listitem,
                parent,
                false
            ) as CategoryListitemBinding

            holder = CategoryViewHolder(viewBinding)
            holder.view.tag = holder
        } else {
            holder = convertView.tag as CategoryViewHolder
        }

        val category = objects[position]
        holder.binding.categoryName.text = context.resources.getString(category.nameResId)
        holder.binding.imageView.setBackgroundResource(category.drawableResId)
        holder.binding.root.setOnClickListener {
            val direction =
                RecipeHomeFragmentDirections.actionRecipeHomeFragmentToRecipeCategoryFragment(category)
            it.findNavController().navigate(direction)
        }

        return holder.view
    }

    // https://www.spreys.com/view-holder-design-pattern-for-android/
    // https://stackoverflow.com/questions/43973490/how-to-do-android-data-binding-a-customadapter-inherited-from-baseadapter-for-sp
    private class CategoryViewHolder(val binding: CategoryListitemBinding) {
        val view: View = binding.root
    }
}
