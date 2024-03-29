package de.hs_rm.recipe_me.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import de.hs_rm.recipe_me.persistence.AppDatabase
import de.hs_rm.recipe_me.persistence.dao.RecipeDao
import de.hs_rm.recipe_me.persistence.dao.RecipeOfTheDayDao
import de.hs_rm.recipe_me.persistence.dao.ShoppingListDao
import de.hs_rm.recipe_me.persistence.dao.UserDao
import de.hs_rm.recipe_me.service.BackupService
import de.hs_rm.recipe_me.service.ImageHandler
import de.hs_rm.recipe_me.service.PreferenceService
import de.hs_rm.recipe_me.service.repository.*
import javax.inject.Singleton

/**
 * Dependency Injection Module for repositories working as Single Source of Truth
 */
@InstallIn(SingletonComponent::class)
@Module
object RepositoryModule {

    @Singleton
    @Provides
    fun provideRecipeRepository(recipeDataSource: RecipeDao) =
        RecipeRepository(recipeDataSource)

    @Singleton
    @Provides
    fun provideRecipeImageRepository(imageHandler: ImageHandler) =
        RecipeImageRepository(imageHandler)

    @Singleton
    @Provides
    fun provideShoppingListRepository(shoppingListDataSource: ShoppingListDao) =
        ShoppingListRepository(shoppingListDataSource)

    @Singleton
    @Provides
    fun provideRecipeOfTheDayRepository(
        rotdDataSource: RecipeOfTheDayDao,
        recipeDataSource: RecipeDao
    ) = RecipeOfTheDayRepository(rotdDataSource, recipeDataSource)

    @Singleton
    @Provides
    fun provideUserRepository(
        userDataSource: UserDao
    ) = UserRepository(userDataSource)

    @Singleton
    @Provides
    fun provideUserImageRepository(imageHandler: ImageHandler) =
        UserImageRepository(imageHandler)

    @Singleton
    @Provides
    fun provideBackupService(
        @ApplicationContext context: Context,
        db: AppDatabase,
        preferenceService: PreferenceService,
        imageHandler: ImageHandler
    ) = BackupService(context, db, preferenceService, imageHandler)

    @Singleton
    @Provides
    fun providePreferenceService(@ApplicationContext context: Context) = PreferenceService(context)

}
