package de.hs_rm.recipe_me.declaration

import de.hs_rm.recipe_me.model.recipe.CookingStep

interface CallbackAdapter {
    fun onCallback(cookingStep: CookingStep)
}
