package de.hs_rm.recipe_me.service

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import de.hs_rm.recipe_me.declaration.getOrAwaitValue
import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

/**
 * https://developer.android.com/training/dependency-injection/hilt-testing
 */
@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class RecipeRepositoryTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Inject
    lateinit var repository: RecipeRepository

    val appContext = InstrumentationRegistry.getInstrumentation().targetContext

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun testInjection() {
        assertNotNull(repository)
    }

    @Test
    fun addRecipeWithRelationsSucceed() {
        //TODO
    }

    @Test
    fun deleteRecipeWithRelationsSucceed() {
        var recipes = repository.getRecipes().getOrAwaitValue()
        val sizeBefore = recipes.size

        // https://proandroiddev.com/testing-kotlin-coroutines-d904738b846d
        runBlocking { repository.deleteRecipeWithRelations(recipes[0]) }

        recipes = repository.getRecipes().getOrAwaitValue()
        val sizeAfter = recipes.size

        assertEquals(sizeBefore, sizeAfter + 1)
    }
}
