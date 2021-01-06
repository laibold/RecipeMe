package de.hs_rm.recipe_me.service

import androidx.lifecycle.LiveData
import de.hs_rm.recipe_me.model.recipe.*
import de.hs_rm.recipe_me.model.relation.RecipeWithRelations
import de.hs_rm.recipe_me.persistence.RecipeDao
import javax.inject.Inject

/**
 * Single Source of Truth for [Recipe]. Use it with Dependency Injection
 */
class RecipeRepository @Inject constructor(
    private val recipeDao: RecipeDao
) {

    /**
     * Insert test recipes
     */
    suspend fun insertTestRecipes() {
        val r1 = Recipe("Börex", 1, RecipeCategory.BAKED_GOODS, "boerex.jpg")
        val id = recipeDao.insert(r1)
        val i1 = Ingredient(id, "Teig", 1.0, IngredientUnit.NONE)
        val i2 = Ingredient(id, "Spinat", 1.0, IngredientUnit.NONE)
        val s1 = CookingStep(id, "boerex-step1.jpg", "rollen", 0)
        val s2 = CookingStep(id, "boerex-step2.jpg", "backen", 0)

        recipeDao.insert(i1)
        recipeDao.insert(i2)
        recipeDao.insert(s1)
        recipeDao.insert(s2)
    }

    /**
     * Get all recipes
     */
    fun getRecipes(): LiveData<List<RecipeWithRelations>> {
        return recipeDao.getRecipes()
    }

    /**
     * Clear recipes
     */
    suspend fun clearRecipes() {
        recipeDao.clear()
    }

    /**
     * Get recipes by category
     */
    fun getRecipesByCategory(recipeCategory: RecipeCategory): LiveData<List<Recipe>> {
        return recipeDao.getRecipesByCategory(recipeCategory)
    }

}
