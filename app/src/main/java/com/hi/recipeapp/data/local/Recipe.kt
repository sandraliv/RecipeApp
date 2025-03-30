package com.hi.recipeapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("user_recipes")
data class Recipe(
    @PrimaryKey val id: Int,
    val title: String,
    val description: String,
    val imageUrl: String,
    val averageRating: Double,
    val ratingCount: Int,
    val tags: List<String>,
    var isFavoritedByUser: Boolean
)