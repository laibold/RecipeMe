package de.hs_rm.recipe_me.persistence

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import de.hs_rm.recipe_me.model.recipe.Recipe
import de.hs_rm.recipe_me.model.recipe.RecipeCategory
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
        val r2 = Recipe("Recipe 2", 2, RecipeCategory.SALADS).apply { id = 2 }

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

            // Prepare for the next version.
            close()
        }

        // Re-open the database with version 2 and provide MIGRATION_9_10 as the migration process.
        db = helper.runMigrationsAndValidate(TEST_DB, 10, true, AppMigration.MIGRATION_9_10)

        val recipeCursor = db.query("SELECT * FROM Recipe")
        recipeCursor.moveToFirst()

        val id1 = recipeCursor.getInt(recipeCursor.getColumnIndex("id"))
        val name1 = recipeCursor.getString(recipeCursor.getColumnIndex("name"))
        val servings1 = recipeCursor.getInt(recipeCursor.getColumnIndex("servings"))
        val categoryString1 = recipeCursor.getString(recipeCursor.getColumnIndex("category"))
        val category1 = RecipeCategory.valueOf(categoryString1)
        val queriedRecipe1 = Recipe(name1, servings1, category1).apply { id = id1.toLong() }

        recipeCursor.moveToNext()

        val id2 = recipeCursor.getInt(recipeCursor.getColumnIndex("id"))
        val name2 = recipeCursor.getString(recipeCursor.getColumnIndex("name"))
        val servings2 = recipeCursor.getInt(recipeCursor.getColumnIndex("servings"))
        val categoryString2 = recipeCursor.getString(recipeCursor.getColumnIndex("category"))
        val category2 = RecipeCategory.valueOf(categoryString2)
        val queriedRecipe2 = Recipe(name2, servings2, category2).apply { id = id2.toLong() }

        assertThat(recipeCursor.count).isEqualTo(2)
        assertThat(queriedRecipe1).isEqualTo(r1)
        assertThat(queriedRecipe2).isEqualTo(r2)
    }
}
