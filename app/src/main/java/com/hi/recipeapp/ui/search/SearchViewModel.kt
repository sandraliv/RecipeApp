package com.hi.recipeapp.ui.search

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hi.recipeapp.classes.RecipeCard
import com.hi.recipeapp.classes.RecipeTag
import com.hi.recipeapp.classes.SessionManager
import com.hi.recipeapp.classes.SortType
import com.hi.recipeapp.services.RecipeService
import com.hi.recipeapp.services.UserService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val recipeService: RecipeService,
    private val sessionManager: SessionManager,
    private val userService: UserService
) : ViewModel() {

    private val _searchResults = MutableLiveData<List<RecipeCard>?>()
    val searchResults: LiveData<List<RecipeCard>?> get() = _searchResults

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _favoriteResult = MutableLiveData<Result<String>>()
    val favoriteResult: LiveData<Result<String>> get() = _favoriteResult

    private val _favoriteActionMessage = MutableLiveData<String?>()
    val favoriteActionMessage: LiveData<String?> get() = _favoriteActionMessage

    // Add this to handle load more behavior
    private val _noMoreRecipes = MutableLiveData<Boolean>(false)
    val noMoreRecipes: LiveData<Boolean> = _noMoreRecipes

    // Track current page number and page size
    private var pageNumber = 0
    private val pageSize = 10
    private var sortType = SortType.RATING

    // Function to search recipes by query and tags
    fun searchByQuery(query: String, tags: Set<String>?) {
        _errorMessage.value = null
        _isLoading.value = true
        pageNumber = 0  // Reset to first page
        Log.d("SEARCH_QUERY", "Query: $query, Tags: $tags")

        recipeService.searchRecipes(query, tags, pageNumber, pageSize, sortType.name.lowercase()) { recipes, error ->
            _isLoading.value = false
            if (recipes != null && recipes.isNotEmpty()) {
                // Step 1: Get the user's favorite recipes
                val userId = sessionManager.getUserId()
                if (userId != -1) {
                    viewModelScope.launch {
                        val userFavorites = userService.getUserFavorites(userId).getOrNull() ?: emptyList()

                        // Step 2: Mark favorited recipes
                        recipes.forEach { recipe ->
                            recipe.isFavoritedByUser = userFavorites.any { it.id == recipe.id }
                        }

                        // Step 3: Update the LiveData with the updated recipes
                        _searchResults.value = recipes
                    }
                } else {
                    // If no user is logged in, set all favorites to false
                    recipes.forEach { recipe ->
                        recipe.isFavoritedByUser = false
                    }
                    _searchResults.value = recipes
                }
            } else {
                _searchResults.value = null
                _errorMessage.value = error ?: "No results found"
            }
        }
    }

    // Function to load more recipes (for pagination)
    fun loadMoreRecipes(query: String, tags: Set<String>?) {
        if (_isLoading.value == true) return  // Prevent multiple simultaneous loads

        _isLoading.value = true
        pageNumber++ // Increment the page number to load the next page

        recipeService.searchRecipes(query, tags, pageNumber, pageSize, sortType.name.lowercase()) { newRecipes, error ->
            _isLoading.value = false
            if (newRecipes != null && newRecipes.isNotEmpty()) {
                val currentResults = _searchResults.value?.toMutableList() ?: mutableListOf()
                currentResults.addAll(newRecipes)

                // Step 1: Get the user's favorite recipes
                val userId = sessionManager.getUserId()
                if (userId != -1) {
                    viewModelScope.launch {
                        val userFavorites = userService.getUserFavorites(userId).getOrNull() ?: emptyList()

                        // Step 2: Mark favorited recipes
                        newRecipes.forEach { recipe ->
                            recipe.isFavoritedByUser = userFavorites.any { it.id == recipe.id }
                        }

                        // Step 3: Update the LiveData with the updated recipes
                        _searchResults.value = currentResults
                    }
                } else {
                    // If no user is logged in, set all favorites to false
                    newRecipes.forEach { recipe ->
                        recipe.isFavoritedByUser = false
                    }
                    _searchResults.value = currentResults
                }
            } else {
                _noMoreRecipes.value = true  // No more recipes to load
            }
        }
    }


    // Function to update the favorite status for a recipe
    fun updateFavoriteStatus(recipe: RecipeCard, isFavorited: Boolean) {
        viewModelScope.launch {
            val userId = sessionManager.getUserId()
            if (userId != -1) {
                try {
                    // Save the favorited status to the session or backend
                    sessionManager.setFavoritedStatus(userId, recipe.id, isFavorited)

                    // Attempt to update backend: add or remove from favorites
                    if (isFavorited) {
                        recipeService.addRecipeToFavorites(recipe.id) // API call to add to favorites
                        _favoriteActionMessage.value = "Recipe added to favorites"
                    } else {
                        recipeService.removeRecipeFromFavorites(recipe.id) // API call to remove from favorites
                        _favoriteActionMessage.value = "Recipe removed from favorites"
                    }

                    // Update the local list with the new favorited status
                    _searchResults.value = _searchResults.value?.map {
                        if (it.id == recipe.id) it.copy(isFavoritedByUser = isFavorited) else it
                    }
                } catch (e: Exception) {
                    _favoriteActionMessage.value = "Failed to update favorite status"
                }
            }
        }
    }
    // Function to update the sort type and refetch recipes
    fun updateSortType(newSortType: SortType) {
        if (newSortType != sortType) {
            sortType = newSortType
            pageNumber = 0  // Reset to first page when sort type changes
            searchByQuery(query = "", tags = null)  // Refetch the recipes with new sort
        }
    }
}



