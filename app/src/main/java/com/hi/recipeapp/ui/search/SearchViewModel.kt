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

    private val _isLoadingMore = MutableLiveData<Boolean>(false)
    val isLoadingMore: LiveData<Boolean> get() = _isLoadingMore

    private val _favoriteActionMessage = MutableLiveData<String?>()
    val favoriteActionMessage: LiveData<String?> get() = _favoriteActionMessage

    private val _noMoreRecipes = MutableLiveData<Boolean>(false)
    val noMoreRecipes: LiveData<Boolean> = _noMoreRecipes

    private var pageNumber = 0
    private val pageSize = 10
    private var sortType = SortType.RATING
    private var currentQuery: String = ""
    private var currentTags: Set<String>? = null

    init {
        searchByQuery(query = "", tags = null, sort = SortType.RATING)
    }


    fun searchByQuery(query: String, tags: Set<String>?, sort: SortType) {
        _errorMessage.value = null
        _isLoading.value = true
        pageNumber = 0
        currentQuery = query
        currentTags = tags
        sortType = sort

        Log.d("SEARCH_QUERY", "Query: $query, Tags: $tags, Sort: $sort")

        recipeService.searchRecipes(query, tags, pageNumber, pageSize, sortType.name.lowercase()) { recipes, error ->
            _isLoading.value = false

            // Check if recipes are null or empty
            if (!recipes.isNullOrEmpty()) {
                try {
                    val userId = sessionManager.getUserId()

                    // Mark favorited recipes using the stored favorite IDs
                    recipes.forEach { recipe ->
                        // Check if the recipe is favorited by the user using getFavoritedStatus
                        recipe.isFavoritedByUser = sessionManager.getFavoritedStatus(userId, recipe.id)
                        Log.d("SEARCH_QUERY", "Recipe: ${recipe.id}, Favorited: ${recipe.isFavoritedByUser}")
                    }
                    _searchResults.value = recipes
                } catch (e: Exception) {
                    _errorMessage.value = "Error while processing favorites: ${e.message}"
                    Log.e("SEARCH_QUERY", "Error processing favorites", e)
                }
            } else {
                _searchResults.value = null
                _errorMessage.value = error ?: "No results found"
                Log.e("SEARCH_QUERY", "Error: ${error ?: "No results found"}")
            }
        }
    }




    fun loadMoreRecipes(query: String, tags: Set<String>?) {
        // Prevent multiple load requests if no more recipes are available
        if (_noMoreRecipes.value == true || _isLoadingMore.value == true) {
            return // Exit early if there are no more recipes or it's already loading
        }

        pageNumber++
        _isLoadingMore.value = true  // Set isLoadingMore to true when loading more recipes
        _isLoading.value =
            false    // Set isLoading to false since we're loading more, not initial recipes

        recipeService.searchRecipes(query, tags, pageNumber, pageSize, sortType.name.lowercase()) { newRecipes, error ->
            _isLoading.value = false
            if (newRecipes != null && newRecipes.isNotEmpty()) {
                val currentResults = _searchResults.value?.toMutableList() ?: mutableListOf()
                currentResults.addAll(newRecipes)

                // Get the user's user ID
                val userId = sessionManager.getUserId()
                if (userId != -1) {
                    // Step 1: Mark favorited recipes using the getFavoritedStatus from SessionManager
                    newRecipes.forEach { recipe ->
                        // Check if the recipe is favorited by the user
                        recipe.isFavoritedByUser = sessionManager.getFavoritedStatus(userId, recipe.id)
                    }

                    // Step 2: Update the LiveData with the updated recipes
                    _searchResults.value = currentResults
                } else {
                    // If no user is logged in, set all recipes' favorites to false
                    newRecipes.forEach { recipe ->
                        recipe.isFavoritedByUser = false
                    }

                    // Update the LiveData with the new list of recipes
                    _searchResults.value = currentResults
                }
            } else {
                _noMoreRecipes.value = true  // No more recipes to load
            }
            _isLoadingMore.value = false
        }
    }



    fun updateFavoriteStatus(recipe: RecipeCard, isFavorited: Boolean) {
        viewModelScope.launch {
            val userId = sessionManager.getUserId()
            if (userId != -1) {
                try {
                    // Save the favorited status to the session
                    sessionManager.setFavoritedStatus(userId, recipe.id, isFavorited)

                    // Attempt to update the backend: add or remove from favorites
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

                    val currentFavorites = sessionManager.getFavoriteRecipeIds().toMutableSet()
                    if (isFavorited) {
                        currentFavorites.add(recipe.id)
                    } else {
                        currentFavorites.remove(recipe.id)
                    }
                    sessionManager.saveFavoriteRecipeIds(currentFavorites)

                } catch (e: Exception) {
                    _favoriteActionMessage.value = "Failed to update favorite status"
                }
            }
        }
    }

    fun updateSortType(newSortType: SortType) {
        if (newSortType != sortType) {
            sortType = newSortType
            pageNumber = 0  // Reset to first page when sort type changes
            // Re-fetch the results with the current query, tags, and updated sort type
            searchByQuery(query = currentQuery, tags = currentTags, sort = newSortType)
        }
    }


}



