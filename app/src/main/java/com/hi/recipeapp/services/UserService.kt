package com.hi.recipeapp.services

import android.util.Log
import com.hi.recipeapp.classes.CalendarEntry
import com.hi.recipeapp.classes.LoginRequest
import com.hi.recipeapp.classes.RecipeCard
import com.hi.recipeapp.classes.SessionManager
import com.hi.recipeapp.classes.User
import com.hi.recipeapp.classes.UserCreateDTO
import com.hi.recipeapp.classes.UserDTO
import com.hi.recipeapp.classes.UserFullRecipe
import com.hi.recipeapp.classes.UserRecipeCard
import com.hi.recipeapp.data.local.Recipe
import com.hi.recipeapp.ui.networking.NetworkService
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

class UserService @Inject constructor(
    private val networkService: NetworkService,
    private val sessionManager: SessionManager
) {

    suspend fun login(username: String, password: String): UserDTO? {
        val loginRequest = LoginRequest(username, password)

        return try {
            val response = networkService.login(loginRequest)
            if (response.isSuccessful) {
                response.body()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
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

    suspend fun changePassword(
        userId: Int,
        currentPassword: String,
        newPassword: String,
        confirmNewPassword: String
    ): Result<String> {
        val updates = mapOf(
            "currentPassword" to currentPassword,
            "newPassword" to newPassword,
            "confirmNewPassword" to confirmNewPassword
        )
        return try {
            Log.d("HELLO", "I AM CALLING THE NET WORK SERVICE")
            val response = networkService.patchUpdateUserPassword(userId, updates)
            if (response.isSuccessful) {
                Result.success(response.body() ?: /* fallback if null */ "")
            } else {
                Result.failure(Exception("Failed to update password"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }

    }

    suspend fun uploadProfilePic(
        userId: Int,
        photoPart: MultipartBody.Part
    ): Response<Map<String, String>> {
        return networkService.postProfilePic(userId, photoPart)
    }

    suspend fun getUserFavorites(userId: Int):
            Result<List<RecipeCard>> {
        return try {
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

    suspend fun deleteUser(userId: Int): Result<Unit> {
        return try {
            val response = networkService.deleteUser(userId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Delete failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    suspend fun getAllUsers(): Result<List<User>> {
        return try {
            val response = networkService.getAllUsers()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Failed to fetch users: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }



    suspend fun getUserRecipes(
        page: Int = 0, size: Int = 10): Result<List<UserRecipeCard>> {
        return try {
            val userId = sessionManager.getUserId()
            if (userId == -1) {
                return Result.failure(Exception("User is not logged in"))
            }
            val response = networkService.getUserRecipes(userId, page, size)
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Failed to fetch user recipes"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchUserRecipeById(id: Int): UserFullRecipe? {
        return try {
            val userId = sessionManager.getUserId()
            val response = networkService.getUserRecipeById(id, userId)
            if (response.isSuccessful) {
                response.body()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    // New method to fetch user saved calendar recipes
    suspend fun getUserSavedToCalendarRecipes(userId: Int): Result<List<CalendarEntry>> {
        return try {
            // Make the network call to fetch saved calendar recipes
            val response = networkService.getUserSavedToCalendarRecipes(userId)

            if (response.isSuccessful) {
                // If the request was successful, return the body (which should be a list of Calendar items)
                Result.success(response.body() ?: emptyList())
            } else {
                // Handle the case where the response is not successful
                Result.failure(Exception("Failed to fetch saved calendar recipes"))
            }
        } catch (e: Exception) {
            // In case of any network errors or exceptions, return a failure result
            Result.failure(e)
        }
    }
    // Function to fetch user profile by ID
    suspend fun getUserProfileById(userId: Int): Result<User> {
        return try {
            val response = networkService.getUserProfileById(userId)
            if (response.isSuccessful) {
                Result.success(response.body() ?: throw Exception("User not found"))
            } else {
                Result.failure(Exception("Failed to fetch user profile"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}






