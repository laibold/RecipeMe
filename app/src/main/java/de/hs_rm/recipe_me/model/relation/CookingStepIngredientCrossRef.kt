package de.hs_rm.recipe_me.model.relation

import androidx.room.Entity
import de.hs_rm.recipe_me.model.recipe.CookingStep
import de.hs_rm.recipe_me.model.recipe.Recipe

/**
 * Cross reference between [CookingStep] and [Recipe] for many to many relationship
 */
@Entity(primaryKeys = ["cookingStepId", "ingredientId"])
class CookingStepIngredientCrossRef(
    val cookingStepId: Long,
    val ingredientId: Long
)
