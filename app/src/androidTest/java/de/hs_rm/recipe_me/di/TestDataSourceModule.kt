package de.hs_rm.recipe_me.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import de.hs_rm.recipe_me.Constants
import de.hs_rm.recipe_me.persistence.AppDatabase
import de.hs_rm.recipe_me.persistence.dao.RecipeDao
import de.hs_rm.recipe_me.persistence.dao.RecipeOfTheDayDao
import de.hs_rm.recipe_me.persistence.dao.ShoppingListDao
import de.hs_rm.recipe_me.persistence.dao.UserDao
import javax.inject.Named

@InstallIn(SingletonComponent::class)
@Module
object TestDataSourceModule {
    private const val TEST = Constants.TEST_NAME

    @Provides
    @Named(TEST)
    fun provideTestDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getInstance(context, AppDatabase.Environment.TEST )
    }

    @Provides
    @Named(TEST)
    fun provideTestRecipeDao(@Named(TEST) appDatabase: AppDatabase): RecipeDao {
        return appDatabase.recipeDao()
    }

    @Provides
    @Named(TEST)
    fun provideTestShoppingListDao(@Named(TEST) appDatabase: AppDatabase): ShoppingListDao {
        return appDatabase.shoppingListDao()
    }

    @Provides
    @Named(TEST)
    fun provideTestRecipeOfTheDayDao(@Named(TEST) appDatabase: AppDatabase): RecipeOfTheDayDao {
        return appDatabase.recipeOfTheDayDao()
    }

    @Provides
    @Named(TEST)
    fun provideTestUserDao(@Named(TEST) appDatabase: AppDatabase): UserDao {
        return appDatabase.userDao()
    }

}
