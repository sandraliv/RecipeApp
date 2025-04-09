package com.hi.recipeapp.classes

import com.google.gson.annotations.SerializedName

data class RecipeCard(
    val imageUrls: List<String>,
    val title: String,
    val description: String,
    val instructions: String,
    @SerializedName("ingredients") val ingredients: Map<String, String>,
    val averageRating: Double,
    val ratingCount: Int,
    val id:Int,
    val tags: List<String>,
    var isFavoritedByUser: Boolean

)

data class FullRecipe(
    val imageUrls: List<String>?,
    val title: String,
    val description: String,
    val averageRating: Double,
    val ratingCount: Int,
    val id:Int,
    val instructions: String,
    @SerializedName("ingredients") val ingredients: Map<String, String>,
    @SerializedName("tags") val tags: List<String>,
    @SerializedName("categories") val categories: Set<Category>,
    var isFavoritedByUser: Boolean
)

data class UserRecipeCard(
    val imageUrls: List<String>,
    val title: String,
    val description: String,
    val id:Int,
)

data class UserFullRecipe(
    val imageUrls: List<String>,
    val title: String,
    val description: String,
    val id: Int,
    val instructions: String,
    val ingredients: Map<String, String>
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

    // Method to get the display name from the serialized name
    fun getDisplayName(): String {
        return when (this) {
            VEGETARIAN -> "Vegetarian"
            VEGAN -> "Vegan"
            GLUTEN_FREE -> "Gluten Free"
            KETO -> "Keto"
            DAIRY_FREE -> "Dairy Free"
            LOW_CARB -> "Low Carb"
        }

    }
}



enum class Category{
    @SerializedName("BREAKFAST")
    BREAKFAST,
    @SerializedName("APPETIZER")
    APPETIZER,
    @SerializedName("MAIN_COURSE")
    MAIN_COURSE,
    @SerializedName("SNACK")
    SNACK,
    @SerializedName("DESSERT")
    DESSERT,
    @SerializedName("BAKING")
    BAKING;

    // Method to get the display name for each category
    fun getDisplayName(): String {
        return when (this) {
            BREAKFAST -> "Breakfast"
            APPETIZER -> "Appetizer"
            MAIN_COURSE -> "Main Course"
            SNACK -> "Snack"
            DESSERT -> "Dessert"
            BAKING -> "Baking"
        }
    }
}
enum class SortType{
    @SerializedName("RATING")
    RATING,
    @SerializedName("DATE")
    DATE;

    // Method to get the display name for each category
    fun getDisplayName(): String {
        return when (this) {
            RATING -> "Rating"
            DATE -> "Date"

        }
    }
}