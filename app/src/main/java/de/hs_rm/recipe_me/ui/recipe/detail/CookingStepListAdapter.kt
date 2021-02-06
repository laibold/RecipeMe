package de.hs_rm.recipe_me.ui.recipe.detail

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import de.hs_rm.recipe_me.databinding.CookingStepListitemBinding
import de.hs_rm.recipe_me.declaration.ui.fragments.CookingStepCallbackAdapter
import de.hs_rm.recipe_me.model.recipe.CookingStep
import de.hs_rm.recipe_me.model.relation.CookingStepWithIngredients
import de.hs_rm.recipe_me.service.Formatter

/**
 * Adapter to show CookingSteps in [CookingStepFragment]]
 */
class CookingStepListAdapter(
    context: Context,
    private val resource: Int,
    private val objects: List<CookingStepWithIngredients>,
    private val callbackListener: CookingStepCallbackAdapter,
) :
    ArrayAdapter<CookingStepWithIngredients>(context, resource, objects) {

    @SuppressLint("SetTextI18n")
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

        val cookingStepWithIngredients = objects[position]
        val cookingStep = cookingStepWithIngredients.cookingStep

        if (cookingStep.imageUri != "") {
            // set image here
        } else {
            holder.binding.cookingStepImage.visibility = View.GONE
        }

        holder.binding.cookingStepNumber.text = (position + 1).toString()
        holder.binding.cookingStepText.text = cookingStep.text

        if (cookingStepWithIngredients.ingredients.isNotEmpty()) {
            holder.binding.assignedIngredientsTextView.visibility = View.VISIBLE
            holder.binding.assignedIngredientsTextView.text =
                Formatter.formatIngredientList(context, cookingStepWithIngredients.ingredients)
        } else {
            holder.binding.assignedIngredientsTextView.visibility = View.GONE
        }

        if (cookingStep.time != CookingStep.DEFAULT_TIME) {
            holder.binding.timerElement.visibility = View.VISIBLE
            holder.binding.timerText.text = "${cookingStep.time} ${
                cookingStep.timeUnit.getNumberString(
                    context.resources,
                    cookingStep.time
                )
            }"
        } else {
            // visibility gone is already defined in the layout, but there seems to be a bug, so set it here again
            holder.binding.timerElement.visibility = View.GONE
        }

        holder.binding.timerElement.setOnClickListener {
            callbackListener.onCallback(cookingStep)
        }

        return holder.view
    }

    // https://www.spreys.com/view-holder-design-pattern-for-android/
    // https://stackoverflow.com/questions/43973490/how-to-do-android-data-binding-a-customadapter-inherited-from-baseadapter-for-sp
    private class CookingStepViewHolder(val binding: CookingStepListitemBinding) {
        val view: View = binding.root
    }
}
