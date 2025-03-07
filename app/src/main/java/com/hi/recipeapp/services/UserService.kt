package com.hi.recipeapp.services

import android.util.Log
import com.hi.recipeapp.classes.FavoriteRecipesDTO
import com.hi.recipeapp.classes.LoginRequest
import com.hi.recipeapp.classes.RecipeCard
import com.hi.recipeapp.classes.SessionManager
import com.hi.recipeapp.classes.UserCreateDTO
import com.hi.recipeapp.classes.UserDTO
import com.hi.recipeapp.classes.UserRecipeCard
import com.hi.recipeapp.ui.networking.NetworkService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

class UserService @Inject constructor(
    private val networkService: NetworkService,
    private val sessionManager: SessionManager
) {

    fun login(username: String, password: String, callback: (UserDTO?, String?) -> Unit) {
        val loginRequest = LoginRequest(username, password)

        networkService.login(loginRequest).enqueue(object : Callback<UserDTO> {
            override fun onResponse(call: Call<UserDTO>, response: Response<UserDTO>) {
                if (response.isSuccessful) {
                    response.body()?.let { user ->
                        callback(user, null) // Login tókst
                    } ?: callback(null, "Invalid response from server")
                } else {
                    callback(null, "Invalid credentials") // Villuskilaboð
                }
            }

            override fun onFailure(call: Call<UserDTO>, t: Throwable) {
                callback(null, "Network error: ${t.localizedMessage}")
            }
        })
    }

    fun signup(
        role: String,
        name: String,
        email: String,
        password: String,
        username: String,
        callback: (UserDTO?, String?) -> Unit
    ) {
        val request = UserCreateDTO(role, name, email, password, username)

        networkService.signup(request).enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                when (response.code()) {
                    201 -> {
                        val user = UserDTO(role, name, email, password, username, 0, null)
                        callback(user, null)  // Successfully registered
                    }

                    409 -> {
                        callback(null, "Signup failed: User already registered with this email.")
                    }

                    else -> {
                        callback(null, "Signup failed: ${response.message()}")
                    }
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                callback(null, "Network error: ${t.localizedMessage}")
            }
        })
    }


    suspend fun getUserFavorites(): Result<List<RecipeCard>> {
        return try {
            val userId = sessionManager.getUserId()
            if (userId == null || userId == -1) {
                return Result.failure(Exception("User is not logged in"))
            }
            val response = networkService.getUserFavorites(userId)

            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Failed to fetch favorite recipes"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun getUserRecipes(page: Int = 0, size: Int = 10): Result<List<UserRecipeCard>> {
        return try {
            // Get the userId from the session manager
            val userId = sessionManager.getUserId()

            // Check if the userId is valid (not null and not -1)
            if (userId == null || userId == -1) {
                return Result.failure(Exception("User is not logged in"))
            }

            // Make the network call to get the user recipes
            val response = networkService.getUserRecipes(userId, page, size)

            if (response.isSuccessful) {
                // If the response is successful, return the result wrapped in a success
                Result.success(response.body() ?: emptyList())
            } else {
                // If the response is not successful, return failure with an error message
                Result.failure(Exception("Failed to fetch user recipes"))
            }
        } catch (e: Exception) {
            // If there is an error during the network call, catch it and return failure
            Result.failure(e)
        }
    }


}






