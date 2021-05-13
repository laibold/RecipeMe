package de.hs_rm.recipe_me.persistence

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import de.hs_rm.recipe_me.declaration.toInt
import de.hs_rm.recipe_me.model.recipe.*
import de.hs_rm.recipe_me.model.shopping_list.ShoppingListItem
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MigrationTest {
    private val TEST_DB = "migration-test"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    fun migrate9To10() {
        val r1 = Recipe("Recipe 1", 1, RecipeCategory.MAIN_DISHES).apply { id = 1 }
        val r2 = Recipe("Recipe 2", 2, RecipeCategory.SNACKS).apply { id = 2 }
        val i1 = Ingredient(1, "Ing 1", 1.0, IngredientUnit.NONE).apply { ingredientId = 3 }
        val i2 = Ingredient(2, "Ing 2", 2.0, IngredientUnit.PACK).apply { ingredientId = 4 }
        val s1 = ShoppingListItem(i1).apply { id = 5 }
        val s2 = ShoppingListItem(i2).apply { id = 6 }
        val cs1 = CookingStep(1, "Step 1", 1, TimeUnit.SECOND).apply { cookingStepId = 7 }
        val cs2 = CookingStep(2, "Step 2", 2, TimeUnit.HOUR).apply { cookingStepId = 8 }

        @Suppress("VARIABLE_WITH_REDUNDANT_INITIALIZER")
        var db = helper.createDatabase(TEST_DB, 9).apply {
            // db has schema version 9. insert some data using SQL queries.
            // You cannot use DAO classes because they expect the latest schema.
            execSQL(
                "INSERT INTO Recipe (id, name, servings, category)" +
                        " VALUES (${r1.id}, \"${r1.name}\", ${r1.servings}, ${r1.category.ordinal})"
            )
            execSQL(
                "INSERT INTO Recipe (id, name, servings, category)" +
                        " VALUES (${r2.id}, \"${r2.name}\", ${r2.servings}, ${r2.category.ordinal})"
            )
            execSQL(
                "INSERT INTO Ingredient (ingredientId, recipeId, name, quantity, unit)" +
                        " VALUES (${i1.ingredientId}, ${i1.recipeId}, \"${i1.name}\", ${i1.quantity}, ${i1.unit.ordinal})"
            )
            execSQL(
                "INSERT INTO Ingredient (ingredientId, recipeId, name, quantity, unit)" +
                        " VALUES (${i2.ingredientId}, ${i2.recipeId}, \"${i2.name}\", ${i2.quantity}, ${i2.unit.ordinal})"
            )
            execSQL(
                "INSERT INTO ShoppingListItem (id, checked, name, quantity, unit)" +
                        " VALUES (${s1.id}, ${s1.checked.toInt()}, \"${s1.name}\", ${s1.quantity}, ${s1.unit.ordinal})"
            )
            execSQL(
                "INSERT INTO ShoppingListItem (id, checked, name, quantity, unit)" +
                        " VALUES (${s2.id}, ${s2.checked.toInt()}, \"${s2.name}\", ${s2.quantity}, ${s2.unit.ordinal})"
            )
            execSQL(
                "INSERT INTO CookingStep (cookingStepId, recipeId, text, time, timeUnit)" +
                        " VALUES (${cs1.cookingStepId}, ${cs1.recipeId}, \"${cs1.text}\", ${cs1.time}, ${cs1.timeUnit.ordinal})"
            )
            execSQL(
                "INSERT INTO CookingStep (cookingStepId, recipeId, text, time, timeUnit)" +
                        " VALUES (${cs2.cookingStepId}, ${cs2.recipeId}, \"${cs2.text}\", ${cs2.time}, ${cs2.timeUnit.ordinal})"
            )
            // Prepare for the next version.
            close()
        }

        // Re-open the database with version 10 and provide MIGRATION_9_10 as the migration process.
        db = helper.runMigrationsAndValidate(TEST_DB, 10, true, AppMigration.MIGRATION_9_10)

        // recipes
        val recipeCursor = db.query("SELECT * FROM Recipe")
        recipeCursor.moveToFirst()

        val rId1 = recipeCursor.getInt(recipeCursor.getColumnIndex("id"))
        val rName1 = recipeCursor.getString(recipeCursor.getColumnIndex("name"))
        val servings1 = recipeCursor.getInt(recipeCursor.getColumnIndex("servings"))
        val categoryString1 = recipeCursor.getString(recipeCursor.getColumnIndex("category"))
        val rCategory1 = RecipeCategory.valueOf(categoryString1)
        val queriedRecipe1 = Recipe(rName1, servings1, rCategory1).apply { id = rId1.toLong() }

        recipeCursor.moveToNext()

        val rId2 = recipeCursor.getInt(recipeCursor.getColumnIndex("id"))
        val rName = recipeCursor.getString(recipeCursor.getColumnIndex("name"))
        val servings2 = recipeCursor.getInt(recipeCursor.getColumnIndex("servings"))
        val categoryString2 = recipeCursor.getString(recipeCursor.getColumnIndex("category"))
        val rCategory2 = RecipeCategory.valueOf(categoryString2)
        val queriedRecipe2 = Recipe(rName, servings2, rCategory2).apply { id = rId2.toLong() }

        assertThat(recipeCursor.count).isEqualTo(2)
        assertThat(queriedRecipe1).isEqualTo(r1)
        assertThat(queriedRecipe2).isEqualTo(r2)

        // ingredients
        val ingredientCursor = db.query("SELECT * FROM Ingredient")
        ingredientCursor.moveToFirst()

        val iId1 = ingredientCursor.getInt(ingredientCursor.getColumnIndex("ingredientId"))
        val iRId1 = ingredientCursor.getInt(ingredientCursor.getColumnIndex("recipeId"))
        val iName1 = ingredientCursor.getString(ingredientCursor.getColumnIndex("name"))
        val iQuantity1 = ingredientCursor.getDouble(ingredientCursor.getColumnIndex("quantity"))
        val iUnitString1 = ingredientCursor.getString(ingredientCursor.getColumnIndex("unit"))
        val iUnit1 = IngredientUnit.valueOf(iUnitString1)
        val queriedIngredient1 = Ingredient(iRId1.toLong(), iName1, iQuantity1, iUnit1)
            .apply { ingredientId = iId1.toLong() }

        ingredientCursor.moveToNext()

        val iId2 = ingredientCursor.getInt(ingredientCursor.getColumnIndex("ingredientId"))
        val iRId2 = ingredientCursor.getInt(ingredientCursor.getColumnIndex("recipeId"))
        val iName2 = ingredientCursor.getString(ingredientCursor.getColumnIndex("name"))
        val iQuantity2 = ingredientCursor.getDouble(ingredientCursor.getColumnIndex("quantity"))
        val iUnitString2 = ingredientCursor.getString(ingredientCursor.getColumnIndex("unit"))
        val iUnit2 = IngredientUnit.valueOf(iUnitString2)
        val queriedIngredient2 = Ingredient(iRId2.toLong(), iName2, iQuantity2, iUnit2)
            .apply { ingredientId = iId2.toLong() }

        assertThat(ingredientCursor.count).isEqualTo(2)
        assertThat(queriedIngredient1).isEqualTo(i1)
        assertThat(queriedIngredient1.recipeId).isEqualTo(i1.recipeId)
        assertThat(queriedIngredient1.ingredientId).isEqualTo(i1.ingredientId)
        assertThat(queriedIngredient2).isEqualTo(i2)
        assertThat(queriedIngredient2.recipeId).isEqualTo(i2.recipeId)
        assertThat(queriedIngredient2.ingredientId).isEqualTo(i2.ingredientId)

        // shopping list items
        val itemCursor = db.query("SELECT * FROM ShoppingListItem")
        itemCursor.moveToFirst()

        val sId1 = itemCursor.getInt(itemCursor.getColumnIndex("id"))
        val sChecked1 = itemCursor.getInt(itemCursor.getColumnIndex("checked"))
        val sName1 = itemCursor.getString(itemCursor.getColumnIndex("name"))
        val sQuantity1 = itemCursor.getDouble(itemCursor.getColumnIndex("quantity"))
        val sUnitString1 = itemCursor.getString(itemCursor.getColumnIndex("unit"))
        val sUnit1 = IngredientUnit.valueOf(sUnitString1)
        val queriedShoppingListItem1 = ShoppingListItem(sName1, sQuantity1, sUnit1).apply {
            id = sId1.toLong()
            checked = sChecked1 == 1
        }

        itemCursor.moveToNext()

        val sId2 = itemCursor.getInt(itemCursor.getColumnIndex("id"))
        val sChecked2 = itemCursor.getInt(itemCursor.getColumnIndex("checked"))
        val sName2 = itemCursor.getString(itemCursor.getColumnIndex("name"))
        val sQuantity2 = itemCursor.getDouble(itemCursor.getColumnIndex("quantity"))
        val sUnitString2 = itemCursor.getString(itemCursor.getColumnIndex("unit"))
        val sUnit2 = IngredientUnit.valueOf(sUnitString2)
        val queriedShoppingListItem2 = ShoppingListItem(sName2, sQuantity2, sUnit2).apply {
            id = sId2.toLong()
            checked = sChecked2 == 1
        }

        assertThat(itemCursor.count).isEqualTo(2)
        assertThat(queriedShoppingListItem1).isEqualTo(s1)
        assertThat(queriedShoppingListItem2).isEqualTo(s2)

        // cooking steps
        val csCursor = db.query("SELECT * FROM CookingStep")
        csCursor.moveToFirst()

        val csId1 = csCursor.getInt(csCursor.getColumnIndex("cookingStepId")).toLong()
        val csRId1 = csCursor.getInt(csCursor.getColumnIndex("recipeId")).toLong()
        val csText1 = csCursor.getString(csCursor.getColumnIndex("text"))
        val csTime1 = csCursor.getInt(csCursor.getColumnIndex("time"))
        val csUnitString1 = csCursor.getString(csCursor.getColumnIndex("timeUnit"))
        val csUnit1 = TimeUnit.valueOf(csUnitString1)
        val queriedCookingStep1 =
            CookingStep(csRId1, csText1, csTime1, csUnit1).apply { cookingStepId = csId1 }

        csCursor.moveToNext()

        val csId2 = csCursor.getInt(csCursor.getColumnIndex("cookingStepId")).toLong()
        val csRId2 = csCursor.getInt(csCursor.getColumnIndex("recipeId")).toLong()
        val csText2 = csCursor.getString(csCursor.getColumnIndex("text"))
        val csTime2 = csCursor.getInt(csCursor.getColumnIndex("time"))
        val csUnitString2 = csCursor.getString(csCursor.getColumnIndex("timeUnit"))
        val csUnit2 = TimeUnit.valueOf(csUnitString2)
        val queriedCookingStep2 =
            CookingStep(csRId2, csText2, csTime2, csUnit2).apply { cookingStepId = csId2 }

        assertThat(csCursor.count).isEqualTo(2)
        assertThat(queriedCookingStep1).isEqualTo(cs1)
        assertThat(queriedCookingStep1.recipeId).isEqualTo(cs1.recipeId)
        assertThat(queriedCookingStep1.cookingStepId).isEqualTo(cs1.cookingStepId)
        assertThat(queriedCookingStep2).isEqualTo(cs2)
        assertThat(queriedCookingStep2.recipeId).isEqualTo(cs2.recipeId)
        assertThat(queriedCookingStep2.cookingStepId).isEqualTo(cs2.cookingStepId)
    }
}
