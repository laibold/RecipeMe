package de.hs_rm.recipe_me.service

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import de.hs_rm.recipe_me.model.recipe.IngredientUnit
import de.hs_rm.recipe_me.model.shopping_list.ShoppingListItem
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.runner.RunWith
import javax.inject.Inject

/**
 * https://developer.android.com/training/dependency-injection/hilt-testing
 */
@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class ShoppingListRepositoryTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Inject
    lateinit var repository: ShoppingListRepository

    private lateinit var appContext: Context
    private val testIds = mutableListOf<Long>()

    @Before
    fun init() {
        hiltRule.inject()
        appContext = InstrumentationRegistry.getInstrumentation().targetContext
        insertTestItems()
    }

    @After
    fun cleanup() {
        for (id in testIds) {
            runBlocking {
                repository.deleteItemById(id)
            }
        }
    }

    @Test
    fun testInjection() {
        Assert.assertNotNull(repository)
    }

    fun testAddOrUpdate() {
        // TODO
    }

    fun testFindExisting() {
        // TODO
    }

    private fun insertTestItems() {
        runBlocking {
            testIds.add(repository.insertItem(ShoppingListItem("Item", 3.0, IngredientUnit.GRAM)))
        }
    }

}
