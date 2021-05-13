package de.hs_rm.recipe_me.ui.recipe.add

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import de.hs_rm.recipe_me.declaration.toEditable
import de.hs_rm.recipe_me.model.recipe.IngredientUnit
import de.hs_rm.recipe_me.model.recipe.Recipe
import de.hs_rm.recipe_me.model.recipe.RecipeCategory
import de.hs_rm.recipe_me.model.recipe.TimeUnit
import de.hs_rm.recipe_me.persistence.AppDatabase
import de.hs_rm.recipe_me.service.repository.RecipeImageRepository
import de.hs_rm.recipe_me.service.repository.RecipeRepository
import kotlinx.coroutines.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import test_shared.declaration.getOrAwaitValue

/**
 * Functional tests for [AddRecipeViewModel]
 */
class AddRecipeViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    lateinit var db: AppDatabase

    private lateinit var recipeRepository: RecipeRepository
    private lateinit var recipeImageRepository: RecipeImageRepository

    private lateinit var viewModel: AddRecipeViewModel

    private lateinit var appContext: Context

    /**
     * Build inMemory database and create ViewModel
     */
    @Before
    fun beforeEach() {
        appContext = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(appContext, AppDatabase::class.java)
            .allowMainThreadQueries().build()
        val dao = db.recipeDao()
        recipeRepository = RecipeRepository(dao)

        recipeImageRepository = mock(RecipeImageRepository::class.java)

        viewModel = AddRecipeViewModel(recipeRepository, recipeImageRepository)
        viewModel.setCategory(RecipeCategory.MAIN_DISHES)
        viewModel.initRecipe(Recipe.DEFAULT_ID)

        viewModel.recipe.getOrAwaitValue()
        viewModel.ingredients.getOrAwaitValue()
        viewModel.cookingStepsWithIngredients.getOrAwaitValue()
    }

    /**
     * Test scenario:
     * Recipe with 3 Ingredients as been persisted. All 3 Ingredients are assigned to a CookingStep.
     * One Ingredient gets removed, one updated, one unchanged; User moves to CookingStep Fragment to edit the CookingStep.
     * Check if the ingredients assigned to the CookingStep are up-to-date
     */
    @Test
    fun updateAssignedIngredientsSuccessful() {
        val numberOfChildren = 3
        val cookingStepIndex = 0
        val updateIndex = 0

        insertTestData(numberOfChildren, numberOfChildren)

        viewModel.prepareCookingStepUpdate(cookingStepIndex)

        // Assign all 3 ingredients to CookingStep
        val ingredient0 = viewModel.ingredients.value!![0]
        val ingredient1 = viewModel.ingredients.value!![1]
        val ingredient2 = viewModel.ingredients.value!![2]
        val assignedIngredients = mutableListOf(ingredient0, ingredient1, ingredient2)

        viewModel.updateCookingStepWithIngredients(
            "CookingStepName".toEditable(),
            (0).toEditable(),
            TimeUnit.SECOND,
            assignedIngredients
        )

        // Persist recipe
        val id = viewModel.persistEntities().getOrAwaitValue()

        GlobalScope.launch(Dispatchers.Main) {
            delay(500)
            viewModel.initRecipe(id)
        }

        viewModel.recipe.getOrAwaitValue()
        viewModel.ingredients.getOrAwaitValue()
        viewModel.cookingStepsWithIngredients.getOrAwaitValue()

        val newName = "New Name"
        val newQuantity = 6.0
        val newIngredientUnit = IngredientUnit.DASH

        // Update ingredient at index 0
        viewModel.prepareIngredientUpdate(updateIndex)
        viewModel.updateIngredient(
            newName.toEditable(),
            newQuantity.toEditable(),
            newIngredientUnit
        )

        //Remove ingredient at index 2
        viewModel.ingredients.value!!.removeAt(2)

        // This happens when CookingStepDialog gets opened - Assigned ingredients will be refreshed
        viewModel.prepareCookingStepUpdate(cookingStepIndex)

        val cookingStepWithIngredients =
            viewModel.cookingStepsWithIngredients.value!![cookingStepIndex]

        assertThat(cookingStepWithIngredients.ingredients.size).isEqualTo(2)

        val updatedAssignedIngredient = cookingStepWithIngredients.ingredients[updateIndex]
        assertThat(updatedAssignedIngredient.name).isEqualTo(newName)
        assertThat(updatedAssignedIngredient.quantity).isEqualTo(newQuantity)
        assertThat(updatedAssignedIngredient.unit).isEqualTo(newIngredientUnit)

        val unchangedIngredient = cookingStepWithIngredients.ingredients[1]
        assertThat(unchangedIngredient == ingredient1).isTrue()
    }

    /**
     * Test persisting of entities and count them
     * https://stackoverflow.com/questions/51810330/testing-livedata-transformations
     */
    @Test
    fun persistEntitiesSuccessful() {
        val numberOfChildren = 3

        insertTestData(numberOfChildren, numberOfChildren)

        val recipe = viewModel.recipe.getOrAwaitValue()

        assertThat(recipe).isNotNull()

        val id = viewModel.persistEntities().getOrAwaitValue()

        assertThat(id).isNotEqualTo(0L)
        assertThat(recipeRepository.getRecipeTotal().getOrAwaitValue()).isEqualTo(1)

        val recipeWithRelations = recipeRepository.getRecipeWithRelationsById(id).getOrAwaitValue()

        assertThat(recipeWithRelations.ingredients.size).isEqualTo(numberOfChildren)
        assertThat(recipeWithRelations.cookingStepsWithIngredients.size).isEqualTo(numberOfChildren)
    }

    /////

    /**
     * Insert as many random ingredients and cooking steps to ViewModel as wanted.
     *
     * @param ingredients amount of ingredients to be inserted
     * @param cookingSteps amount of cookingSteps to be inserted
     */
    private fun insertTestData(ingredients: Int, cookingSteps: Int) {
        runBlocking {
            for (j in 1..ingredients) {
                viewModel.addIngredient(
                    "Inserted name $j".toEditable(),
                    "$j".toEditable(),
                    IngredientUnit.PINCH
                )
            }
            for (j in 1..cookingSteps) {
                viewModel.addCookingStepWithIngredients(
                    "Inserted text $j".toEditable(),
                    "$j".toEditable(),
                    TimeUnit.HOUR,
                    mutableListOf()
                )
            }
        }
    }

}
