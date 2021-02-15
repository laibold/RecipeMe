package de.hs_rm.recipe_me.declaration.ui.fragments

import de.hs_rm.recipe_me.model.recipe.Ingredient

/**
 * Contains the abstract method onCallback(), which can be used to define a callback from
 * an Adapter to a Fragment. Let the Fragment implement EditIngredientAdapter and hand it to the Adapter.
 * Afterwards it's possible to call the defined method in the Fragment from within the Adapter.
 */
interface EditIngredientAdapter {
    fun onCallback(ingredient: Ingredient, position: Int)
}
