package com.hi.recipeapp.ui.Networking


import com.hi.recipeapp.classes.RecipeCard
import com.hi.recipeapp.classes.UserDTO
import retrofit2.Call
import retrofit2.http.GET

import retrofit2.http.Query

interface ApiService {
    @GET("users/1")
    fun getRoot(): Call<UserDTO>

    @GET("recipes")
    fun getRecipesByQuery(@Query("query") query: String): Call<List<RecipeCard>>

    @GET("recipes")
    fun getAllRecipes(): Call<List<RecipeCard>>


}