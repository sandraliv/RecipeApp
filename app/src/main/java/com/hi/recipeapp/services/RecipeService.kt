package com.hi.recipeapp.services

import android.util.Log
import com.hi.recipeapp.classes.FullRecipe
import com.hi.recipeapp.classes.RecipeCard
import com.hi.recipeapp.classes.RecipeTag
import com.hi.recipeapp.classes.UserFullRecipe
import com.hi.recipeapp.ui.networking.NetworkService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

class RecipeService @Inject constructor(
    private val networkService: NetworkService
) {

    fun searchRecipes(
        query: String,
        tags: Set<String>?,
        callback: (List<RecipeCard>?, String?) -> Unit
    ) {
        // Log the query and tags
        Log.d("RECIPE_SERVICE", "Query: $query, Tags: $tags")

        // Now pass the tags as Set<String> to the network service
        networkService.getRecipesByQueryAndTags(
            query.takeIf { it.isNotEmpty() },  // Only pass query if it's not empty
            tags.takeIf { it.isNullOrEmpty().not() } // Only pass tags if they're not null or empty
        ).enqueue(object : Callback<List<RecipeCard>> {
            override fun onResponse(
                call: Call<List<RecipeCard>>,
                response: Response<List<RecipeCard>>
            ) {
                if (response.isSuccessful) {
                    val recipes = response.body()
                    Log.d("NETWORK_RESPONSE", "Received recipes: $recipes")
                    callback(recipes, null)
                } else {
                    Log.e("NETWORK_ERROR", "Error response: ${response.code()}")
                    callback(null, "Error: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<RecipeCard>>, t: Throwable) {
                callback(null, "Network error: ${t.localizedMessage}") // Handle network failure
            }
        })
    }


    // Fetch all recipes (for initial display or fallback)
    fun fetchRecipes(callback: (List<RecipeCard>?, String?) -> Unit) {
        networkService.getAllRecipes().enqueue(object : Callback<List<RecipeCard>> {
            override fun onResponse(
                call: Call<List<RecipeCard>>,
                response: Response<List<RecipeCard>>
            ) {
                val recipes = response.body()
                if (response.isSuccessful && !recipes.isNullOrEmpty()) {
                    callback(recipes, null)
                } else {
                    callback(null, "No recipes found.")
                }
            }

            override fun onFailure(call: Call<List<RecipeCard>>, t: Throwable) {
                callback(null, "Network error: ${t.localizedMessage}")
            }
        })
    }

    // Fetch recipe by ID
    fun fetchRecipeById(id: Int, callback: (FullRecipe?, String?) -> Unit) {
        networkService.getRecipeById(id).enqueue(object : Callback<FullRecipe> {
            override fun onResponse(call: Call<FullRecipe>, response: Response<FullRecipe>) {
                if (response.isSuccessful) {
                    callback(response.body(), null)
                } else {
                    callback(null, "Error: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<FullRecipe>, t: Throwable) {
                callback(null, "Network error: ${t.localizedMessage}")
            }
        })
    }

    suspend fun uploadUserRecipe(userId: Int, recipe: UserFullRecipe): Boolean {
        return try {
            val response = networkService.uploadRecipe(userId, recipe)
            if (response.isSuccessful) {
                Log.d("RecipeService", "Recipe uploaded successfully: ${response.body()}")
                true
            } else {
                Log.e("RecipeService", "Failed to upload recipe: ${response.code()}")
                false
            }
        } catch (e: Exception) {
            Log.e("RecipeService", "Exception: ${e.localizedMessage}")
            false
        }
    }





}









