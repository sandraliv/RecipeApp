package com.hi.recipeapp.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hi.recipeapp.services.RecipeService
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val recipeService: RecipeService
) : ViewModel (){

    //Holds search results
    private val _searchResults = MutableLiveData<String>()
    //Allows only reading of the search results to prevent accidental modifications from UI
    val searchResults: LiveData<String> get() = _searchResults

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    //A function to update _searchResults, which will notify observers(UI) of the change.
    //The underscore is used to indicate that is should only be modified within the ViewModel,
    fun updateSearchResults(results: String) {
        _searchResults.value = results  // Update the search results with the fetched data
    }
    fun searchByQuery(query: String) {
        _isLoading.value = true // ✅ Show loading state

        recipeService.searchRecipes(query) { recipes, error ->
            _isLoading.postValue(false) // ✅ Hide loading state

            if (!recipes.isNullOrEmpty()) {
                val firstRecipe = recipes.first()
                val resultText =
                    "Found Recipe: ${firstRecipe.title}\nRating: ${firstRecipe.averageRating}\nDescription: ${firstRecipe.description}"
                _searchResults.postValue(resultText) // ✅ Update LiveData
            } else {
                _searchResults.postValue(error ?: "No recipes found for '$query'") // ✅ Handle error
            }
        }
    }

}