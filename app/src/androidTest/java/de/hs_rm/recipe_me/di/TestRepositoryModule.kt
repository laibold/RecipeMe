package de.hs_rm.recipe_me.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import de.hs_rm.recipe_me.Constants
import de.hs_rm.recipe_me.persistence.dao.RecipeDao
import de.hs_rm.recipe_me.persistence.dao.RecipeOfTheDayDao
import de.hs_rm.recipe_me.persistence.dao.ShoppingListDao
import de.hs_rm.recipe_me.persistence.dao.UserDao
import de.hs_rm.recipe_me.service.repository.*
import javax.inject.Named
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object TestRepositoryModule {
    private const val TEST = Constants.TEST_NAME

    @Singleton
    @Provides
    @Named(TEST)
    fun provideRecipeRepository(@Named(TEST) recipeDataSource: RecipeDao) =
        RecipeRepository(recipeDataSource)

    @Singleton
    @Provides
    @Named(TEST)
    fun provideRecipeImageRepository(@ApplicationContext context: Context) =
        RecipeImageRepository(context)

    @Singleton
    @Provides
    @Named(TEST)
    fun provideShoppingListRepository(@Named(TEST) shoppingListDataSource: ShoppingListDao) =
        ShoppingListRepository(shoppingListDataSource)

    @Singleton
    @Provides
    @Named(TEST)
    fun provideRecipeOfTheDayRepository(
        @Named(TEST) rotdDataSource: RecipeOfTheDayDao,
        @Named(TEST) recipeDataSource: RecipeDao
    ) = RecipeOfTheDayRepository(rotdDataSource, recipeDataSource)

    @Singleton
    @Provides
    @Named(TEST)
    fun provideUserRepository(
        @Named(TEST) userDataSource: UserDao
    ) = UserRepository(userDataSource)

    @Singleton
    @Provides
    @Named(TEST)
    fun provideUserImageRepository(@ApplicationContext context: Context) =
        UserImageRepository(context)
}
