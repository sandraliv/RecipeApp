package com.hi.recipeapp.services

import android.util.Log
import com.hi.recipeapp.classes.FullRecipe
import com.hi.recipeapp.classes.RecipeCard
import com.hi.recipeapp.ui.networking.NetworkService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

class RecipeService @Inject constructor(
    private val networkService: NetworkService
) {

    fun searchRecipes(query: String, callback: (List<RecipeCard>?, String?) -> Unit) {
        networkService.getRecipesByQuery(query).enqueue(object : Callback<List<RecipeCard>> {
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
        networkService.getAllRecipes().enqueue(object : Callback<List<RecipeCard>> {
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

    fun fetchRecipeById(id: Int, callback: (FullRecipe?, String?) -> Unit) {
        networkService.getRecipeById(id).enqueue(object : Callback<FullRecipe> {
            override fun onResponse(
                call: Call<FullRecipe>,
                response: Response<FullRecipe>
            ) {
                if (response.isSuccessful) {
                    Log.d("RecipeService", "Recipe fetch successful, ID: $id") // Add logging here
                    callback(response.body(), null)

                } else {
                    Log.d("RecipeService", "Error: ${response.code()}") // Add logging for error codes
                    callback(null, "Error: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<FullRecipe>, t: Throwable) {
                Log.d("RecipeService", "Network failure: ${t.localizedMessage}") // Log failure
                callback(null, "Network error: ${t.localizedMessage}")
            }
        })
    }





}
