package com.hi.recipeapp.services

import android.util.Log
import com.hi.recipeapp.classes.LoginRequest
import com.hi.recipeapp.classes.UserCreateDTO
import com.hi.recipeapp.classes.UserDTO
import com.hi.recipeapp.ui.networking.NetworkService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

class UserService @Inject constructor(
    private val networkService: NetworkService
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

    fun signup( role: String, name: String, email: String, password: String, username: String,
        callback: (UserDTO?, String?) -> Unit
    ) {
        val request = UserCreateDTO(role, name, email, password, username)

        networkService.signup(request).enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful) {
                    val user = UserDTO("USER", name, email, password, username, 0, null)

                    callback(user, null)  // Skilum UserDTO
                } else {
                    callback(null, "Signup failed: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                callback(null, "Network error: ${t.localizedMessage}")
            }
        })
    }
}

