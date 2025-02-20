package com.hi.recipeapp.services

import com.hi.recipeapp.classes.LoginRequest
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
                        callback(user, null) // ✅ Login successful
                    } ?: callback(null, "Invalid response from server") // ✅ Handle empty response
                } else {
                    callback(null, "Invalid credentials") // ✅ Handle login failure
                }
            }

            override fun onFailure(call: Call<UserDTO>, t: Throwable) {
                callback(null, "Network error: ${t.localizedMessage}") // ✅ Handle network issues
            }
        })
    }
}