package de.hs_rm.recipe_me.ui.shopping_list

import android.content.Context
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import de.hs_rm.recipe_me.databinding.ShoppingListListitemBinding
import de.hs_rm.recipe_me.declaration.ui.fragments.ShoppingListAdapter
import de.hs_rm.recipe_me.model.shopping_list.ShoppingListItem

/**
 * Adapter for View of ShoppingListItems. Layout: shopping_list_listitem in ListView
 */
class ShoppingListListViewAdapter(
    context: Context,
    private val resource: Int,
    private val objects: List<ShoppingListItem>,
    private val callbackListener: ShoppingListAdapter
) : ArrayAdapter<ShoppingListItem>(context, resource, objects) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val holder: ShoppingListItemViewHolder

        if (convertView == null) {
            val viewBinding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                resource,
                parent,
                false
            ) as ShoppingListListitemBinding

            holder = ShoppingListItemViewHolder(viewBinding)
            holder.view.tag = holder
        } else {
            holder = convertView.tag as ShoppingListItemViewHolder
        }

        val listItem = objects[position]

        holder.binding.itemText.text = listItem.format(context)

        val checked = listItem.checked
        holder.binding.itemCheckbox.isChecked = listItem.checked
//        if (checked) {
//            holder.binding.itemText.paintFlags =
//                holder.binding.itemText.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
//        } else {
//            holder.binding.itemText.paintFlags =
//                holder.binding.itemText.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG.inv()
//        }
//
//        holder.binding.itemCheckbox.isChecked = checked
//        listItem.checked = checked

        holder.binding.root.setOnClickListener {
            listItem.checked = !listItem.checked
            notifyDataSetChanged()
        }

        return holder.view
    }

    // https://www.spreys.com/view-holder-design-pattern-for-android/
    // https://stackoverflow.com/questions/43973490/how-to-do-android-data-binding-a-customadapter-inherited-from-baseadapter-for-sp
    private class ShoppingListItemViewHolder(val binding: ShoppingListListitemBinding) {
        val view: View = binding.root
    }

}
