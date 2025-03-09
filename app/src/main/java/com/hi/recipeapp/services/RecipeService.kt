package com.hi.recipeapp.services

import android.util.Log
import com.hi.recipeapp.classes.FullRecipe
import com.hi.recipeapp.classes.RecipeCard
import com.hi.recipeapp.classes.RecipeTag
import com.hi.recipeapp.classes.SessionManager
import com.hi.recipeapp.ui.networking.NetworkService
import retrofit2.Call
import retrofit2.Callback
import okhttp3.Request
import okhttp3.ResponseBody
import retrofit2.Response
import javax.inject.Inject

class RecipeService @Inject constructor(
    private val networkService: NetworkService,
    private val sessionManager: SessionManager
) {

    fun searchRecipes(query: String, tags: Set<String>?, callback: (List<RecipeCard>?, String?) -> Unit) {
        // Log the query and tags
        Log.d("RECIPE_SERVICE", "Query: $query, Tags: $tags")

        // Now pass the tags as Set<String> to the network service
        networkService.getRecipesByQueryAndTags(
            query.takeIf { it.isNotEmpty() },  // Only pass query if it's not empty
            tags.takeIf { it.isNullOrEmpty().not() } // Only pass tags if they're not null or empty
        ).enqueue(object : Callback<List<RecipeCard>> {
            override fun onResponse(call: Call<List<RecipeCard>>, response: Response<List<RecipeCard>>) {
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


    suspend fun addRecipeToFavorites(recipeId: Int): Result<String> {
        return try {
            val userId = sessionManager.getUserId()
            if (userId == -1) {
                return Result.failure(Exception("User is not logged in"))
            }
            val response = networkService.addRecipeToFavorites(recipeId, userId)
            if (response.isSuccessful) {
                Result.success("Recipe added to favorites")
            } else {
                Result.failure(Exception("Failed to add recipe to favorites"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    suspend fun removeRecipeFromFavorites(recipeId: Int): Result<String> {
        return try {
            val userId = sessionManager.getUserId()  // Get userId from session or wherever appropriate

            // Call the network service to remove the recipe from favorites
            val response = networkService.removeRecipeFromFavorites(recipeId, userId)

            if (response.isSuccessful) {
                // If the response is successful, return a success result with a message
                Result.success("Recipe removed from favorites")
            } else {
                // If the response failed, return a failure result with an error message
                Result.failure(Exception("Failed to remove from favorites"))
            }
        } catch (e: Exception) {
            // If an exception occurs (e.g., network failure), return a failure result with the exception
            Result.failure(e)
        }
    }




}









