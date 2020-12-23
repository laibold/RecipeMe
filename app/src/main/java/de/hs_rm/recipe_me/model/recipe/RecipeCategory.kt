package de.hs_rm.recipe_me.model.recipe

import androidx.room.Entity
import de.hs_rm.recipe_me.R

/**
 * Category a [Recipe] belongs to
 */
@Entity
enum class RecipeCategory(nameResId: Int) {
    BAKED_GOODS(R.string.baked_goods),
    SALADS(R.string.salads),
    MAIN_DISHES(R.string.main_dishes),
    DESSERTS(R.string.desserts),
    SOUPS(R.string.soups),
    SNACKS(R.string.snacks),
    BREAKFAST(R.string.breakfast),
    BEVERAGES(R.string.beverages);
}
