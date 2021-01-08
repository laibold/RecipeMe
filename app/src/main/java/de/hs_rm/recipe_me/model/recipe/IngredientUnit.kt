package de.hs_rm.recipe_me.model.recipe

import de.hs_rm.recipe_me.R

/**
 * Unit for describing amounts of Ingredients. Singular and plural names given
 */
enum class IngredientUnit(private val singularResId: Int, private val pluralResId: Int) :
    NumberModel {

    NONE(R.string.none_unit, R.string.none_unit),
    GRAM(R.string.gram, R.string.gram),
    MILLILITER(R.string.milliliter, R.string.milliliter),
    TEASPOON(R.string.teaspoon, R.string.teaspoon),
    TABLESPOON(R.string.tablespoon, R.string.tablespoon),
    CENTIMETER(R.string.centimeter, R.string.centimeter),
    PACKAGE(R.string.package_sg, R.string.package_pl),
    PACK(R.string.pack_sg, R.string.pack_pl),
    PINCH(R.string.pinch_sg, R.string.pinch_pl),
    SLICE(R.string.slice_sg, R.string.slice_pl),
    DASH(R.string.dash_sg, R.string.dash_pl),
    STICK(R.string.stick_sg, R.string.stick_pl),
    CLOVE(R.string.clove_sg, R.string.clove_pl),
    SPRIG(R.string.sprig_sg, R.string.sprig_pl);

    override fun getSingularId(): Int {
        return singularResId
    }

    override fun getPluralId(): Int {
        return pluralResId
    }
}
