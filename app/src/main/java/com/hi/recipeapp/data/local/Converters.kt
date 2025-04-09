package com.hi.recipeapp.data.local

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hi.recipeapp.classes.RecipeTag

class Converters {

    private val gson = Gson()

    // Map<String, String>
    @TypeConverter
    fun fromMap(map: Map<String, String>?): String {
        return gson.toJson(map)
    }

    @TypeConverter
    fun fromRecipeTagList(tags: List<RecipeTag>?): String {
        return Gson().toJson(tags)
    }

    @TypeConverter
    fun toRecipeTagList(json: String): List<RecipeTag> {
        val type = object : TypeToken<List<RecipeTag>>() {}.type
        return Gson().fromJson(json, type)
    }

    @TypeConverter
    fun toMap(value: String): Map<String, String> {
        val type = object : TypeToken<Map<String, String>>() {}.type
        return gson.fromJson(value, type)
    }

    // List<String>
    @TypeConverter
    fun fromList(list: List<String>?): String {
        return gson.toJson(list)
    }

    @TypeConverter
    fun toList(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, listType)
    }

    // Set<RecipeTag>
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
}
