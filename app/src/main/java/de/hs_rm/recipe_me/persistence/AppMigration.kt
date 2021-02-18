package de.hs_rm.recipe_me.persistence

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migrations as static objects for [AppDatabase]
 */
object AppMigration {

    /**
     * Create table ShoppingListItem
     */
    val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                    "CREATE TABLE IF NOT EXISTS ShoppingListItem(" +
                            " id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                            " checked INTEGER NOT NULL," +
                            " name TEXT NOT NULL," +
                            " quantity REAL NOT NULL," +
                            " unit INTEGER NOT NULL)"
            )
        }
    }

    /**
     * Create table RecipeOfTheDay
     */
    val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                    "CREATE TABLE IF NOT EXISTS RecipeOfTheDay(" +
                            " id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                            " date TEXT NOT NULL," +
                            " recipeId INTEGER NOT NULL," +
                            " FOREIGN KEY(recipeId) REFERENCES Recipe(id)" +
                            " ON UPDATE NO ACTION" +
                            " ON DELETE CASCADE)"
            )
            database.execSQL(
                    "CREATE INDEX index_RecipeOfTheDay_recipeId ON RecipeOfTheDay(recipeId)"
            )
        }
    }

    /**
     * Rename Primary keys from Ingredient and CookingStep,
     * Create table CookingStepIngredientCrossRef
     */
    val MIGRATION_6_7 = object : Migration(6, 7) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Copy content from Ingredient with new primary key name
            database.execSQL("ALTER TABLE Ingredient RENAME TO Ingredient_old")
            database.execSQL(
                    "CREATE TABLE IF NOT EXISTS Ingredient(" +
                            " ingredientId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                            " recipeId INTEGER NOT NULL," +
                            " name TEXT NOT NULL," +
                            " quantity REAL NOT NULL," +
                            " unit INTEGER NOT NULL)"
            )
            database.execSQL(
                    "INSERT INTO Ingredient (ingredientId, recipeId, name, quantity, unit)" +
                            " SELECT id, recipeId, name, quantity, unit" +
                            " FROM Ingredient_old"
            )
            database.execSQL(
                    "DROP TABLE Ingredient_old"
            )

            // Copy content from CookingStep with new primary key name
            database.execSQL("ALTER TABLE CookingStep RENAME TO CookingStep_old")
            database.execSQL(
                    "CREATE TABLE CookingStep (" +
                            " cookingStepId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                            " recipeId INTEGER NOT NULL," +
                            " imageUri TEXT NOT NULL," +
                            " text TEXT NOT NULL," +
                            " time INTEGER NOT NULL," +
                            " timeUnit INTEGER NOT NULL)"
            )
            database.execSQL(
                    "INSERT INTO CookingStep (cookingStepId, recipeId, imageUri, text, time, timeUnit)" +
                            " SELECT id, recipeId, imageUri, text, time, timeUnit" +
                            " FROM CookingStep_old"
            )
            database.execSQL(
                    "DROP TABLE CookingStep_old"
            )

            // Create new cross ref table
            database.execSQL(
                    "CREATE TABLE IF NOT EXISTS CookingStepIngredientCrossRef (" +
                            " cookingStepId INTEGER NOT NULL," +
                            " ingredientId INTEGER NOT NULL," +
                            " PRIMARY KEY(cookingStepId, ingredientId))"
            )
            database.execSQL(
                    "CREATE INDEX index_CookingStepIngredientCrossRef_ingredientId" +
                            " ON CookingStepIngredientCrossRef (ingredientId)"
            )
        }
    }

    /**
     * Create table User
     */
    val MIGRATION_7_8 = object : Migration(7, 8) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                "CREATE TABLE IF NOT EXISTS User (" +
                        " id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                        " name TEXT NOT NULL)"
            )
        }
    }

}
