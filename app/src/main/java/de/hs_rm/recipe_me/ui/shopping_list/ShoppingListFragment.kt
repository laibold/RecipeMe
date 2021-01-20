package de.hs_rm.recipe_me.ui.shopping_list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import de.hs_rm.recipe_me.R
import de.hs_rm.recipe_me.databinding.ShoppingListFragmentBinding
import de.hs_rm.recipe_me.model.shopping_list.ShoppingListItem

@AndroidEntryPoint
class ShoppingListFragment : Fragment() {

    private lateinit var binding: ShoppingListFragmentBinding
    private val viewModel: ShoppingListViewModel by viewModels()
    private lateinit var adapter: ShoppingListListViewAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.shopping_list_fragment,
            container,
            false
        )

        viewModel.loadShoppingListItems()

        viewModel.shoppingListItems.observe(viewLifecycleOwner, {
            setAdapter(it)
            toggleClearButtonVisibility(it.isNotEmpty())
        })

        return binding.root
    }

    /**
     * Set Adapter to ListView and ClickListener to its items
     */
    private fun setAdapter(list: List<ShoppingListItem>) {
        adapter =
            ShoppingListListViewAdapter(
                requireContext(),
                R.layout.shopping_list_listitem,
                list
            )

        binding.shoppingListListLayout.listView.setOnItemClickListener { _, _, _, id ->
            viewModel.toggleItemChecked(id.toInt())
        }

        binding.shoppingListListLayout.listView.adapter = adapter
    }

    /**
     * Toggle visibility of clear list button. Button should be displayed if list is not empty
     * @param listNotEmpty true if list is not empty
     */
    private fun toggleClearButtonVisibility(listNotEmpty: Boolean) {
        if (listNotEmpty) {
            binding.shoppingListListLayout.clearListButton.visibility = View.VISIBLE
        } else {
            binding.shoppingListListLayout.clearListButton.visibility = View.GONE
        }
    }

}
