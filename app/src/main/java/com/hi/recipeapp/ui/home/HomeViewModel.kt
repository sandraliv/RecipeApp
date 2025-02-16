package com.hi.recipeapp.ui.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hi.recipeapp.classes.RecipeCard
import com.hi.recipeapp.services.RecipeService

class HomeViewModel : ViewModel() {
    private lateinit var recipeService:RecipeService
    recipeService

    // LiveData to hold the list of recipes
    private val _recipes = MutableLiveData<List<RecipeCard>>()
    val recipes: LiveData<List<RecipeCard>> get() = _recipes

    // LiveData for error messages
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    // LiveData to track loading state
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    // Fetch recipes from the API using the RecipeService
    fun fetchRecipes() {
        // Show the loading state (optional, if you have a progress bar)
        _errorMessage.value = null // Clear previous error messages

        recipeService.fetchRecipes { recipes, error ->
            if (recipes != null) {
                // Successful fetch: update LiveData
                _recipes.value = recipes
            } else {
                // Error occurred: update error message
                _errorMessage.value = error ?: "Unknown error occurred"
            }
        }
    }
}
