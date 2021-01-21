package de.hs_rm.recipe_me.ui.recipe.category

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import de.hs_rm.recipe_me.R
import de.hs_rm.recipe_me.databinding.RecipeCategoryFragmentBinding
import de.hs_rm.recipe_me.declaration.ui.fragments.DeleteRecipeCallbackAdapter
import de.hs_rm.recipe_me.model.recipe.Recipe
import de.hs_rm.recipe_me.ui.component.CustomAlertDialog

@AndroidEntryPoint
class RecipeCategoryFragment : Fragment(), DeleteRecipeCallbackAdapter {

    private val viewModel: RecipeCategoryViewModel by viewModels()
    private val args: RecipeCategoryFragmentArgs by navArgs()
    private lateinit var binding: RecipeCategoryFragmentBinding
    private lateinit var adapter: RecipeListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.recipe_category_fragment,
            container,
            false
        )

        viewModel.category = args.recipeCategory
        val name = context?.resources?.getString(viewModel.category.nameResId)
        binding.categoryHeadline.text = name

        binding.addButton.setOnClickListener {
            val direction = RecipeCategoryFragmentDirections.toAddRecipeNavGraph(viewModel.category)
            findNavController().navigate(direction)
        }

        setAdapter()

        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set action to back button: if item in adapter is selected, remove the selection by setting
        // itemSelected to false. If nothing is selected, navigate to HomeFragment
        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (adapter.itemSelected.get()) {
                        adapter.removeSelection()
                    } else {
                        val direction = RecipeCategoryFragmentDirections.toRecipeHomeFragment()
                        findNavController().navigate(direction)
                    }
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    /**
     * Set Adapter with recipes of selected category to ListView
     */
    private fun setAdapter() {
        val list = binding.recipeList
        viewModel.getRecipesByCategory(viewModel.category).observe(this.viewLifecycleOwner, {
            adapter = RecipeListAdapter(requireContext(), R.layout.recipe_listitem, it, this)
            list.adapter = adapter
            adapter.notifyDataSetChanged()
        })
    }

    /**
     * Create delete dialog to let the user confirm the deletion of a recipe
     */
    private fun deleteDialog(recipe: Recipe): CustomAlertDialog {
        return CustomAlertDialog.Builder(requireActivity())
            .title(R.string.delete)
            .message(R.string.delete_recipe_message)
            .positiveButton(R.string.delete) {
                viewModel.deleteRecipeAndRelations(recipe)
                adapter.notifyDataSetChanged()
            }
            .negativeButton(
                R.string.cancel
            ) {
                adapter.removeSelection()
            }
            .create()
    }

    /**
     * On callback from RecipeListAdapter delete selected Recipe
     */
    override fun onCallback(recipe: Recipe) {
        deleteDialog(recipe).show()
    }

}
