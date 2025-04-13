package com.hi.recipeapp.ui.bycategory

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hi.recipeapp.classes.Category
import com.hi.recipeapp.classes.RecipeCard
import com.hi.recipeapp.classes.SessionManager
import com.hi.recipeapp.classes.SortType
import com.hi.recipeapp.services.RecipeService
import com.hi.recipeapp.services.UserService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val recipeService: RecipeService,
    private val userService: UserService,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _recipesByCategory = MutableLiveData<List<RecipeCard>>(emptyList()) // Non-nullable, initial value is empty list
    val recipesByCategory: LiveData<List<RecipeCard>> = _recipesByCategory


    private val _errorMessage = MutableLiveData<String?>() // Nullable type for error message
    val errorMessage: LiveData<String?> = _errorMessage


    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _favoriteActionMessage = MutableLiveData<String?>()
    val favoriteActionMessage: LiveData<String?> get() = _favoriteActionMessage
    // Pagination variables
    private var pageNumber = 1 // Track the current page number for pagination
    private val pageSize = 20 // Number of items per page

    private var currentSort = "rating" // Default sort

    /**
     * Fetches recipes based on the given category and sort type.
     * Updates the LiveData with the fetched recipes and their favorite status.
     *
     * @param category The category for which recipes are being fetched.
     * @param sort The sort type (e.g., rating, popularity).
     */
    fun getRecipesByCategory(category: Category, sort: String = "rating") {
        currentSort = sort // Store the current sort type

        _errorMessage.value = null
        _isLoading.value = true
        Log.d("CATEGORY_QUERY", "Category: $category, Sort: $sort")

        // Call the RecipeService to get recipes by category
        recipeService.getRecipesByCategory(category, sort) { recipes, error ->
            _isLoading.value = false

            // Ensure that recipes is not null before assigning it
            val nonNullRecipes = recipes ?: emptyList()  // If recipes is null, use an empty list.

            if (nonNullRecipes.isNotEmpty()) {
                // Step 1: Get the user's favorite recipes
                val userId = sessionManager.getUserId()
                if (userId != -1) {
                    viewModelScope.launch {
                        val userFavorites = userService.getUserFavorites(userId).getOrNull() ?: emptyList()

                        // Step 2: Mark favorited recipes
                        nonNullRecipes.forEach { recipe ->
                            recipe.isFavoritedByUser = userFavorites.any { it.id == recipe.id }
                        }

                        _recipesByCategory.value = nonNullRecipes // Assign non-null list
                    }
                } else {
                    // If no user is logged in, set all favorites to false
                    nonNullRecipes.forEach { recipe ->
                        recipe.isFavoritedByUser = false
                    }
                    _recipesByCategory.value = nonNullRecipes // Assign non-null list
                }
            } else {
                // If no recipes found, assign an empty list
                _recipesByCategory.value = emptyList() // Assign non-null empty list
                _errorMessage.value = error ?: "No results found"
            }
        }
    }

    /**
     * Loads more recipes for a given category when scrolling or requesting more.
     * Appends the new recipes to the existing list and updates the LiveData.
     *
     * @param category The category for which more recipes are being fetched.
     * @param sort The sort type (e.g., rating, popularity).
     */
    fun loadMoreRecipes(category: Category, sort: String = "rating") {
        pageNumber++ // Increment the page number to load the next page
        _isLoading.value = true

        recipeService.getRecipesByCategory(category, sort) { newRecipes, error ->
            _isLoading.value = false

            val nonNullRecipes = newRecipes ?: emptyList()

            if (nonNullRecipes.isNotEmpty()) {
                // Append new recipes to the existing list
                val updatedList = _recipesByCategory.value?.toMutableList() ?: mutableListOf()
                updatedList.addAll(nonNullRecipes)

                // Update the LiveData with the new list
                _recipesByCategory.value = updatedList
            }

            if (nonNullRecipes.size < pageSize) {
                // If fewer items are returned than the page size, we assume it's the last page
                // Optionally, disable the "load more" button or update UI accordingly
            }

            if (error != null) {
                _errorMessage.value = error
            }
        }
    }

    /**
     * Updates the favorite status of a recipe and reflects the changes in the backend and session.
     *
     * @param recipe The recipe to be updated.
     * @param isFavorited Boolean indicating the new favorite status.
     */
    fun updateFavoriteStatus(recipe: RecipeCard, isFavorited: Boolean) {
        viewModelScope.launch {
            val userId = sessionManager.getUserId()
            if (userId != -1) {
                sessionManager.setFavoritedStatus(userId, recipe.id, isFavorited)

                // Update the backend
                try {
                    if (isFavorited) {
                        recipeService.addRecipeToFavorites(recipe.id)
                        _favoriteActionMessage.value = "Recipe added to favorites"
                    } else {
                        recipeService.removeRecipeFromFavorites(recipe.id)
                        _favoriteActionMessage.value = "Recipe removed from favorites"
                    }

                    // Update the local list with the new favorited status
                    _recipesByCategory.value = _recipesByCategory.value?.map {
                        if (it.id == recipe.id) it.copy(isFavoritedByUser = isFavorited) else it
                    }
                } catch (e: Exception) {
                    _favoriteActionMessage.value = "Failed to update favorite status"
                }
            }
        }
    }
    fun updateSortType(category: Category, newSortType: String) {
        // Optional: Check if the recipes are already loaded
        if (_recipesByCategory.value?.isNotEmpty() == true) {
            // If recipes are already loaded, fetch them again with the new sort type
            getRecipesByCategory(category, newSortType)
        } else {
            // If no recipes are currently loaded, fetch with the new sort type
            getRecipesByCategory(category, newSortType)
        }
    }


}
