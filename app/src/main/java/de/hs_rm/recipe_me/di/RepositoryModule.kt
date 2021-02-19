package de.hs_rm.recipe_me.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import de.hs_rm.recipe_me.persistence.dao.RecipeDao
import de.hs_rm.recipe_me.persistence.dao.RecipeOfTheDayDao
import de.hs_rm.recipe_me.persistence.dao.ShoppingListDao
import de.hs_rm.recipe_me.persistence.dao.UserDao
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
    fun provideRecipeImageRepository(@ApplicationContext context: Context) =
        RecipeImageRepository(context)

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
    fun provideUserImageRepository(@ApplicationContext context: Context) =
        UserImageRepository(context)

}
