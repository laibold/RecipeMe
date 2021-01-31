package de.hs_rm.recipe_me.ui.recipe.add

import android.content.Context
import android.text.Editable
import android.widget.EditText
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import de.hs_rm.recipe_me.model.recipe.IngredientUnit
import de.hs_rm.recipe_me.model.recipe.RecipeCategory
import de.hs_rm.recipe_me.persistence.AppDatabase
import de.hs_rm.recipe_me.persistence.RecipeDao
import de.hs_rm.recipe_me.service.RecipeRepository
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AddRecipeViewModelTest {

    private lateinit var db: AppDatabase
    private lateinit var recipeRepository: RecipeRepository
    private lateinit var recipeDao: RecipeDao
    private lateinit var viewModel: AddRecipeViewModel

    private lateinit var appContext: Context

    /**
     * Build inMemory database and create ViewModel
     */
    @Before
    fun init() {
        appContext = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(appContext, AppDatabase::class.java).build()
        recipeDao = db.recipeDao()
        recipeRepository = RecipeRepository(recipeDao)

        viewModel = AddRecipeViewModel(recipeRepository)
    }

    /**
     * Try to set recipe name and servings with invalid values. Function should return false.
     */
    @Test
    fun setRecipeAttributesUnsuccessful() {
        assertFalse(viewModel.setRecipeAttributes("", "", RecipeCategory.SNACKS))
        assertFalse(viewModel.setRecipeAttributes("", "1", RecipeCategory.SNACKS))
        assertFalse(viewModel.setRecipeAttributes("name", "", RecipeCategory.SNACKS))
        assertFalse(viewModel.setRecipeAttributes("name", "0", RecipeCategory.SNACKS))
    }

    /**
     * Set recipe name and servings with valid values. Function should return true.
     */
    @Test
    fun setRecipeAttributesSuccessful() {
        assertTrue(viewModel.setRecipeAttributes("n", "1", RecipeCategory.SNACKS))
    }

    /**
     * Add Ingredients with valid values, check return values and amount of ingredients.
     */
    @Test
    fun addIngredientSuccessful() {
        db.clearAllTables()
        val name = getEditable("Valid Name")
        val quantity1 = getEditable("1.5")
        val quantity2 = getEditable("1,5")
        val unit = IngredientUnit.NONE

        val countBefore = viewModel.ingredients.value?.size!!

        assertTrue(viewModel.addIngredient(name, quantity1, unit))
        assertTrue(viewModel.addIngredient(name, quantity2, unit))

        val countAfter = viewModel.ingredients.value?.size!!

        assertEquals((countBefore + 2), countAfter)
    }

    /**
     * Mock Editable text from EditText
     */
    private fun getEditable(s: String): Editable {
        val editText = EditText(appContext)
        editText.setText(s)
        return editText.text
    }

}
