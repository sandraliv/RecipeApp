package com.hi.recipeapp.ui.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hi.recipeapp.classes.RecipeCard
import com.hi.recipeapp.services.RecipeService
import com.hi.recipeapp.ui.networking.NetworkClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeViewModel : ViewModel() {
    // LiveData to hold the list of recipes
    private val _recipes = MutableLiveData<List<RecipeCard>?>()
    val recipes: MutableLiveData<List<RecipeCard>?> get() = _recipes

    // LiveData for error messages
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: MutableLiveData<String?> get() = _errorMessage

    // LiveData to track loading state
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    // Fetch recipes from the API using the RecipeService
    fun fetchRecipes() {
        NetworkClient.service.getAllRecipes().enqueue(object : Callback<List<RecipeCard>> {
            override fun onResponse(
                call: Call<List<RecipeCard>>,
                response: Response<List<RecipeCard>>
            ) {
                if (response.isSuccessful) {
                    val recipes = response.body() ?: emptyList()
                    recipes.forEach {
                        Log.d("HomeViewModel", "Recipe image URL: ${it.imageUrl}")
                    }
                    _recipes.value = recipes
                } else {
                    _errorMessage.value = "Error: ${response.code()}"
                }
            }

            override fun onFailure(call: Call<List<RecipeCard>>, t: Throwable) {
                // Handle failure
                _errorMessage.value = t.message
            }
        })
    }
}
