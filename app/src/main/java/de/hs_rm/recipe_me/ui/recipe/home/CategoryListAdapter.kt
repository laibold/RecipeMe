package de.hs_rm.recipe_me.ui.recipe.home

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy.ALL
import de.hs_rm.recipe_me.databinding.CategoryListitemBinding
import de.hs_rm.recipe_me.model.recipe.RecipeCategory

/**
 * Adapter for View of RecipeCategories. Layout: category_listitem in ListView
 */
class CategoryListAdapter(
    context: Context,
    private val resource: Int,
    private val objects: Array<RecipeCategory>
) :
    ArrayAdapter<RecipeCategory>(context, resource, objects) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val holder: CategoryViewHolder

        if (convertView == null) {
            val viewBinding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                resource,
                parent,
                false
            ) as CategoryListitemBinding

            holder = CategoryViewHolder(viewBinding)
            holder.view.tag = holder
        } else {
            holder = convertView.tag as CategoryViewHolder
        }

        val category = objects[position]
        holder.binding.categoryName.text = context.getString(category.nameResId)

        Glide.with(context)
            .load(category.drawableResId)
            .diskCacheStrategy(ALL)
            .into(holder.binding.imageView)

        holder.binding.root.setOnClickListener {
            val direction = RecipeHomeFragmentDirections.toRecipeCategoryFragment(category)
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
