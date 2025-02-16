package com.hi.recipeapp.classes

import com.google.gson.annotations.SerializedName

data class RecipeCard(
    @SerializedName("image_url") val imageUrl: String, 
    val title: String,
    val description: String,
    val averageRating: Double,
    val ratingCount: Int,
    val id:Int
)