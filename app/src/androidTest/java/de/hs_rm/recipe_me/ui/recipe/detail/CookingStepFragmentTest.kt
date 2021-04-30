package de.hs_rm.recipe_me.ui.recipe.detail

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import de.hs_rm.recipe_me.R
import de.hs_rm.recipe_me.declaration.espresso.withListSize
import de.hs_rm.recipe_me.declaration.launchFragmentInHiltContainer
import de.hs_rm.recipe_me.di.Constants
import de.hs_rm.recipe_me.model.recipe.*
import de.hs_rm.recipe_me.model.relation.CookingStepIngredientCrossRef
import de.hs_rm.recipe_me.persistence.AppDatabase
import de.hs_rm.recipe_me.persistence.dao.RecipeDao
import de.hs_rm.recipe_me.service.Formatter
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers.anything
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject
import javax.inject.Named

@HiltAndroidTest
class CookingStepFragmentTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Inject
    @Named(Constants.TEST_NAME)
    lateinit var db: AppDatabase

    lateinit var recipeDao: RecipeDao

    private lateinit var context: Context

    @Before
    fun beforeEach() {
        hiltRule.inject()
        Truth.assertThat(db.openHelper.databaseName).isEqualTo(AppDatabase.Environment.TEST.dbName)

        context = InstrumentationRegistry.getInstrumentation().targetContext
        db.clearAllTables()
        recipeDao = db.recipeDao()
    }

    /**
     * Test representation of Recipe in CookingStepFragment including servings multiplier
     */
    @Test
    fun canShowCookingSteps() {
        val recipe = Recipe("Recipe Name", 2, RecipeCategory.MAIN_DISHES)
        val ingredients = listOf(
            Ingredient("Potatoes", 100.0, IngredientUnit.STICK),
            Ingredient("Sugar", 0.0, IngredientUnit.NONE),
            Ingredient("Garlic", 10.0, IngredientUnit.CLOVE)
        )
        val cookingSteps = listOf(
            CookingStep("Prepare your kitchen", 25, TimeUnit.MINUTE),
            CookingStep("Do something with ingredients", 1, TimeUnit.SECOND),
            CookingStep("Cleanup before guests come", CookingStep.DEFAULT_TIME, TimeUnit.SECOND)
        )
        var recipeId: Long
        runBlocking {
            recipeId = recipeDao.insert(recipe)
            val ingredientIds = mutableListOf<Long>()
            val cookingStepIds = mutableListOf<Long>()
            ingredients.forEach { ingredient ->
                ingredientIds.add(
                    recipeDao.insert(ingredient.apply { this.recipeId = recipeId })
                )
            }
            cookingSteps.forEach { step ->
                cookingStepIds.add(
                    recipeDao.insert(step.apply { this.recipeId = recipeId })
                )
            }
            ingredientIds.forEach { ingredientId ->
                recipeDao.insert(CookingStepIngredientCrossRef(cookingStepIds[0], ingredientId))
            }
            recipeDao.insert(CookingStepIngredientCrossRef(cookingStepIds[1], ingredientIds[1]))
        }

        val args = bundleOf("recipeId" to recipeId)
        var viewModel: RecipeDetailViewModel? = null
        launchFragmentInHiltContainer<CookingStepFragment>(args) {
            val tempViewModel: RecipeDetailViewModel by activityViewModels()
            tempViewModel.servings.set(4)
            viewModel = tempViewModel
        }

        // recipe name
        onView(withId(R.id.headline)).check(matches(withText(recipe.name)))

        // number of steps
        onView(withId(R.id.cooking_step_list_view)).check(matches(withListSize(3)))

        val multiplier = viewModel!!.getServingsMultiplier()
        // step1
        val step1 =
            onData(anything()).inAdapterView(withId(R.id.cooking_step_list_view)).atPosition(0)
        step1.onChildView(withId(R.id.cooking_step_number)).check(matches(withText("1")))
        var formattedIngredients = Formatter.formatIngredientList(context, ingredients, multiplier)
        step1.onChildView(withId(R.id.assigned_ingredients_text_view))
            .check(matches(withText(formattedIngredients)))
        step1.onChildView(withId(R.id.cooking_step_text))
            .check(matches(withText("Prepare your kitchen")))
        var timerText = "25 ${TimeUnit.MINUTE.getNumberString(context.resources, 25)}"
        step1.onChildView(withId(R.id.timer_text)).check(matches(withText(timerText)))

        // step2
        val step2 =
            onData(anything()).inAdapterView(withId(R.id.cooking_step_list_view)).atPosition(1)
        step2.onChildView(withId(R.id.cooking_step_number)).check(matches(withText("2")))
        formattedIngredients =
            Formatter.formatIngredientList(context, ingredients.subList(1, 2), multiplier)
        step2.onChildView(withId(R.id.assigned_ingredients_text_view))
            .check(matches(withText(formattedIngredients)))
        step2.onChildView(withId(R.id.cooking_step_text))
            .check(matches(withText(cookingSteps[1].text)))
        timerText = "1 ${TimeUnit.SECOND.getNumberString(context.resources, 1)}"
        step2.onChildView(withId(R.id.timer_text)).check(matches(withText(timerText)))

        // step3
        val step3 =
            onData(anything()).inAdapterView(withId(R.id.cooking_step_list_view)).atPosition(2)
        step3.onChildView(withId(R.id.cooking_step_number)).check(matches(withText("3")))
        step3.onChildView(withId(R.id.assigned_ingredients_text_view)).check(matches(withText("")))
        step3.onChildView(withId(R.id.cooking_step_text))
            .check(matches(withText(cookingSteps[2].text)))
        step3.onChildView(withId(R.id.timer_element))
            .check(matches(withEffectiveVisibility(Visibility.GONE)))
    }

}
