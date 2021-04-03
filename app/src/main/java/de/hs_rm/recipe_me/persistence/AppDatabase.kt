package de.hs_rm.recipe_me.persistence

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import de.hs_rm.recipe_me.model.RecipeOfTheDay
import de.hs_rm.recipe_me.model.recipe.CookingStep
import de.hs_rm.recipe_me.model.recipe.Ingredient
import de.hs_rm.recipe_me.model.recipe.Recipe
import de.hs_rm.recipe_me.model.relation.CookingStepIngredientCrossRef
import de.hs_rm.recipe_me.model.shopping_list.ShoppingListItem
import de.hs_rm.recipe_me.model.user.User
import de.hs_rm.recipe_me.persistence.dao.RecipeDao
import de.hs_rm.recipe_me.persistence.dao.RecipeOfTheDayDao
import de.hs_rm.recipe_me.persistence.dao.ShoppingListDao
import de.hs_rm.recipe_me.persistence.dao.UserDao

/**
 * Room Database for this app. Use Daos with Dependency Injection
 */
@Database(
    entities = [
        Recipe::class,
        Ingredient::class,
        CookingStep::class,
        ShoppingListItem::class,
        RecipeOfTheDay::class,
        CookingStepIngredientCrossRef::class,
        User::class
    ],
    version = 9
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun recipeDao(): RecipeDao
    abstract fun shoppingListDao(): ShoppingListDao
    abstract fun recipeOfTheDayDao(): RecipeOfTheDayDao
    abstract fun userDao(): UserDao

    companion object {
        // be careful here!!!
        private const val CREATE_FROM_ASSET = false
        // really careful!!!

        private const val ASSET_NAME = "database/data.db"
        lateinit var env: Environment

        // For Singleton instantiation
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context, env: Environment = Environment.PROD): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context, env).also { instance = it }
            }
        }

        // Create and pre-populate the database. See this article for more details:
        // https://medium.com/google-developers/7-pro-tips-for-room-fbadea4bfbd1#4785
        private fun buildDatabase(context: Context, env: Environment): AppDatabase {
            return Room.databaseBuilder(context, AppDatabase::class.java, env.dbName).also { db ->
                this.env = env

                if (env == Environment.TEST) {
                    // allow main thread queries only in test environment
                    db.allowMainThreadQueries()
                }
                if (CREATE_FROM_ASSET) {
                    db.createFromAsset(ASSET_NAME).fallbackToDestructiveMigration()
                }
            }
                .addMigrations(
                    AppMigration.MIGRATION_4_5,
                    AppMigration.MIGRATION_5_6,
                    AppMigration.MIGRATION_6_7,
                    AppMigration.MIGRATION_7_8,
                    AppMigration.MIGRATION_8_9
                )
                .build()
        }

    }

    /**
     * Enum for environments with database names
     */
    enum class Environment(val dbName: String) {
        PROD("test_db"), TEST("android_test_db")
    }
}
