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

data class FullRecipe(
    @SerializedName("image_url") val imageUrl: String,
    val title: String,
    val description: String,
    val averageRating: Double,
    val ratingCount: Int,
    val id:Int,
    val instructions: String,
    @SerializedName("ingredients") val ingredients: Map<String, String>, // Map for ingredient_name and ingredient_quantity
    @SerializedName("tags") val tags: Set<RecipeTag>,
    @SerializedName("categories") val categories: Set<Category> // Set of Category enums



)
enum class RecipeTag {
    VEGETARIAN,
    VEGAN,
    GLUTEN_FREE,
    KETO,
    DAIRY_FREE,
    LOW_CARB;
}


enum class Category{
    BREAKFAST,
    APPETIZER,
    MAIN_COURSE,
    SNACK,
    DESSERT,
    BAKING;
}