package com.hi.recipeapp.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hi.recipeapp.classes.RecipeCard
import com.hi.recipeapp.services.RecipeService
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val recipeService: RecipeService
) : ViewModel() {

    // LiveData to store the search results (nullable)
    private val _searchResults = MutableLiveData<List<RecipeCard>?>()
    val searchResults: LiveData<List<RecipeCard>?> get() = _searchResults

    // LiveData to store error messages (nullable)
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    // LiveData to track loading state
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    // Function to trigger the search and update the LiveData
    fun searchByQuery(query: String) {
        _errorMessage.value = null // Clear any previous error messages
        _isLoading.value = true // Set loading state to true

        recipeService.searchRecipes(query) { recipes, error ->
            _isLoading.value = false // Set loading state to false

            if (recipes != null && recipes.isNotEmpty()) {
                _searchResults.value = recipes // Update search results in LiveData
            } else {
                _searchResults.value = null // No recipes found, set the result to null
                _errorMessage.value = error ?: "No results found" // Set error message if no recipes
            }
        }
    }
}
