package de.hs_rm.recipe_me.model.relation

import androidx.room.Entity

@Entity(primaryKeys = ["cookingStepId", "ingredientId"])
class CookingStepIngredientCrossRef(
    val cookingStepId: Long,
    val ingredientId: Long
)
