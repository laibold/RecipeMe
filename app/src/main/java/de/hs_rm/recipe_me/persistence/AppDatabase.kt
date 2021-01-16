package de.hs_rm.recipe_me.persistence

import android.content.Context
import androidx.room.*
import de.hs_rm.recipe_me.model.recipe.CookingStep
import de.hs_rm.recipe_me.model.recipe.Ingredient
import de.hs_rm.recipe_me.model.recipe.Recipe

/**
 * Room Database for this app. Use Daos with Dependency Injection
 */
@Database(entities = [Recipe::class, Ingredient::class, CookingStep::class], version = 4)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun recipeDao(): RecipeDao

    companion object {
        private const val DATABASE_NAME = "test_db"
        private const val ASSET_NAME = "database/data.db"

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
                //.createFromAsset(ASSET_NAME)
                .build()
        }

    }
}
