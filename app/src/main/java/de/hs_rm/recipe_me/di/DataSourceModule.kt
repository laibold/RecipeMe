package de.hs_rm.recipe_me.di;

import android.content.Context
import dagger.Module;
import dagger.Provides
import dagger.hilt.InstallIn;
import dagger.hilt.android.components.ApplicationComponent;
import dagger.hilt.android.qualifiers.ApplicationContext
import de.hs_rm.recipe_me.persistence.AppDatabase
import de.hs_rm.recipe_me.persistence.RecipeDao
import javax.inject.Singleton

/**
 * Dependency Injection Module for data sources like Dao
 */
@Module
@InstallIn(ApplicationComponent::class)
object DataSourceModule {

    @Provides
    @Singleton
    fun providesRecipeDao(@ApplicationContext context: Context): RecipeDao {
        return AppDatabase.getInstance(context).recipeDao()
    }

}
