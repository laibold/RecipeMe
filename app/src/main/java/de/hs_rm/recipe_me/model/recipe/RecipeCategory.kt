package de.hs_rm.recipe_me.model.recipe

import androidx.room.Entity
import de.hs_rm.recipe_me.R

/**
 * Category a [Recipe] belongs to
 */
@Entity
enum class RecipeCategory(val nameResId: Int, val drawableResId: Int) {

    MAIN_DISHES(R.string.main_dishes, R.drawable.category_main_dishes),
    SALADS(R.string.salads, R.drawable.category_salads),
    SOUPS(R.string.soups, R.drawable.category_soups),
    DESSERTS(R.string.desserts, R.drawable.category_desserts),
    SNACKS(R.string.snacks, R.drawable.category_snacks),
    BREAKFAST(R.string.breakfast, R.drawable.category_breakfast),
    BAKED_GOODS(R.string.baked_goods, R.drawable.category_baked_goods),
    BEVERAGES(R.string.beverages, R.drawable.category_beverages);

}
