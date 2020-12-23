package de.hs_rm.recipe_me.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import de.hs_rm.recipe_me.persistence.RecipeDao
import de.hs_rm.recipe_me.service.RecipeRepository
import javax.inject.Singleton

/**
 * Dependency Injection Module for repositories working as Single Source of Truth
 */
@Module
@InstallIn(ApplicationComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun providesRecipeRepository(recipeDataSource: RecipeDao) = RecipeRepository(recipeDataSource)

}
