package com.hi.recipeapp.classes

import com.google.gson.annotations.SerializedName

data class RecipeCard(
    @SerializedName("image_url") val imageUrl: String,
    val title: String,
    val description: String,
    val averageRating: Double,
    val ratingCount: Int,
    val id:Int,
    val tags: List<String>,
    var isFavoritedByUser: Boolean = false

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
    @SerializedName("categories") val categories: Set<Category>, // Set of Category enums
    var isFavoritedByUser: Boolean = false
)

data class UserRecipeCard(
    @SerializedName("image_url") val imageUrl: String,
    val title: String,
    val description: String,
    val id:Int,
)

data class UserFullRecipe(
    @SerializedName("image_url") val imageUrl: String,
    val title: String,
    val description: String,
    val id: Int,
    val instructions: String,
    @SerializedName("ingredients") val ingredients: Map<String, String> // 🔥 Breytt í Map
)






enum class RecipeTag {
    @SerializedName("VEGETARIAN")
    VEGETARIAN,

    @SerializedName("VEGAN")
    VEGAN,

    @SerializedName("GLUTEN_FREE")
    GLUTEN_FREE,

    @SerializedName("KETO")
    KETO,

    @SerializedName("DAIRY_FREE")
    DAIRY_FREE,

    @SerializedName("LOW_CARB")
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