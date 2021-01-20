package de.hs_rm.recipe_me.ui.profile

import androidx.databinding.ObservableInt
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.hs_rm.recipe_me.model.relation.RecipeWithRelations
import de.hs_rm.recipe_me.service.RecipeRepository
import kotlinx.coroutines.launch

class ProfileViewModel @ViewModelInject constructor(
    private val repository: RecipeRepository
) : ViewModel() {

    lateinit var total: LiveData<Int>

    /**
     * Get total from repository and save it to ViewModel
     */
    fun loadRecipeTotal() {
        total = repository.getRecipeTotal()
    }

}
