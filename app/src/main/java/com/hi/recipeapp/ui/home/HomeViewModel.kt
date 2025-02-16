package com.hi.recipeapp.ui.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hi.recipeapp.classes.RecipeCard
import com.hi.recipeapp.services.RecipeService

class HomeViewModel : ViewModel() {


    private val recipeService = RecipeService()  // Initialize the service

    // LiveData to hold the list of recipes
    private val _recipes = MutableLiveData<List<RecipeCard>>()
    val recipes: LiveData<List<RecipeCard>> get() = _recipes

    // LiveData for error messages
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    // Fetch recipes using RecipeService
    fun fetchRecipes() {
        recipeService.fetchRecipes { recipeList, error ->
            if (recipeList != null) {
                _recipes.value = recipeList  // Update LiveData with the fetched recipes
            } else {
                _errorMessage.value = error  // Update LiveData with the error message
            }
        }
    }



}
