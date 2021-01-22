package de.hs_rm.recipe_me.model.shopping_list

/**
 * Used to show that a [ShoppingListItem] can't be extended by another Ingredient
 * because they are mismatching
 */
class MismatchingIngredientException(
    message: String = "ShoppingListItem can't be extended by this Ingredient"
) : Exception(message)
