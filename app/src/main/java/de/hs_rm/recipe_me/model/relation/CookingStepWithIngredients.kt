package de.hs_rm.recipe_me.model.relation

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import de.hs_rm.recipe_me.model.recipe.CookingStep
import de.hs_rm.recipe_me.model.recipe.Ingredient

/**
 * Class for querying [CookingStep] with belonging [Ingredient]s
 */
data class CookingStepWithIngredients(
    @Embedded var cookingStep: CookingStep,
    @Relation(
        parentColumn = "cookingStepId",
        entityColumn = "ingredientId",
        associateBy = Junction(CookingStepIngredientCrossRef::class)
    )
    var ingredients: List<Ingredient>
) {
    constructor(cookingStep: CookingStep) : this(
        cookingStep,
        listOf()
    )

}
