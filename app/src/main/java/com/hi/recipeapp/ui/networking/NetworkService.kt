package com.hi.recipeapp.ui.networking

import com.hi.recipeapp.classes.FullRecipe
import com.hi.recipeapp.classes.LoginRequest
import com.hi.recipeapp.classes.RecipeCard
import com.hi.recipeapp.classes.UserCreateDTO
import com.hi.recipeapp.classes.UserDTO
import com.hi.recipeapp.classes.UserFullRecipe
import com.hi.recipeapp.classes.UserRecipeCard
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface NetworkService {

    @GET("recipes")
    fun getRecipesByQueryAndTags(
        @Query("query") query: String?,
        @Query("tags") tags: Set<String>?
    ): Call<List<RecipeCard>>

    @GET("recipes/all")
    fun getAllRecipes(): Call<List<RecipeCard>>

    @GET("recipes/{id}")
    fun getRecipeById(@Path("id") id: Int): Call<FullRecipe>

    @GET("users/{id}/getUserFav")
    suspend fun getUserFavorites(@Path("id") userId: Int): Response<List<RecipeCard>>

    @GET("user-recipes/{userId}/getUserRecipes")
    suspend fun getUserRecipes(
        @Path("userId") userId: Int,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): Response<List<UserRecipeCard>>

    @POST("recipes/{id}/addAsFav")
    suspend fun addRecipeToFavorites(
        @Path("id") recipeId: Int,
        @Query("userId") userId: Int
    ): Response<String>

    @DELETE("recipes/removeFavorite/{recipeId}")
    suspend fun removeRecipeFromFavorites(
        @Path("recipeId") recipeId: Int,
        @Query("userId") userId: Int
    ): Response<String>  // Assuming the response is a simple String message

    @GET("user-recipes/{id}")
    suspend fun getUserRecipeById(
        @Path("id") recipeId: Int,
        @Query("userId") userId: Int
    ): Response<UserFullRecipe>

    @GET("users/1")
    fun getRoot(): Call<UserDTO>

    @POST("users/Login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<UserDTO>

    @POST("users/Register")
    fun signup(@Body signupRequest: UserCreateDTO): Call<String> // ✅ Call<String> í stað Call<UserDTO>

}

