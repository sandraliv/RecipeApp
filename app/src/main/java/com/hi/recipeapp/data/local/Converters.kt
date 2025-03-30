package com.hi.recipeapp.data.local

import androidx.room.TypeConverter

class Converters {

    @TypeConverter
    fun fromStringToList(value: String): List<String> {
        return if (value.isBlank()) emptyList()
        else value.split(",")
    }

    @TypeConverter
    fun fromListToString(list: List<String>): String {
        return list.joinToString(",")
    }
}
