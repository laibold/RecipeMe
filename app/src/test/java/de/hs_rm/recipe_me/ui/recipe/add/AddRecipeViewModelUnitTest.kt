package de.hs_rm.recipe_me.ui.recipe.add

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.google.common.truth.Truth.assertThat
import de.hs_rm.recipe_me.EditableMock
import de.hs_rm.recipe_me.declaration.MainCoroutineRule
import de.hs_rm.recipe_me.model.recipe.*
import de.hs_rm.recipe_me.model.relation.CookingStepIngredientCrossRef
import de.hs_rm.recipe_me.model.relation.CookingStepWithIngredients
import de.hs_rm.recipe_me.model.relation.RecipeWithRelations
import de.hs_rm.recipe_me.service.repository.RecipeImageRepository
import de.hs_rm.recipe_me.service.repository.RecipeRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.*
import test_shared.TestDataProvider
import test_shared.declaration.getOrAwaitValue

class AddRecipeViewModelUnitTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    @get:Rule
    val coroutineRule = MainCoroutineRule()

    /**
     * Try to set recipe name and servings with invalid values. Function should return false.
     */
    @Test
    fun validatesInvalidRecipeAttributesUnsuccessful() {
        val viewModel = getViewModel(mock(), mock())

        var success = viewModel.setRecipeAttributes("", "", RecipeCategory.SNACKS)
        assertThat(success).isFalse()

        success = viewModel.setRecipeAttributes("", "1", RecipeCategory.SNACKS)
        assertThat(success).isFalse()

        success = viewModel.setRecipeAttributes("name", "", RecipeCategory.SNACKS)
        assertThat(success).isFalse()

        success = viewModel.setRecipeAttributes("name", "0", RecipeCategory.SNACKS)
        assertThat(success).isFalse()
    }

    /**
     * Set recipe name and servings with valid values. Function should return true.
     */
    @Test
    fun validatesValidRecipeAttributesSuccessful() {
        val viewModel = getViewModel(mock(), mock())

        val success = viewModel.setRecipeAttributes("n", "1", RecipeCategory.SNACKS)
        assertThat(success).isTrue()
    }

    /**
     * Add Ingredients with valid values, check return values and amount of ingredients.
     */
    @Test
    fun addsIngredientSuccessful() {
        val viewModel = getViewModel(mock(), mock())

        val name = EditableMock("Valid Name")
        val quantity1 = EditableMock(1.5)
        val quantity2 = EditableMock("1,5")
        val quantity3 = EditableMock("")
        val unit = IngredientUnit.NONE

        val countBefore = viewModel.ingredients.value?.size!!

        assertThat(viewModel.addIngredient(name, quantity1, unit)).isTrue()
        assertThat(viewModel.addIngredient(name, quantity2, unit)).isTrue()
        assertThat(viewModel.addIngredient(name, quantity3, unit)).isTrue()

        val countAfter = viewModel.ingredients.value?.size!!
        assertThat(countAfter).isEqualTo((countBefore + 3))
    }

    /**
     * Add Ingredients with valid name, check return value and amount of ingredients (shouldn't increase).
     */
    @Test
    fun doesNotAddInvalidIngredient() {
        val viewModel = getViewModel(mock(), mock())

        val nameInvalid = EditableMock("")
        val quantityValid = EditableMock(1.5)
        val unit = IngredientUnit.NONE

        val countBefore = viewModel.ingredients.value?.size!!

        assertThat(viewModel.addIngredient(nameInvalid, quantityValid, unit)).isFalse()

        val countAfter = viewModel.ingredients.value?.size!!
        assertThat(countAfter).isEqualTo(countBefore)
    }

    /**
     * Update ingredient with valid values and check them as well as the amount of ingredients
     */
    @Test
    fun updatesValidIngredientSuccessful() {
        val viewModel = getViewModel(mock(), mock())

        insertTestData(viewModel, 3, 0)
        val position = 1
        val name = "new name"
        val quantity = 1.5
        val ingredientUnit = IngredientUnit.CLOVE

        viewModel.prepareIngredientUpdate(position)

        val countBefore = viewModel.ingredients.value?.size!!

        val success = viewModel.updateIngredient(
            EditableMock(name),
            EditableMock(quantity),
            ingredientUnit
        )
        assertThat(success).isTrue()

        val countAfter = viewModel.ingredients.value?.size!!

        assertThat(viewModel.ingredients.value!![position].name).isEqualTo(name)
        assertThat(viewModel.ingredients.value!![position].quantity).isEqualTo(quantity)
        assertThat(viewModel.ingredients.value!![position].unit).isEqualTo(ingredientUnit)

        assertThat(countAfter).isEqualTo(countBefore)
    }

    /**
     * Update ingredient with invalid values and check them as well as the amount of ingredients (shouldn't change)
     */
    @Test
    fun doesNotUpdateInvalidIngredient() {
        val viewModel = getViewModel(mock(), mock())

        insertTestData(viewModel, 3, 0)
        val position = 1
        val name = ""
        val quantity = 1.5
        val ingredientUnit = IngredientUnit.CLOVE

        viewModel.prepareIngredientUpdate(position)

        val countBefore = viewModel.ingredients.value?.size!!

        val success = viewModel.updateIngredient(
            EditableMock(name),
            EditableMock(quantity),
            ingredientUnit
        )
        assertThat(success).isFalse()

        val countAfter = viewModel.ingredients.value?.size!!

        assertThat(viewModel.ingredients.value!![position].name).isNotEqualTo(name)
        assertThat(viewModel.ingredients.value!![position].quantity).isNotEqualTo(quantity)
        assertThat(viewModel.ingredients.value!![position].unit).isNotEqualTo(ingredientUnit)

        assertThat(countAfter).isEqualTo(countBefore)
    }

    /**
     * Test that if an ingredients gets removed in viewModel scope, it wont be assigned to a
     * CookingStep in viewModel scope anymore
     */
    @Test
    fun deletesAssignedIngredientSuccessful() {
        val viewModel = getViewModel(mock(), mock())

        viewModel.addIngredient(EditableMock("Delete"), EditableMock(1.0), IngredientUnit.NONE)
        viewModel.addIngredient(EditableMock("Keep"), EditableMock(1.0), IngredientUnit.NONE)

        val deleteIngredient = viewModel.ingredients.value!![0]
        val keepIngredient = viewModel.ingredients.value!![1]

        viewModel.addCookingStepWithIngredients(
            EditableMock("Text"),
            EditableMock(0),
            TimeUnit.SECOND,
            mutableListOf(deleteIngredient, keepIngredient)
        )

        val ingredientsBefore = viewModel.cookingStepsWithIngredients.value?.get(0)!!.ingredients
        val beforeCountList = ArrayList<Ingredient>(ingredientsBefore)
        assertThat(beforeCountList.size).isEqualTo(2)

        viewModel.ingredients.value!!.remove(deleteIngredient)
        viewModel.prepareCookingStepUpdate(0)

        val ingredientsAfter = viewModel.cookingStepsWithIngredients.value?.get(0)!!.ingredients
        val afterCountList = ArrayList<Ingredient>(ingredientsAfter)
        assertThat(afterCountList.size).isEqualTo(1)
    }

    /**
     * Add CookingStepsWithIngredients with valid values, check return values and amount of cooking steps.
     */
    @Test
    fun addsValidCookingStepSuccessful() {
        val viewModel = getViewModel(mock(), mock())

        val text = EditableMock("Valid Text")
        val time1 = EditableMock(1)
        val time2 = EditableMock("")
        val unit = TimeUnit.SECOND
        val ingredients = mutableListOf(TestDataProvider.getRandomIngredient(0))

        val countBefore = viewModel.cookingStepsWithIngredients.value?.size!!

        assertThat(viewModel.addCookingStepWithIngredients(text, time1, unit, ingredients)).isTrue()
        assertThat(viewModel.addCookingStepWithIngredients(text, time2, unit, ingredients)).isTrue()

        val countAfter = viewModel.cookingStepsWithIngredients.value?.size!!

        assertThat(countAfter).isEqualTo(countBefore + 2)
    }

    /**
     * Add CookingStepWithIngredients with invalid text, check return value and amount of CookingSteps (shouldn't increase).
     */
    @Test
    fun doesNotAddInvalidCookingStep() {
        val viewModel = getViewModel(mock(), mock())

        val textInvalid = EditableMock("")
        val timeValid = EditableMock(3)
        val unit = TimeUnit.MINUTE
        val ingredients = mutableListOf(TestDataProvider.getRandomIngredient(0))

        val countBefore = viewModel.cookingStepsWithIngredients.value?.size!!

        assertThat(
            viewModel.addCookingStepWithIngredients(
                textInvalid,
                timeValid,
                unit,
                ingredients
            )
        ).isFalse()

        val countAfter = viewModel.cookingStepsWithIngredients.value?.size!!
        assertThat(countAfter).isEqualTo(countBefore)
    }

    /**
     * Update CookingStep with valid values and check them as well as the amount of CookingSteps
     */
    @Test
    fun updatesValidCookingStepSuccessful() {
        val viewModel = getViewModel(mock(), mock())

        insertTestData(viewModel, 0, 3)
        val position = 1
        val text = "new text"
        val time = 50
        val unit = TimeUnit.MINUTE
        val ingredients = mutableListOf(TestDataProvider.getRandomIngredient(0))

        viewModel.prepareCookingStepUpdate(position)

        val countBefore = viewModel.cookingStepsWithIngredients.value?.size!!

        val success = viewModel.updateCookingStepWithIngredients(
            EditableMock(text),
            EditableMock(time),
            unit,
            ingredients
        )
        assertThat(success).isTrue()

        val countAfter = viewModel.cookingStepsWithIngredients.value?.size!!

        assertThat(viewModel.cookingStepsWithIngredients.value!![position].cookingStep.text)
            .isEqualTo(text)
        assertThat(viewModel.cookingStepsWithIngredients.value!![position].cookingStep.time)
            .isEqualTo(time)
        assertThat(viewModel.cookingStepsWithIngredients.value!![position].cookingStep.timeUnit)
            .isEqualTo(unit)
        assertThat(viewModel.cookingStepsWithIngredients.value!![position].ingredients.size)
            .isEqualTo(1)

        assertThat(countAfter).isEqualTo(countBefore)
    }

    /**
     * Update CookingStepWithIngredient with invalid values and check them as well as the amount of CookingStep (shouldn't change)
     */
    @Test
    fun doesNotUpdateInvalidCookingStep() {
        val viewModel = getViewModel(mock(), mock())

        insertTestData(viewModel, 2, 3)
        val position = 1
        val text = ""
        val time = 0
        val unit = TimeUnit.MINUTE
        val ingredient = viewModel.ingredients.getOrAwaitValue()[0]
        val ingredients = mutableListOf(ingredient)

        viewModel.prepareCookingStepUpdate(position)

        val countBefore = viewModel.cookingStepsWithIngredients.getOrAwaitValue().size

        val success = viewModel.updateCookingStepWithIngredients(
            EditableMock(text),
            EditableMock(time),
            unit,
            ingredients
        )
        assertThat(success).isFalse()

        val countAfter = viewModel.cookingStepsWithIngredients.getOrAwaitValue().size

        assertThat(countAfter).isEqualTo(countBefore)

        assertThat(viewModel.cookingStepsWithIngredients.value!![position].cookingStep.text)
            .isNotEqualTo(text)
        assertThat(viewModel.cookingStepsWithIngredients.value!![position].cookingStep.time)
            .isNotEqualTo(time)
        assertThat(viewModel.cookingStepsWithIngredients.value!![position].cookingStep.timeUnit)
            .isNotEqualTo(unit)
    }

    /**
     * Test validating name
     */
    @Test
    fun validateNameSuccessful() {
        val viewModel = getViewModel(mock(), mock())
        assertThat(viewModel.validateName(EditableMock("n"))).isEqualTo(0)
    }

    /**
     * Test validating name
     */
    @Test
    fun validateNameUnsuccessful() {
        val viewModel = getViewModel(mock(), mock())
        assertThat(viewModel.validateName(EditableMock(""))).isNotEqualTo(0)
    }

    /**
     * Test validating servings
     */
    @Test
    fun validateServingsSuccessful() {
        val viewModel = getViewModel(mock(), mock())
        assertThat(viewModel.validateServings(EditableMock(1))).isEqualTo(0)
    }

    /**
     * Test validating servings
     */
    @Test
    fun validateServingsUnsuccessful() {
        val viewModel = getViewModel(mock(), mock())
        assertThat(viewModel.validateServings(EditableMock(0))).isNotEqualTo(0)
        assertThat(viewModel.validateServings(EditableMock(""))).isNotEqualTo(0)
    }

    /**
     * Test if a recipe can be created successful after another recipe has been updated before
     */
    @Test
    fun updateAndCreateRecipeSuccessful() {
        val recipeToEditId = 1L
        val ingredients = mutableListOf(
            TestDataProvider.getRandomIngredient(recipeToEditId).apply { ingredientId = 3 },
            TestDataProvider.getRandomIngredient(recipeToEditId).apply { ingredientId = 4 }
        )
        val cookingStepsWithIngredients = listOf(
            CookingStepWithIngredients(
                TestDataProvider.getRandomCookingStep(recipeToEditId).apply { cookingStepId = 5 },
                ingredients.subList(0, 0)
            ),
            CookingStepWithIngredients(
                TestDataProvider.getRandomCookingStep(recipeToEditId).apply { cookingStepId = 6 },
                ingredients
            )
        )
        val recipeToEdit =
            Recipe("name", 1, RecipeCategory.BAKED_GOODS).apply { id = recipeToEditId }
        val recipeWithRelationsToEdit = RecipeWithRelations(
            recipeToEdit,
            ingredients,
            cookingStepsWithIngredients
        )

        val recipeRepository: RecipeRepository = mock {
            onBlocking { insert(any<Recipe>()) } doReturn 0L
            onBlocking { insert(any<Ingredient>()) } doReturn 0L
            onBlocking { insert(any<CookingStep>()) } doReturn 0L
            onBlocking { getRecipeById(recipeToEditId) } doReturn recipeToEdit
            onBlocking { getRecipeWithRelationsById(recipeToEditId) } doReturn
                    MutableLiveData(recipeWithRelationsToEdit)
        }

        val viewModel = getViewModel(recipeRepository, mock())

        viewModel.initRecipe(recipeToEditId)
        viewModel.persistEntities().getOrAwaitValue()
        verifyBlocking(recipeRepository, times(1)) { update(any<Recipe>()) }
        verifyBlocking(recipeRepository, times(2)) { update(any<Ingredient>()) }
        verifyBlocking(recipeRepository, times(2)) { update(any<CookingStep>()) }
        // recipe updated

        viewModel.setCategory(RecipeCategory.MAIN_DISHES)
        viewModel.initRecipe(Recipe.DEFAULT_ID)

        val recipe = viewModel.recipe.getOrAwaitValue()

        assertThat(recipe).isNotNull()
        assertThat(recipe!!.id).isEqualTo(Recipe.DEFAULT_ID)
        assertThat(recipe.category).isEqualTo(RecipeCategory.MAIN_DISHES)
        assertThat(recipe.name).isEqualTo("")
        assertThat(recipe.servings).isEqualTo(0)

        insertTestData(viewModel, 3, 3)
        viewModel.persistEntities().getOrAwaitValue()
        // second recipe saved

        verifyBlocking(recipeRepository, times(1)) { insert(any<Recipe>()) }
        verifyBlocking(recipeRepository, times(3)) { insert(any<Ingredient>()) }
        verifyBlocking(recipeRepository, times(3)) { insert(any<CookingStep>()) }
    }

    /**
     * Test if variables get reset on initRecipe(). This needs to happen to prevent
     * a former recipe to be shown again, because ViewModel has activity scoped lifecycle.
     */
    @Test
    fun clearValuesSuccessful() {
        val viewModel = getViewModel(mock(), mock())
        insertTestData(viewModel, 1, 1)

        viewModel.setRecipeAttributes("Name", "1", RecipeCategory.BREAKFAST)
        viewModel.initRecipe(Recipe.DEFAULT_ID)

        assertThat(viewModel.ingredients.value).isNotNull()
        assertThat(viewModel.ingredients.value!!.size).isEqualTo(0)

        assertThat(viewModel.cookingStepsWithIngredients).isNotNull()
        assertThat(viewModel.cookingStepsWithIngredients.value!!.size).isEqualTo(0)

        assertThat(viewModel.recipe.value!!.name).isEqualTo("")
        // servings is private and can't be tested
        assertThat(viewModel.category.value).isEqualTo(RecipeCategory.MAIN_DISHES)

        // recipeToUpdate is private and can't be tested here
        assertThat(viewModel.recipeToUpdate).isNull()
    }

    /**
     * Test update of all entities
     */
    @Test
    fun updateEntitiesSuccessful() {
        var countingId = 9L

        val recipeToEditId = 1L
        val ingredients = mutableListOf(
            TestDataProvider.getRandomIngredient(recipeToEditId).apply { ingredientId = 3 },
            TestDataProvider.getRandomIngredient(recipeToEditId).apply { ingredientId = 4 },
            TestDataProvider.getRandomIngredient(recipeToEditId).apply { ingredientId = 5 }
        )
        val cookingStepsWithIngredients = listOf(
            CookingStepWithIngredients(
                TestDataProvider.getRandomCookingStep(recipeToEditId).apply { cookingStepId = 6 },
                ingredients.subList(0, 1)
            ),
            CookingStepWithIngredients(
                TestDataProvider.getRandomCookingStep(recipeToEditId).apply { cookingStepId = 7 },
                ingredients
            ),
            CookingStepWithIngredients(
                TestDataProvider.getRandomCookingStep(recipeToEditId).apply { cookingStepId = 8 },
                mutableListOf()
            )
        )
        val recipeToEdit =
            Recipe("name", 1, RecipeCategory.BAKED_GOODS).apply { id = recipeToEditId }
        val recipeWithRelationsToEdit = RecipeWithRelations(
            recipeToEdit,
            ingredients,
            cookingStepsWithIngredients
        )

        val name = "New Name"
        val servings = "6"
        val category = RecipeCategory.BREAKFAST

        val recipeRepository: RecipeRepository = mock {
            onBlocking { insert(any<Recipe>()) } doReturn ++countingId
            onBlocking { insert(any<Ingredient>()) } doReturn ++countingId
            onBlocking { insert(any<CookingStep>()) } doReturn ++countingId
            onBlocking { getRecipeById(recipeToEditId) } doReturn recipeToEdit
            onBlocking { getRecipeWithRelationsById(recipeToEditId) } doReturn
                    MutableLiveData(recipeWithRelationsToEdit)
        }

        val viewModel = getViewModel(recipeRepository, mock())

        viewModel.initRecipe(recipeToEditId)

        // change name, servings and category
        viewModel.setRecipeAttributes(name, servings, category)

        // add ingredient
        viewModel.addIngredient(
            EditableMock("New Ingredient"),
            EditableMock(1),
            IngredientUnit.NONE
        )
        val addedIngredient = viewModel.ingredients.getOrAwaitValue().last()
        // delete ingredient
        val deletedIngredient = viewModel.ingredients.getOrAwaitValue()[1]
        viewModel.ingredients.value!!.removeAt(1)
        // edit ingredient
        viewModel.prepareIngredientUpdate(0)
        viewModel.updateIngredient(
            EditableMock("Updated Ingredient"),
            EditableMock(2),
            IngredientUnit.CAN
        )

        // add step
        viewModel.addCookingStepWithIngredients(
            EditableMock("New Step"),
            EditableMock(""),
            TimeUnit.SECOND,
            mutableListOf()
        )
        val addedCookingStep = viewModel.cookingStepsWithIngredients.getOrAwaitValue().last()
        // edit step
        viewModel.prepareCookingStepUpdate(0)
        viewModel.updateCookingStepWithIngredients(
            EditableMock("Updated Step"),
            EditableMock(""),
            TimeUnit.MINUTE,
            mutableListOf()
        )
        // delete step
        val deletedCookingStep =
            viewModel.cookingStepsWithIngredients.getOrAwaitValue()[2].cookingStep
        viewModel.cookingStepsWithIngredients.value!!.removeAt(2)
        // add ingredients to step
        viewModel.cookingStepsWithIngredients.getOrAwaitValue()[0].ingredients.add(ingredients[1])
        viewModel.cookingStepsWithIngredients.getOrAwaitValue()[0].ingredients.add(ingredients[2])
        // remove ingredient from step
        viewModel.cookingStepsWithIngredients.getOrAwaitValue()[1].ingredients.removeAt(0)

        viewModel.persistEntities().getOrAwaitValue()

        // assert recipe updating
        verifyBlocking(recipeRepository, times(1)) { update(recipeToEdit) }

        // assert ingredient adding, updating and deleting
        assertThat(viewModel.ingredients.getOrAwaitValue()).hasSize(3)
        verifyBlocking(recipeRepository, times(2)) { update(any<Ingredient>()) }
        verifyBlocking(recipeRepository, times(1)) { insert(addedIngredient) }
        verifyBlocking(recipeRepository, times(1)) { deleteIngredient(deletedIngredient) }

        // assert cooking step adding, updating and deleting
        assertThat(viewModel.cookingStepsWithIngredients.getOrAwaitValue()).hasSize(3)
        verifyBlocking(recipeRepository, times(2)) { update(any<CookingStep>()) }
        verifyBlocking(recipeRepository, times(1)) { insert(addedCookingStep.cookingStep) }
        verifyBlocking(recipeRepository, times(1)) { deleteCookingStep(deletedCookingStep) }
        verifyBlocking(recipeRepository, times(3)) {
            deleteCookingStepIngredientCrossRefs(any(), any())
        }

        verifyBlocking(recipeRepository, times(4)) { insert(any<CookingStepIngredientCrossRef>()) }
    }

    /////

    private fun getViewModel(
        recipeRepository: RecipeRepository,
        recipeImageRepository: RecipeImageRepository
    ): AddRecipeViewModel {
        val viewModel = AddRecipeViewModel(recipeRepository, recipeImageRepository)
        viewModel.setCategory(RecipeCategory.MAIN_DISHES)
        viewModel.initRecipe(Recipe.DEFAULT_ID)

        viewModel.recipe.getOrAwaitValue()
        viewModel.ingredients.getOrAwaitValue()
        viewModel.cookingStepsWithIngredients.getOrAwaitValue()

        return viewModel
    }

    /**
     * Insert as many random ingredients and cooking steps to ViewModel as wanted.
     *
     * @param ingredients amount of ingredients to be inserted
     * @param cookingSteps amount of cookingSteps to be inserted
     */
    private fun insertTestData(viewModel: AddRecipeViewModel, ingredients: Int, cookingSteps: Int) {
        runBlocking {
            for (j in 1..ingredients) {
                viewModel.addIngredient(
                    EditableMock("Inserted name $j"),
                    EditableMock("$j"),
                    IngredientUnit.PINCH
                )
            }
            for (j in 1..cookingSteps) {
                viewModel.addCookingStepWithIngredients(
                    EditableMock("Inserted text $j"),
                    EditableMock("$j"),
                    TimeUnit.HOUR,
                    mutableListOf()
                )
            }
        }
    }

}
