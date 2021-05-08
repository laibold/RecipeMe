package de.hs_rm.recipe_me.persistence

import androidx.room.TypeConverter
import java.time.LocalDate

/**
 * Converters for [AppDatabase]
 */
class Converters {

    /**
     * [LocalDate] -> [String]
     */
    @TypeConverter
    fun localDateToString(date: LocalDate): String {
        return date.toString()
    }

    /**
     * [String] -> [LocalDate]
     */
    @TypeConverter
    fun stringToDate(dateStr: String): LocalDate {
        return LocalDate.parse(dateStr)
    }

}
