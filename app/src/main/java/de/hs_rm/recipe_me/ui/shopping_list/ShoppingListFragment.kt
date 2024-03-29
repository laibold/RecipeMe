package de.hs_rm.recipe_me.ui.shopping_list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import de.hs_rm.recipe_me.R
import de.hs_rm.recipe_me.databinding.ShoppingListFragmentBinding
import de.hs_rm.recipe_me.model.shopping_list.ShoppingListItem
import de.hs_rm.recipe_me.model.user.User
import de.hs_rm.recipe_me.service.Formatter
import de.hs_rm.recipe_me.service.TextSharer

@AndroidEntryPoint
class ShoppingListFragment : Fragment() {

    private lateinit var binding: ShoppingListFragmentBinding
    private val viewModel: ShoppingListViewModel by viewModels()
    private lateinit var adapter: ShoppingListListViewAdapter
    private var isInitialized = false

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
        viewModel.loadUser()

        viewModel.shoppingListItems.observe(viewLifecycleOwner, {
            onShoppingListItemsChanged(it)
        })

        binding.shoppingListListLayout.listView.emptyView =
            binding.shoppingListListLayout.addHintText

        binding.addItemButton.setOnClickListener {
            onAddItem()
        }

        binding.addItemEditText.setOnEditorActionListener { _, actionId: Int, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                onAddItem()
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }

        binding.shoppingListListLayout.clearListButton.setOnClickListener {
            onClearButtonPressed()
        }

        viewModel.user.observe(viewLifecycleOwner, { user ->
            binding.shareButton.setOnClickListener {
                TextSharer.share(requireContext(), getShareText(user))
            }
        })

        return binding.root
    }

    /**
     * When shoppingListItems change:
     * - show content (just important for initial loading)
     * - (re-)set adapter
     * - show clear and share button if list is not empty
     */
    private fun onShoppingListItemsChanged(list: List<ShoppingListItem>) {
        if (!isInitialized) {
            binding.contentWrapper.visibility = View.VISIBLE
        }
        setAdapter(list)
        toggleButtonsVisibility(list.isNotEmpty())
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
    private fun toggleButtonsVisibility(listNotEmpty: Boolean) {
        if (listNotEmpty) {
            binding.shoppingListListLayout.clearListButton.visibility = View.VISIBLE

            if (viewModel.allItemsChecked()) {
                binding.shareButton.visibility = View.GONE
            } else {
                binding.shareButton.visibility = View.VISIBLE
            }
        } else {
            binding.shoppingListListLayout.clearListButton.visibility = View.GONE
            binding.shareButton.visibility = View.GONE
        }

    }

    /**
     * Validate text content. If valid, add new item to list and scroll to top to make it visible
     */
    private fun onAddItem() {
        if (binding.addItemEditText.text.isBlank()) {
            binding.addItemEditText.text.clear()
            binding.addItemEditText.error = getString(R.string.err_enter_text)
        } else {
            viewModel.addShoppingListItem(binding.addItemEditText.text)
            binding.shoppingListListLayout.scrollView.smoothScrollTo(0, 0)
            binding.addItemEditText.text.clear()
        }
    }

    /**
     * If items are checked, remove them. Show toast if no item is checked
     */
    private fun onClearButtonPressed() {
        if (viewModel.isItemChecked() == true) {
            viewModel.clearCheckedItems()
        } else {
            val message = getString(R.string.check_items_to_be_removed)
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }

    /**
     * @return Text for sharing list items to other apps
     */
    private fun getShareText(user: User?): String {
        val username = if (user == null) {
            getString(R.string.my)
        } else {
            Formatter.formatNameToGenitive(user.name)
        }
        var s = getString(R.string.shopping_list_export_headline).format(username)
        viewModel.shoppingListItems.value?.let {
            for (item in it) {
                if (!item.checked) {
                    s += Formatter.formatIngredientValues(
                        requireContext(),
                        item.name,
                        item.quantity,
                        item.unit
                    ) + "\n"
                }
            }
        }
        s += "\n" + getString(R.string.store_link)
        return s
    }

}
