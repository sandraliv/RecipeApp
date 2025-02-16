package com.hi.recipeapp.ui.networking

import com.hi.recipeapp.classes.RecipeCard
import com.hi.recipeapp.classes.UserDTO
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

// Define API calls inside NetworkService (No ApiService.kt needed)
interface NetworkService {
    @GET("recipes")
    fun getRecipesByQuery(@Query("query") query: String): Call<List<RecipeCard>>

    @GET("recipes")
    fun getAllRecipes(): Call<List<RecipeCard>>

    @GET("users/1")
    fun getRoot(): Call<UserDTO>
}

// Provide a single instance of NetworkService
object NetworkClient {
    val service: NetworkService by lazy {
        ApiClient.instance.create(NetworkService::class.java)
    }
}
