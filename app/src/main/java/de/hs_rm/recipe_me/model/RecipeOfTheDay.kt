package de.hs_rm.recipe_me.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE
import androidx.room.PrimaryKey
import de.hs_rm.recipe_me.model.recipe.Recipe
import java.time.LocalDate

/**
 * Entity for recipe of the day. Holds a date field and a foreign key for a recipe
 */
@Entity(
    foreignKeys = [ForeignKey(
        entity = Recipe::class,
        parentColumns = ["id"],
        childColumns = ["recipeId"],
        onDelete = CASCADE,
    )]
)
class RecipeOfTheDay(
    var date: LocalDate,
    @ColumnInfo(index = true)
    var recipeId: Long
) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
}
