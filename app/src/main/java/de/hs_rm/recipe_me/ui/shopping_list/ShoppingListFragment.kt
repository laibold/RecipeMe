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
import de.hs_rm.recipe_me.declaration.ui.fragments.ShoppingListAdapter
import de.hs_rm.recipe_me.model.shopping_list.ShoppingListItem

@AndroidEntryPoint
class ShoppingListFragment : Fragment(), ShoppingListAdapter {

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

//        viewModel.addShoppingListItem("3 Bananen")
//        viewModel.addShoppingListItem("5 Banonen")
//        viewModel.addShoppingListItem("Citron")

        viewModel.loadShoppingListItems()

        viewModel.shoppingListItems.observe(viewLifecycleOwner, {
            setAdapter(it)
        })

        return binding.root
    }

    private fun setAdapter(list: List<ShoppingListItem>) {
        adapter =
            ShoppingListListViewAdapter(
                requireContext(),
                R.layout.shopping_list_listitem,
                list,
                this
            )
        binding.shoppingListListView.adapter = adapter
    }

}