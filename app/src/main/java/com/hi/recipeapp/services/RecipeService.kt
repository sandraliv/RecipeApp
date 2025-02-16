package com.hi.recipeapp.services

import com.hi.recipeapp.classes.RecipeCard
import com.hi.recipeapp.ui.networking.NetworkClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RecipeService {

    fun searchRecipes(query: String, callback: (List<RecipeCard>?, String?) -> Unit) {
        NetworkClient.service.getRecipesByQuery(query).enqueue(object : Callback<List<RecipeCard>> {
            override fun onResponse(call: Call<List<RecipeCard>>, response: Response<List<RecipeCard>>) {
                response.body()?.takeIf { it.isNotEmpty() }?.let { recipes ->
                    callback(recipes, null) // ✅ Return successful results
                } ?: callback(null, "No recipes found for '$query'") // ✅ Handle empty list
            }

            override fun onFailure(call: Call<List<RecipeCard>>, t: Throwable) {
                callback(null, "Network error: ${t.localizedMessage}") // ✅ Handle network failure
            }
        })
    }

    fun fetchRecipes(callback: (List<RecipeCard>?, String?) -> Unit) {
        NetworkClient.service.getAllRecipes().enqueue(object : Callback<List<RecipeCard>> {
            override fun onResponse(
                call: Call<List<RecipeCard>>,
                response: Response<List<RecipeCard>>
            ) {
                if (response.isSuccessful) {
                    callback(response.body(), null)
                } else {
                    callback(null, "Error: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<RecipeCard>>, t: Throwable) {
                callback(null, "Network error: ${t.localizedMessage}")
            }
        })
    }






}
