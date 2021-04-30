package de.hs_rm.recipe_me.ui.recipe.detail

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import de.hs_rm.recipe_me.R
import de.hs_rm.recipe_me.declaration.anyNotNull
import de.hs_rm.recipe_me.declaration.eqNotNull
import de.hs_rm.recipe_me.declaration.espresso.withListSize
import de.hs_rm.recipe_me.declaration.launchFragmentInHiltContainer
import de.hs_rm.recipe_me.di.Constants
import de.hs_rm.recipe_me.model.recipe.*
import de.hs_rm.recipe_me.persistence.AppDatabase
import de.hs_rm.recipe_me.persistence.dao.RecipeDao
import de.hs_rm.recipe_me.service.Formatter
import de.hs_rm.recipe_me.service.PreferenceService
import de.hs_rm.recipe_me.service.repository.ShoppingListRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers.anything
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.*
import test_shared.TestDataProvider
import javax.inject.Inject
import javax.inject.Named

@HiltAndroidTest
class RecipeDetailFragmentTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Inject
    @Named(Constants.TEST_NAME)
    lateinit var db: AppDatabase

    private lateinit var recipeDao: RecipeDao

    private lateinit var context: Context

    @Before
    fun beforeEach() {
        hiltRule.inject()
        assertThat(db.openHelper.databaseName).isEqualTo(AppDatabase.Environment.TEST.dbName)

        context = InstrumentationRegistry.getInstrumentation().targetContext
        db.clearAllTables()
        recipeDao = db.recipeDao()
    }

    @Test
    fun resetsServingsOnDestroy() {
        val recipe1 = Recipe(RecipeCategory.MAIN_DISHES).apply { servings = 3 }
        val recipeId1 = insertTestData(recipe1)
        val recipe2 = Recipe(RecipeCategory.MAIN_DISHES).apply { servings = 5 }
        val recipeId2 = insertTestData(recipe2)

        val args1 = bundleOf("recipeId" to recipeId1)
        var viewModel: RecipeDetailViewModel? = null
        launchFragmentInHiltContainer<RecipeDetailFragment>(args1) {
            val tempViewModel: RecipeDetailViewModel by activityViewModels()
            viewModel = tempViewModel
            viewModel!!.servings.set(4)
            onDestroy()
        }

        val args2 = bundleOf("recipeId" to recipeId2)
        launchFragmentInHiltContainer<RecipeDetailFragment>(args2) {
            val tempViewModel: RecipeDetailViewModel by activityViewModels()
            viewModel = tempViewModel
        }

        assertThat(viewModel!!.servings.get()).isEqualTo(5)
    }

    /**
     * Test if recipe name, servings, ingredients and cooking steps are shown correctly
     */
    @Test
    fun canShowRecipe() {
        PreferenceService(context).setShowCookingStepPreview(true)

        val recipe = Recipe("Recipe Name", 3, RecipeCategory.MAIN_DISHES)
        val ingredients = listOf(
            Ingredient("Ingredient 1", 3.0, IngredientUnit.CAN),
            Ingredient("Ingredient 2", 0.0, IngredientUnit.NONE),
            Ingredient("Ingredient 3", 1.0, IngredientUnit.STICK)
        )
        val cookingSteps = listOf(
            CookingStep("Step 1", 0, TimeUnit.SECOND),
            CookingStep("Step 2", 0, TimeUnit.SECOND),
            CookingStep("Step 3", 0, TimeUnit.SECOND)
        )
        val recipeId = insertTestData(recipe, ingredients, cookingSteps)

        val args = bundleOf("recipeId" to recipeId)
        launchFragmentInHiltContainer<RecipeDetailFragment>(args)

        // name
        onView(withId(R.id.recipe_detail_name)).check(matches(withText("Recipe Name")))
        // servings
        onView(withId(R.id.servings_size)).check(matches(withText("3")))
        onView(withId(R.id.servings_text)).check(matches(withText(context.getString(R.string.servings))))
        // ingredients and formatting
        onView(withId(R.id.ingredients_list_view)).check(matches(withListSize(3)))
        val formattedText = Formatter.formatIngredient(context, ingredients[0])
        onData(anything()).inAdapterView(withId(R.id.ingredients_list_view)).atPosition(0)
            .onChildView(withId(R.id.ingredient_text_view)).check(matches(withText(formattedText)))
        // cooking steps
        val text = "Step 1\n\nStep 2\n\nStep 3"
        onView(withId(R.id.cooking_steps_text)).check(matches(withText(text)))
    }

    /**
     * Test if cooking step preview is hidden depending on preference
     */
    @Test
    fun hidesCookingStepPreviewDependingOnPreference() {
        PreferenceService(context).setShowCookingStepPreview(false)
        val recipe = Recipe(RecipeCategory.MAIN_DISHES)
        val cookingStep = TestDataProvider.getRandomCookingStep()
        val recipeId = insertTestData(recipe, cookingSteps = listOf(cookingStep))

        val args = bundleOf("recipeId" to recipeId)
        launchFragmentInHiltContainer<RecipeDetailFragment>(args)

        onView(withId(R.id.cooking_steps_headline)).check(matches(withEffectiveVisibility(Visibility.GONE)))
        onView(withId(R.id.cooking_steps_text)).check(matches(withText("")))
    }

    /**
     * Test if button for navigation to CookingStepFragment is hidden when recipe doesn't contain cooking steps
     */
    @Test
    fun hidesButtonWhenNoStepsPresent() {
        val recipe = Recipe(RecipeCategory.MAIN_DISHES)
        val recipeId = insertTestData(recipe)

        val args = bundleOf("recipeId" to recipeId)
        launchFragmentInHiltContainer<RecipeDetailFragment>(args)

        onView(withId(R.id.to_cooking_steps_button))
            .check(matches(withEffectiveVisibility(Visibility.GONE)))
    }

    /**
     * Test if servings are changed correctly via buttons
     */
    @Test
    fun canChangeServings() {
        val recipe = Recipe("Recipe Name", 2, RecipeCategory.MAIN_DISHES)
        val ingredients = listOf(
            Ingredient("Ingredient 1", 3.0, IngredientUnit.CAN),
            Ingredient("Ingredient 1", 1.0, IngredientUnit.CAN)
        )
        val recipeId = insertTestData(recipe, ingredients)

        val args = bundleOf("recipeId" to recipeId)
        var viewModel: RecipeDetailViewModel? = null
        launchFragmentInHiltContainer<RecipeDetailFragment>(args) {
            val tempViewModel: RecipeDetailViewModel by activityViewModels()
            viewModel = tempViewModel
        }

        // define matchers
        val servingsSize = onView(withId(R.id.servings_size))
        val servingsText = onView(withId(R.id.servings_text))
        val minusButton = onView(withId(R.id.minus_button))
        val plusButton = onView(withId(R.id.plus_button))
        val ingredientText = onData(anything()).inAdapterView(withId(R.id.ingredients_list_view))
            .atPosition(0)
            .onChildView(withId(R.id.ingredient_text_view))

        // servings
        servingsSize.check(matches(withText("2")))
        servingsText.check(matches(withText(context.getString(R.string.servings))))

        //decrease to 1 serving
        minusButton.perform(click())
        servingsSize.check(matches(withText("1")))
        servingsText.check(matches(withText(context.getString(R.string.serving))))

        var formattedText =
            Formatter.formatIngredient(context, ingredients[0], viewModel!!.getServingsMultiplier())
        ingredientText.check(matches(withText(formattedText)))

        // try to decrease to 0 - size should not change
        minusButton.perform(click())
        servingsSize.check(matches(withText("1")))
        servingsText.check(matches(withText(context.getString(R.string.serving))))

        formattedText =
            Formatter.formatIngredient(context, ingredients[0], viewModel!!.getServingsMultiplier())
        ingredientText.check(matches(withText(formattedText)))

        //increase to 3 servings
        plusButton.perform(click())
        plusButton.perform(click())
        servingsSize.check(matches(withText("3")))
        servingsText.check(matches(withText(context.getString(R.string.servings))))

        formattedText =
            Formatter.formatIngredient(context, ingredients[0], viewModel!!.getServingsMultiplier())
        ingredientText.check(matches(withText(formattedText)))
    }

    /**
     * Test if visibility and checked state for adding ingredients to shopping list behave as expected
     */
    @Test
    fun canSwitchAddIngredientsToShoppingListStates() {
        val recipe = Recipe("Recipe Name", 2, RecipeCategory.MAIN_DISHES)
        val ingredients = listOf(
            Ingredient("Ingredient 1", 3.0, IngredientUnit.CAN),
            Ingredient("Ingredient 1", 1.0, IngredientUnit.CAN)
        )
        val recipeId = insertTestData(recipe, ingredients)

        val args = bundleOf("recipeId" to recipeId)
        launchFragmentInHiltContainer<RecipeDetailFragment>(args)

        // define matchers
        val addToShoppingListButton = onView(withId(R.id.add_to_shopping_list_button))
        val cancelButton = onView(withId(R.id.to_shopping_list_cancel_button))
        val acceptButton = onView(withId(R.id.to_shopping_list_accept_button))
        val checkbox = onData(anything()).inAdapterView(withId(R.id.ingredients_list_view))
            .atPosition(0)
            .onChildView(withId(R.id.item_checkbox))

        // click add to shopping list button and check visibilities
        addToShoppingListButton.perform(click())
        addToShoppingListButton.check(matches(withEffectiveVisibility(Visibility.GONE)))
        cancelButton.check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        acceptButton.check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        checkbox.check(matches(withEffectiveVisibility(Visibility.VISIBLE)))

        // select and deselect first ingredient
        checkbox.perform(click())
        checkbox.check(matches(isChecked()))
        checkbox.perform(click())
        checkbox.check(matches(isNotChecked()))

        // click on cancel button and check visibilities
        cancelButton.perform(click())
        addToShoppingListButton.check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        cancelButton.check(matches(withEffectiveVisibility(Visibility.GONE)))
        acceptButton.check(matches(withEffectiveVisibility(Visibility.GONE)))
        checkbox.check(matches(withEffectiveVisibility(Visibility.GONE)))
    }

    /**
     * Test if ingredients can be added to shopping list
     */
    @Test
    fun canAddIngredientsToShoppingList() {
        val recipe = Recipe("Recipe Name", 2, RecipeCategory.MAIN_DISHES)
        val ingredients = listOf(
            Ingredient("Ingredient 1", 3.0, IngredientUnit.CAN),
            Ingredient("Ingredient 2", 0.0, IngredientUnit.NONE),
            Ingredient("Ingredient 3", 1.0, IngredientUnit.CAN)
        )
        val recipeId = insertTestData(recipe, ingredients)

        val args = bundleOf("recipeId" to recipeId)
        var viewModel: RecipeDetailViewModel? = null
        launchFragmentInHiltContainer<RecipeDetailFragment>(args) {
            val tempViewModel: RecipeDetailViewModel by activityViewModels()
            viewModel = tempViewModel
        }

        // mock ShoppingListRepository
        val shoppingListRepository = mock(ShoppingListRepository::class.java)
        runBlocking {
            `when`(
                shoppingListRepository.addOrUpdateFromIngredient(
                    anyNotNull(Ingredient::class.java),
                    anyNotNull(Double::class.java)
                )
            ).thenAnswer {}
        }
        viewModel!!.shoppingListRepository = shoppingListRepository
        GlobalScope.launch(Dispatchers.Main) { viewModel!!.servings.set(10) }

        // define matchers
        val addToShoppingListButton = onView(withId(R.id.add_to_shopping_list_button))
        val acceptButton = onView(withId(R.id.to_shopping_list_accept_button))
        val checkbox0 = onData(anything()).inAdapterView(withId(R.id.ingredients_list_view))
            .atPosition(0).onChildView(withId(R.id.item_checkbox))
        val checkbox1 = onData(anything()).inAdapterView(withId(R.id.ingredients_list_view))
            .atPosition(1).onChildView(withId(R.id.item_checkbox))
        val checkbox2 = onData(anything()).inAdapterView(withId(R.id.ingredients_list_view))
            .atPosition(2).onChildView(withId(R.id.item_checkbox))

        // click add to shopping list button and select first checkbox
        addToShoppingListButton.perform(click())
        checkbox0.perform(click())
        checkbox0.check(matches(isChecked()))
        checkbox1.perform(click())
        checkbox1.check(matches(isChecked()))

        // click accept and verify function call in repository
        acceptButton.perform(click())
        runBlocking {
            verify(shoppingListRepository, times(1))
                .addOrUpdateFromIngredient(eqNotNull(ingredients[0]), eq(5.0))
            verify(shoppingListRepository, times(1))
                .addOrUpdateFromIngredient(eqNotNull(ingredients[1]), eq(5.0))
        }

        // check if elements are unchecked on second opening
        addToShoppingListButton.perform(click())
        checkbox0.check(matches(isNotChecked()))
        checkbox1.check(matches(isNotChecked()))
        checkbox2.check(matches(isNotChecked()))
    }

    /**
     * Test navigation to CookingStepFragment
     */
    @Test
    fun canNavigateToCookingStepFragment() {
        val recipe = Recipe(RecipeCategory.MAIN_DISHES)
        val cookingStep = TestDataProvider.getRandomCookingStep()
        val recipeId = insertTestData(recipe, cookingSteps = listOf(cookingStep))

        val navController = mock(NavController::class.java)
        val args = bundleOf("recipeId" to recipeId)
        launchFragmentInHiltContainer<RecipeDetailFragment>(args) {
            Navigation.setViewNavController(requireView(), navController)
        }

        onView(withId(R.id.to_cooking_steps_button)).perform(click())
        verify(navController).navigate(RecipeDetailFragmentDirections.toCookingStepFragment(recipeId))
    }

    /**
     * Test navigation to AddRecipeNavGraph with parameters
     */
    @Test
    fun canNavigateToEditRecipe() {
        val recipe = Recipe(RecipeCategory.MAIN_DISHES)
        val cookingStep = TestDataProvider.getRandomCookingStep()
        val recipeId = insertTestData(recipe, cookingSteps = listOf(cookingStep))

        val navController = mock(NavController::class.java)
        val args = bundleOf("recipeId" to recipeId)
        launchFragmentInHiltContainer<RecipeDetailFragment>(args) {
            Navigation.setViewNavController(requireView(), navController)
        }

        onView(withId(R.id.edit_recipe_button)).perform(click())
        verify(navController).navigate(
            RecipeDetailFragmentDirections.toAddRecipeNavGraph(
                recipeId = recipeId,
                clearValues = true
            )
        )
    }

    /////

    /**
     * Add recipe and optionally ingredients and cooking steps to RecipeDao
     */
    private fun insertTestData(
        recipe: Recipe,
        ingredients: List<Ingredient> = listOf(),
        cookingSteps: List<CookingStep> = listOf()
    ): Long {
        var recipeId: Long
        runBlocking {
            recipeId = recipeDao.insert(recipe)

            ingredients.forEach { ingredient ->
                recipeDao.insert(ingredient.apply { this.recipeId = recipeId })
            }

            cookingSteps.forEach { cookingStep ->
                recipeDao.insert(cookingStep.apply { this.recipeId = recipeId })
            }
        }

        return recipeId
    }

}
