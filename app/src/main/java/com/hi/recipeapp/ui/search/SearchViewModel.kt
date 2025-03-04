package com.hi.recipeapp.ui.search

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hi.recipeapp.classes.RecipeCard
import com.hi.recipeapp.classes.RecipeTag
import com.hi.recipeapp.services.RecipeService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val recipeService: RecipeService
) : ViewModel() {

    private val _searchResults = MutableLiveData<List<RecipeCard>?>()
    val searchResults: LiveData<List<RecipeCard>?> get() = _searchResults

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading



    fun searchByQuery(query: String, tags: Set<String>?) {
        _errorMessage.value = null
        _isLoading.value = true
        Log.d("SEARCH_QUERY", "Query: $query, Tags: $tags")

        recipeService.searchRecipes(query, tags) { recipes, error ->
            _isLoading.value = false
            if (recipes != null && recipes.isNotEmpty()) {
                _searchResults.value = recipes
            } else {
                _searchResults.value = null
                _errorMessage.value = error ?: "No results found"
            }
        }
    }

}



