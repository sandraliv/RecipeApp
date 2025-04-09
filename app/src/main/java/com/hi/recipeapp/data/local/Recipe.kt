package com.hi.recipeapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.hi.recipeapp.classes.Category

@Entity("user_recipes")
data class Recipe(
    @PrimaryKey val id: Int,
    val title: String,
    val description: String,
    val imageUrls: List<String>,
    val averageRating: Double,
    val ratingCount: Int,
    val tags: List<String>,
    var isFavoritedByUser: Boolean,
    val instructions: String,
    val ingredients: Map<String, String>
)