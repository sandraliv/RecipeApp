package com.hi.recipeapp.ui.networking

import com.hi.recipeapp.classes.Category
import com.hi.recipeapp.classes.FavoriteRecipesDTO
import com.hi.recipeapp.classes.FullRecipe
import com.hi.recipeapp.classes.LoginRequest
import com.hi.recipeapp.classes.RecipeCard
import com.hi.recipeapp.classes.RecipeTag
import com.hi.recipeapp.classes.UserCreateDTO
import com.hi.recipeapp.classes.UserDTO
import com.hi.recipeapp.classes.UserFullRecipe
import com.hi.recipeapp.classes.UserRecipeCard
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

// Define API calls inside NetworkService (No ApiService.kt needed)
interface NetworkService {

    @GET("recipes")
    fun getRecipesByQueryAndTags(
        @Query("query") query: String?,
        @Query("tags") tags: Set<String>?
    ): Call<List<RecipeCard>>

    @GET("recipes")
    fun getAllRecipes(): Call<List<RecipeCard>>

    @GET("recipes/{id}")
    fun getRecipeById(@Path("id") id: Int): Call<FullRecipe>


    @GET("users/{id}/getUserFav")
    suspend fun getUserFavorites(@Path("id") userId: Int): Response<List<RecipeCard>>

    @POST("recipes/{id}/removeFromFav")
    suspend fun removeRecipeFromFavorites(
        @Path("id") recipeId: Int // No need for userId in headers
    ): Response<String>

    @POST("recipes/{id}/addAsFav")
    suspend fun addRecipeToFavorites(
        @Path("id") recipeId: Int // No need for userId in headers
    ): Response<String>

    @GET("user-recipes/{userId}/getUserRecipes")
    suspend fun getUserRecipes(
        @Path("userId") userId: Int,       // Corrected to match the Path variable
        @Query("page") page: Int = 0,      // Default page is 0
        @Query("size") size: Int = 10      // Default size is 10
    ): Response<List<UserRecipeCard>>

    @GET("user-recipes/{id}") // Get a specific user recipe by ID
    suspend fun getUserRecipeById(
        @Path("id") recipeId: Int, // Path parameter for recipe ID
        @Query("userId") userId: Int // Query parameter for user ID
    ): Response<UserFullRecipe>


    @GET("users/1")
    fun getRoot(): Call<UserDTO>

    @POST("users/Login")
    fun login(@Body loginRequest: LoginRequest): Call<UserDTO>

    @POST("users/Register")
    fun signup(@Body signupRequest: UserCreateDTO): Call<String> // ✅ Call<String> í stað Call<UserDTO>

}

