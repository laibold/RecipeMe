package de.hs_rm.recipe_me.ui.recipe.add

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import de.hs_rm.recipe_me.databinding.AddCookingStepListitemBinding
import de.hs_rm.recipe_me.declaration.ui.fragments.EditCookingStepAdapter
import de.hs_rm.recipe_me.model.recipe.CookingStep

class AddCookingStepListAdapter(
    context: Context,
    private val resource: Int,
    private val objects: MutableList<CookingStep>,
    private val callbackListener: EditCookingStepAdapter
) :
    ArrayAdapter<CookingStep>(context, resource, objects) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val holder: CookingStepViewHolder

        if (convertView == null) {
            val viewBinding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                resource,
                parent,
                false
            ) as AddCookingStepListitemBinding

            holder = CookingStepViewHolder(viewBinding)
            holder.view.tag = holder
        } else {
            holder = convertView.tag as CookingStepViewHolder
        }

        val cookingStep = objects[position]

        holder.binding.cookingStepText.text = cookingStep.text
        holder.binding.removeButton.setOnClickListener {
            removeObject(position)
        }
        holder.binding.editButton.setOnClickListener {
            callbackListener.onCallback(objects[position], position)
        }

        return holder.view
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
    private class CookingStepViewHolder(val binding: AddCookingStepListitemBinding) {
        val view: View = binding.root
    }
}
