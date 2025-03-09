package com.hi.recipeapp.ui.fullrecipe

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hi.recipeapp.classes.FullRecipe
import com.hi.recipeapp.services.RecipeService
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FullRecipeViewModel @Inject constructor(
    private val recipeService: RecipeService
) : ViewModel() {

    private val _recipe = MutableLiveData<FullRecipe?>()
    val recipe: LiveData<FullRecipe?> = _recipe

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    fun fetchRecipeById(id: Int) {
        recipeService.fetchRecipeById(id) { result, error ->
            if (result != null) {
                Log.d("FullRecipeViewModel", "Fetched recipe: $result") // Add this log to verify fetched data
                _recipe.value = result
            } else {
                Log.d("FullRecipeViewModel", "Error fetching recipe: $error") // Log error message
                _errorMessage.value = error
            }
        }
    }
}
