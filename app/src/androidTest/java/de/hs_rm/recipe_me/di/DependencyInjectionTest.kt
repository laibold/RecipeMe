package de.hs_rm.recipe_me.di

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import de.hs_rm.recipe_me.persistence.RecipeDao
import de.hs_rm.recipe_me.persistence.ShoppingListDao
import de.hs_rm.recipe_me.service.repository.RecipeOfTheDayRepository
import de.hs_rm.recipe_me.service.repository.RecipeRepository
import de.hs_rm.recipe_me.service.repository.ShoppingListRepository
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

/**
 * Test Dependency Injection with Dagger Hilt
 * https://developer.android.com/training/dependency-injection/hilt-testing
 */
@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class DependencyInjectionTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Inject
    lateinit var recipeOfTheDayRepository: RecipeOfTheDayRepository

    @Inject
    lateinit var recipeRepository: RecipeRepository

    @Inject
    lateinit var shoppingListRepository: ShoppingListRepository

    @Inject
    lateinit var recipeDao: RecipeDao

    @Inject
    lateinit var recipeOfTheDayDao: RecipeDao

    @Inject
    lateinit var shoppingListDao: ShoppingListDao

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun testRepositoryInjections() {
        assertNotNull(recipeOfTheDayRepository)
        assertNotNull(recipeRepository)
        assertNotNull(shoppingListRepository)
    }

    @Test
    fun testDaoInjections() {
        assertNotNull(recipeDao)
        assertNotNull(recipeOfTheDayDao)
        assertNotNull(shoppingListDao)
    }

}
