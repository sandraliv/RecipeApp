package com.hi.recipeapp.ui.Networking

import com.hi.recipeapp.classes.APIObject
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {
    @GET("{id}")
    fun getPostById(@Path("id") postId: Int): Call<APIObject>
}