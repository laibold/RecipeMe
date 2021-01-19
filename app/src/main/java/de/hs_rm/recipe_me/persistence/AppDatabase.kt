package de.hs_rm.recipe_me.persistence

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import de.hs_rm.recipe_me.model.recipe.CookingStep
import de.hs_rm.recipe_me.model.recipe.Ingredient
import de.hs_rm.recipe_me.model.recipe.Recipe
import de.hs_rm.recipe_me.model.shopping_list.ShoppingListItem

/**
 * Room Database for this app. Use Daos with Dependency Injection
 */
@Database(
    entities = [Recipe::class, Ingredient::class, CookingStep::class, ShoppingListItem::class],
    version = 5
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun recipeDao(): RecipeDao

    companion object {
        private const val DATABASE_NAME = "test_db"
//        private const val ASSET_NAME = "database/data.db"

        // For Singleton instantiation
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        // Create and pre-populate the database. See this article for more details:
        // https://medium.com/google-developers/7-pro-tips-for-room-fbadea4bfbd1#4785
        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
//                .createFromAsset(ASSET_NAME)
                .addMigrations(MIGRATION_4_5)
                .build()
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `ShoppingListItem`" +
                            " (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                            " `checked` INTEGER NOT NULL," +
                            " `ingredients` TEXT NOT NULL)"
                )
            }
        }
    }
}
