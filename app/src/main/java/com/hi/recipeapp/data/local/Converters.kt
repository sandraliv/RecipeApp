package com.hi.recipeapp.data.local

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hi.recipeapp.classes.Category
import com.hi.recipeapp.classes.RecipeTag
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter


class Converters {

    private val gson = Gson()

    @TypeConverter
    fun fromMapToString(map: Map<String, String>): String {
        return gson.toJson(map)
    }

    @TypeConverter
    fun fromStringToMap(value: String): Map<String, String> {
        val type = object : TypeToken<Map<String, String>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun fromStringToList(value: String): List<String> {
        return if (value.isBlank()) emptyList()
        else value.split(",")
    }

    @TypeConverter
    fun fromListToString(list: List<String>): String {
        return list.joinToString(",")
    }

    @TypeConverter
    fun fromRecipeTagSet(tags: Set<RecipeTag>?): String? {
        return gson.toJson(tags)
    }

    @TypeConverter
    fun toRecipeTagSet(data: String?): Set<RecipeTag> {
        if (data == null) return emptySet()
        val type = object : TypeToken<Set<RecipeTag>>() {}.type
        return gson.fromJson(data, type)
    }

    // Convert LocalDate to String (for database storage)
    @TypeConverter
    fun fromLocalDateToString(date: LocalDate?): String? {
        return date?.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    }

    // Convert String (from database) to LocalDate
    @TypeConverter
    fun fromStringToLocalDate(dateString: String?): LocalDate? {
        return dateString?.let {
            LocalDate.parse(it, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        }
    }

}
