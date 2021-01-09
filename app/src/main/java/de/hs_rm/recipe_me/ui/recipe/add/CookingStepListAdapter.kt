package de.hs_rm.recipe_me.ui.recipe.add

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import de.hs_rm.recipe_me.R
import de.hs_rm.recipe_me.databinding.CookingStepListitemBinding
import de.hs_rm.recipe_me.databinding.IngredientListitemBinding
import de.hs_rm.recipe_me.model.recipe.CookingStep

class CookingStepListAdapter(
    context: Context,
    private val resource: Int,
    private val objects: Array<CookingStep>
) :
    ArrayAdapter<CookingStep>(context, resource, objects) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val holder: CookingStepListAdapter.CookingStepViewHolder

        if (convertView == null) {
            val viewBinding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                resource,
                parent,
                false
            ) as CookingStepListitemBinding

            holder = CookingStepListAdapter.CookingStepViewHolder(viewBinding)
            holder.view.tag = holder
        } else {
            holder = convertView.tag as CookingStepListAdapter.CookingStepViewHolder
        }

        val cookingStep = objects[position]

        holder.binding.cookingStepText.text = cookingStep.text

        holder.binding.editButton.setOnClickListener {
            Toast.makeText(context, "$position bearbeiten", Toast.LENGTH_SHORT).show()
        }

        holder.binding.removeButton.setOnClickListener {
            Toast.makeText(context, "$position entfernen", Toast.LENGTH_SHORT).show()
        }

        return holder.view
    }

    // https://www.spreys.com/view-holder-design-pattern-for-android/
    // https://stackoverflow.com/questions/43973490/how-to-do-android-data-binding-a-customadapter-inherited-from-baseadapter-for-sp
    private class CookingStepViewHolder(val binding: CookingStepListitemBinding) {
        val view: View = binding.root
    }
}