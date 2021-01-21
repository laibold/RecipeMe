package de.hs_rm.recipe_me.ui.recipe.detail

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import de.hs_rm.recipe_me.databinding.CookingStepListitemBinding
import de.hs_rm.recipe_me.model.recipe.CookingStep

/**
 * Adapter to show CookingSteps in [CookingStepFragment]]
 */
class CookingStepListAdapter(
    context: Context,
    private val resource: Int,
    private val objects: List<CookingStep>,
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
            ) as CookingStepListitemBinding

            holder = CookingStepViewHolder(viewBinding)
            holder.view.tag = holder
        } else {
            holder = convertView.tag as CookingStepViewHolder
        }

        val cookingStep = objects[position]

        if (cookingStep.imageUri != "") {
            // set image here
        } else {
            holder.binding.cookingStepImage.visibility = View.GONE
        }

        holder.binding.cookingStepNumber.text = (position + 1).toString()
        holder.binding.cookingStepText.text = cookingStep.text

        return holder.view
    }

    // https://www.spreys.com/view-holder-design-pattern-for-android/
    // https://stackoverflow.com/questions/43973490/how-to-do-android-data-binding-a-customadapter-inherited-from-baseadapter-for-sp
    private class CookingStepViewHolder(val binding: CookingStepListitemBinding) {
        val view: View = binding.root
    }
}
