package com.hi.recipeapp.classes

data class RecipeCard(
    val imageUrl: String,
    val title: String,
    val description: String,
    val averageRating: Double,
    val ratingCount: Int
)