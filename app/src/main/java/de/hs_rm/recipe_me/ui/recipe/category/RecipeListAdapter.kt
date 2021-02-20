package de.hs_rm.recipe_me.ui.recipe.category

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.Observable
import androidx.databinding.ObservableBoolean
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import de.hs_rm.recipe_me.databinding.RecipeListitemBinding
import de.hs_rm.recipe_me.declaration.ui.fragments.DeleteRecipeCallbackAdapter
import de.hs_rm.recipe_me.model.recipe.Recipe
import de.hs_rm.recipe_me.service.GlideApp

class RecipeListAdapter(
    private val context: Context,
    private val resource: Int,
    private val objects: List<Recipe>,
    private val viewModel: RecipeCategoryViewModel,
    private val callbackListener: DeleteRecipeCallbackAdapter
) : RecyclerView.Adapter<RecipeListAdapter.RecipeViewHolder>() {

    /**
     * Indicates whether a recipe list item is selected. Other items will observe this value
     * and unset their own selection if value switches to false. This happens when any item is
     * single-clicked (selection disappears completely) or when an item gets long-clicked
     * (selection will "move" to this item).
     */
    var itemSelected = ObservableBoolean(false)

    class RecipeViewHolder(val binding: RecipeListitemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val viewBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            resource,
            parent,
            false
        ) as RecipeListitemBinding

        return RecipeViewHolder(viewBinding)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val recipe = objects[position]

        holder.binding.recipeName.text = recipe.name

        val imageFile = viewModel.getRecipeImageFile(recipe.id)
        if (imageFile != null) {
            GlideApp.with(context)
                .setDefaultRequestOptions(
                    RequestOptions()
                        .error(recipe.category.drawableResId)
                )
                .load(Uri.fromFile(imageFile))
                .signature(ObjectKey(System.currentTimeMillis())) // use timestamp to prevent problems with caching
                .into(holder.binding.recipeImageView)
        } else {
            holder.binding.recipeImageView.setImageResource(recipe.category.drawableResId)
        }

        // on long click remove other selection by setting itemSelected and set selection to this item
        holder.binding.itemWrapper.setOnLongClickListener {
            showOverlay(holder.binding)
            return@setOnLongClickListener true
        }

        // remove selection if itemSelected is set to false
        itemSelected.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(observable: Observable, i: Int) {
                val itemSelected = observable as ObservableBoolean
                if (!itemSelected.get()) {
                    hideOverlay(holder.binding)
                }
            }
        })

        // If item is single-clicked remove selection if present, otherwise navigate to recipe
        holder.binding.itemWrapper.setOnClickListener {
            if (itemSelected.get()) {
                itemSelected.set(false)
            } else {
                val direction = RecipeCategoryFragmentDirections.toRecipeDetailFragment(recipe.id)
                it.findNavController().navigate(direction)
            }
        }

        // Navigate to AddRecipeNavGraph with recipeId to edit the recipe
        holder.binding.editOverlay.editButton.setOnClickListener {
            val direction = RecipeCategoryFragmentDirections.toAddRecipeNavGraph(
                recipeId = recipe.id,
                clearValues = true
            )
            it.findNavController().navigate(direction)
        }

        // Call fragment if delete button is clicked. It will remove the recipe and refresh the list
        holder.binding.editOverlay.deleteButton.setOnClickListener {
            callbackListener.onCallback(objects[position])
        }
    }

    override fun getItemCount() = objects.size

    /**
     * Remove other selection, vibrate and show new selection
     */
    private fun showOverlay(binding: RecipeListitemBinding) {
        itemSelected.set(false)
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            //deprecated in API 26
            vibrator.vibrate(20)
        }

        itemSelected.set(true)
        binding.editOverlay.overlayWrapper.visibility = View.VISIBLE
    }

    /**
     * Remove selection
     */
    private fun hideOverlay(binding: RecipeListitemBinding) {
        binding.editOverlay.overlayWrapper.visibility = View.GONE
        itemSelected.set(false)
    }

    /**
     * Remove selection overlay
     */
    fun removeSelection() {
        itemSelected.set(false)
    }

}
