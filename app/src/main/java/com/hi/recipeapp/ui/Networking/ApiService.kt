package com.hi.recipeapp.ui.Networking

import com.hi.recipeapp.classes.UserDTO
import retrofit2.Call
import retrofit2.http.GET

interface ApiService {
    @GET("users/1")
    fun getRoot(): Call<UserDTO>
}
