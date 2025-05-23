package com.hi.recipeapp.classes

data class UserDTO(
    val role: String,
    val name: String,
    val email: String,
    val password: String,
    val username: String,
    var id: Int,
    val profilePictureUrl: String?
)

data class FavoriteRecipesDTO(
    val favoriteRecipes: List<RecipeCard>
)

data class UserCreateDTO(
    val role: String,
    val name: String,
    val email: String,
    val password: String,
    val username: String
)

data class LoginRequest(
    val username: String,
    val password: String
)

data class User(
    val name: String,
    val username: String,
    val email: String,
    val role: String,
    val id: Int
)