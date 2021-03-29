package de.hs_rm.recipe_me.ui.profile.settings

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * ViewModel for [SettingsFragment]
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val context: ApplicationContext,
) : ViewModel() {

}
