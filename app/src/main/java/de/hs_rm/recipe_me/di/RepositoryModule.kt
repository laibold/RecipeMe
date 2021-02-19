package de.hs_rm.recipe_me.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.hs_rm.recipe_me.persistence.RecipeDao
import de.hs_rm.recipe_me.persistence.RecipeOfTheDayDao
import de.hs_rm.recipe_me.persistence.ShoppingListDao
import de.hs_rm.recipe_me.persistence.UserDao
import de.hs_rm.recipe_me.service.repository.RecipeOfTheDayRepository
import de.hs_rm.recipe_me.service.repository.RecipeRepository
import de.hs_rm.recipe_me.service.repository.ShoppingListRepository
import de.hs_rm.recipe_me.service.UserRepository
import javax.inject.Singleton

/**
 * Dependency Injection Module for repositories working as Single Source of Truth
 */
@InstallIn(SingletonComponent::class)
@Module
object RepositoryModule {

    @Singleton
    @Provides
    fun providesRecipeRepository(recipeDataSource: RecipeDao) =
        RecipeRepository(recipeDataSource)

    @Singleton
    @Provides
    fun providesShoppingListRepository(shoppingListDataSource: ShoppingListDao) =
        ShoppingListRepository(shoppingListDataSource)

    @Singleton
    @Provides
    fun providesRecipeOfTheDayRepository(
        rotdDataSource: RecipeOfTheDayDao,
        recipeDataSource: RecipeDao
    ) = RecipeOfTheDayRepository(rotdDataSource, recipeDataSource)

    @Singleton
    @Provides
    fun providesUserRepository(userDataSource: UserDao) =
        UserRepository(userDataSource)

}
