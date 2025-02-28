package com.hi.recipeapp.ui.networking

import com.hi.recipeapp.classes.FullRecipe
import com.hi.recipeapp.classes.LoginRequest
import com.hi.recipeapp.classes.RecipeCard
import com.hi.recipeapp.classes.UserCreateDTO
import com.hi.recipeapp.classes.UserDTO
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

// Define API calls inside NetworkService (No ApiService.kt needed)
interface NetworkService {
    @GET("recipes")
    fun getRecipesByQuery(@Query("query") query: String): Call<List<RecipeCard>>

    @GET("recipes")
    fun getAllRecipes(): Call<List<RecipeCard>>

    @GET("recipes/{id}")
    fun getRecipeById(@Path("id") id: Int): Call<FullRecipe>

    @GET("users/1")
    fun getRoot(): Call<UserDTO>

    @POST("users/Login")
    fun login(@Body loginRequest: LoginRequest): Call<UserDTO>

    @POST("users/Register")
    fun signup(@Body signupRequest: UserCreateDTO): Call<String>
}

