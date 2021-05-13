package de.hs_rm.recipe_me.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import de.hs_rm.recipe_me.persistence.AppDatabase
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

}

object Constants {
    const val TEST_NAME = "test"
}
