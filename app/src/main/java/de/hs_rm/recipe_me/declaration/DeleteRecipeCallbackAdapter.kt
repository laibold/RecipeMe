package de.hs_rm.recipe_me.declaration

import de.hs_rm.recipe_me.model.recipe.Recipe

interface DeleteRecipeCallbackAdapter {
    fun onCallback(recipe: Recipe)
}
