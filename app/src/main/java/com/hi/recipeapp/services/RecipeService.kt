package com.hi.recipeapp.services

import android.util.Log
import com.hi.recipeapp.classes.Category
import com.hi.recipeapp.classes.FullRecipe
import com.hi.recipeapp.classes.RecipeCard
import com.hi.recipeapp.classes.SessionManager
import com.hi.recipeapp.classes.UserFullRecipe
import com.hi.recipeapp.ui.networking.NetworkService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

class RecipeService @Inject constructor(
    private val networkService: NetworkService,
    private val sessionManager: SessionManager
) {

    fun searchRecipes(
        query: String,
        tags: Set<String>?,
        page: Int = 0,            // Default page number to 0
        size: Int = 20,           // Default size to 10
        sort: String = "RATING",  // Default sort to "RATING"
        callback: (List<RecipeCard>?, String?) -> Unit
    ) {
        Log.d("RECIPE_SERVICE", "Query: $query, Tags: $tags, Page: $page, Size: $size, Sort: $sort")

        // Prepare query parameters
        val queryParam = query.takeIf { it.isNotEmpty() }
        val tagsParam = tags.takeIf { it.isNullOrEmpty().not() }

        // Make the network call with pagination and sorting
        networkService.getRecipesByQueryAndTags(
            queryParam,
            tagsParam,
            page,
            size,
            sort
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


    fun fetchRecipes(
        sort: String = "rating",
        page: Int = 0,
        size: Int = 20,
        callback: (List<RecipeCard>?, String?) -> Unit
    ) {
        networkService.getAllRecipes(page = page, size = size, sort = sort)
            .enqueue(object : Callback<List<RecipeCard>> {
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
            val userId = sessionManager.getUserId()
            val response = networkService.removeRecipeFromFavorites(recipeId, userId)
            if (response.isSuccessful) {
                Result.success("Recipe removed from favorites")
            } else {
                Result.failure(Exception("Failed to remove from favorites"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addRecipeRating(recipeId: Int, score: Int): Result<String> {
        return try {
            // Check if the user is logged in
            val userId =
                sessionManager.getUserId()  // Assume this returns the userId or -1 if not logged in
            if (userId == -1) {
                return Result.failure(Exception("User is not logged in"))
            }

            // Make the network request to add the rating
            val response = networkService.addRatingToRecipe(recipeId, userId, score)

            // Check if the response is successful
            if (response.isSuccessful) {
                // Rating added successfully
                Result.success("Rating added successfully")
            } else {
                // Handle failure, maybe the response contains an error message
                Result.failure(Exception("Failed to add rating"))
            }
        } catch (e: Exception) {
            // Catch any exception that may occur and return a failure result
            Result.failure(e)
        }
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

    // Fetch recipes by category with callback
    fun getRecipesByCategory(
        category: Category,
        sort: String = "rating", // Add sort parameter with a default value
        callback: (List<RecipeCard>?, String?) -> Unit
    ) {
        Log.d("RECIPE_SERVICE", "Category: $category, Sort: $sort")

        // Convert the enum category to a string
        val categoryName = category.name

        // Make the network call to get recipes by category
        networkService.getRecipesByCategory(
            categories = setOf(categoryName),
            sort = sort,
            page = 0,  // Default page (can be adjusted)
            size = 20  // Default size (can be adjusted)
        ).enqueue(object : Callback<List<RecipeCard>> {
            override fun onResponse(
                call: Call<List<RecipeCard>>,
                response: Response<List<RecipeCard>>
            ) {
                if (response.isSuccessful) {
                    val recipes = response.body()
                    Log.d("NETWORK_RESPONSE", "Received recipes: $recipes")
                    callback(recipes, null) // Return the recipes to the callback
                } else {
                    Log.e("NETWORK_ERROR", "Error response: ${response.code()}")
                    callback(null, "Error: ${response.code()}") // Return error message
                }
            }

            override fun onFailure(call: Call<List<RecipeCard>>, t: Throwable) {
                callback(null, "Network error: ${t.localizedMessage}") // Handle network failure
            }
        })
    }
}













