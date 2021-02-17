package de.hs_rm.recipe_me.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import de.hs_rm.recipe_me.persistence.AppDatabase
import de.hs_rm.recipe_me.persistence.RecipeDao
import de.hs_rm.recipe_me.persistence.RecipeOfTheDayDao
import de.hs_rm.recipe_me.persistence.ShoppingListDao
import javax.inject.Singleton

/**
 * Dependency Injection Module for data sources like Dao
 */
@InstallIn(SingletonComponent::class)
@Module
object DataSourceModule {

    @Singleton
    @Provides
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getInstance(context)
    }

    @Provides
    fun provideRecipeDao(appDatabase: AppDatabase): RecipeDao {
        return appDatabase.recipeDao()
    }

    @Provides
    fun provideShoppingListDao(appDatabase: AppDatabase): ShoppingListDao {
        return appDatabase.shoppingListDao()
    }

    @Provides
    fun provideRecipeOfTheDayDao(appDatabase: AppDatabase): RecipeOfTheDayDao {
        return appDatabase.recipeOfTheDayDao()
    }

}
