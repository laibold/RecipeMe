package de.hs_rm.recipe_me.model.user

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class User(
    var name: String
) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0

    constructor() : this("")

}