package de.hs_rm.recipe_me.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import de.hs_rm.recipe_me.persistence.RecipeDao
import de.hs_rm.recipe_me.persistence.RecipeOfTheDayDao
import de.hs_rm.recipe_me.persistence.ShoppingListDao
import de.hs_rm.recipe_me.persistence.UserDao
import de.hs_rm.recipe_me.service.RecipeOfTheDayRepository
import de.hs_rm.recipe_me.service.RecipeRepository
import de.hs_rm.recipe_me.service.ShoppingListRepository
import de.hs_rm.recipe_me.service.UserRepository
import javax.inject.Singleton

/**
 * Dependency Injection Module for repositories working as Single Source of Truth
 */
@Module
@InstallIn(ApplicationComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun providesRecipeRepository(recipeDataSource: RecipeDao) =
        RecipeRepository(recipeDataSource)

    @Provides
    @Singleton
    fun providesShoppingListRepository(shoppingListDataSource: ShoppingListDao) =
        ShoppingListRepository(shoppingListDataSource)

    @Provides
    @Singleton
    fun providesRecipeOfTheDayRepository(
        rotdDataSource: RecipeOfTheDayDao,
        recipeDataSource: RecipeDao
    ) = RecipeOfTheDayRepository(rotdDataSource, recipeDataSource)

    @Provides
    @Singleton
    fun providesUserRepository(userDataSource: UserDao) =
        UserRepository(userDataSource)
    
}
