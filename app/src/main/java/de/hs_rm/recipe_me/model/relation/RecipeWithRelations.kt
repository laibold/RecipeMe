package de.hs_rm.recipe_me.model.relation

import androidx.room.Embedded
import androidx.room.Relation
import de.hs_rm.recipe_me.model.recipe.CookingStep
import de.hs_rm.recipe_me.model.recipe.Ingredient
import de.hs_rm.recipe_me.model.recipe.Recipe

/**
 * Class that merges recipes with their 1:n relations (ingredients and cookingSteps)
 */
data class RecipeWithRelations(
    @Embedded var recipe: Recipe,
    @Relation(
        parentColumn = "id",
        entityColumn = "recipeId"
    )
    var ingredients: List<Ingredient>,
    @Relation(
        entity = CookingStep::class,
        parentColumn = "id",
        entityColumn = "recipeId"
    )
    var cookingStepsWithIngredients: List<CookingStepWithIngredients>
)
