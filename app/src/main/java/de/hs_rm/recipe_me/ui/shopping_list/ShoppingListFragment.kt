package de.hs_rm.recipe_me.ui.shopping_list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import de.hs_rm.recipe_me.R
import de.hs_rm.recipe_me.databinding.MainFragmentBinding
import de.hs_rm.recipe_me.databinding.ShoppingListFragmentBinding

class ShoppingListFragment : Fragment() {

    private lateinit var binding: ShoppingListFragmentBinding

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
        return binding.root
    }

}