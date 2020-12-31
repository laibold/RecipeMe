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
    @Embedded val recipe: Recipe,
    @Relation(
        parentColumn = "id",
        entityColumn = "recipeId"
    )
    val ingredients: List<Ingredient>,
    @Relation(
        parentColumn = "id",
        entityColumn = "recipeId"
    )
    val cookingSteps: List<CookingStep>
)