package de.hs_rm.recipe_me.ui.recipe.add

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import de.hs_rm.recipe_me.databinding.CookingStepListitemBinding
import de.hs_rm.recipe_me.declaration.CallbackAdapter
import de.hs_rm.recipe_me.model.recipe.CookingStep

class CookingStepListAdapter(
    context: Context,
    private val resource: Int,
    private val objects: MutableList<CookingStep>,
    private val callbackListener: CallbackAdapter
) :
    ArrayAdapter<CookingStep>(context, resource, objects) {

    var editingEnabled = true

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val holder: CookingStepListAdapter.CookingStepViewHolder

        if (convertView == null) {
            val viewBinding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                resource,
                parent,
                false
            ) as CookingStepListitemBinding

            holder = CookingStepViewHolder(viewBinding)
            holder.view.tag = holder
        } else {
            holder = convertView.tag as CookingStepViewHolder
        }

        val cookingStep = objects[position]

        holder.binding.cookingStepText.text = cookingStep.text

        if (editingEnabled) {
            enableButtons(holder, position)
        } else {
            disableButtons(holder)
        }

        return holder.view
    }

    private fun enableButtons(holder: CookingStepViewHolder, position: Int) {
        holder.binding.removeButton.visibility = View.VISIBLE
        holder.binding.removeButton.setOnClickListener { removeObject(position) }

        holder.binding.editButton.visibility = View.VISIBLE
        holder.binding.editButton.setOnClickListener {
            callbackListener.onCallback(objects[position], position)
        }
    }

    private fun disableButtons(holder: CookingStepListAdapter.CookingStepViewHolder) {
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
    private class CookingStepViewHolder(val binding: CookingStepListitemBinding) {
        val view: View = binding.root
    }
}
