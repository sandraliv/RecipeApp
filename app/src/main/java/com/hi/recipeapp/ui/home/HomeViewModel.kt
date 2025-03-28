package com.hi.recipeapp.ui.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hi.recipeapp.classes.RecipeCard
import com.hi.recipeapp.classes.SessionManager
import com.hi.recipeapp.classes.SortType
import com.hi.recipeapp.services.RecipeService
import com.hi.recipeapp.services.UserService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val recipeService: RecipeService,
    private val userService: UserService,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _recipes = MutableLiveData<List<RecipeCard>?>()
    val recipes: LiveData<List<RecipeCard>?> get() = _recipes

    private val _favoriteActionMessage = MutableLiveData<String?>()
    val favoriteActionMessage: LiveData<String?> get() = _favoriteActionMessage

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    // Add this to your ViewModel
    private val _noMoreRecipes = MutableLiveData<Boolean>(false)
    val noMoreRecipes: LiveData<Boolean> = _noMoreRecipes


    private var pageNumber = 0  // Track the page number for pagination
    private val pageSize = 20    // Define how many items to load per page


    init {
        fetchRecipesSortedBy(sortType = SortType.RATING)
    }

    fun fetchRecipesSortedBy(sortType: SortType) {
        val userId = sessionManager.getUserId()
        _isLoading.value = true
        // Clear the existing recipes list before fetching new data
        _recipes.value = null
        val sortString = sortType.name.lowercase()

        // Step 1: Fetch sorted recipes
        recipeService.fetchRecipes(sortString) { recipes, error ->
            if (error != null) {
                _errorMessage.value = error
                _isLoading.value = false
            } else {
                // Log each recipe's image URLs directly
                recipes?.forEach { recipe ->
                    val imageUrls = recipe.imageUrls  // Access imageUrls directly
                    if (imageUrls.isNullOrEmpty()) {
                        Log.d("HomeViewModel", "Recipe ID: ${recipe.id} has no images.")
                    } else {
                        imageUrls.forEach { url ->
                            Log.d("HomeViewModel", "Recipe ID: ${recipe.id} Image URL: $url")
                        }
                    }
                }

                // Step 2: Fetch the user's favorite recipes
                if (userId != -1) {
                    viewModelScope.launch {
                        val userFavorites = userService.getUserFavorites(userId).getOrNull() ?: emptyList()

                        // Step 3: Mark favorited recipes
                        recipes?.forEach { recipe ->
                            recipe.isFavoritedByUser = userFavorites.any { it.id == recipe.id }
                        }

                        // Step 4: Update the LiveData with the updated recipes
                        _recipes.value = recipes
                    }
                } else {
                    recipes?.forEach { recipe ->
                        recipe.isFavoritedByUser = false
                    }
                    _recipes.value = recipes
                }
                _isLoading.value = false
            }
        }
    }



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
                    _recipes.value = _recipes.value?.map {
                        if (it.id == recipe.id) it.copy(isFavoritedByUser = isFavorited) else it
                    }
                } catch (e: Exception) {
                    _favoriteActionMessage.value = "Failed to update favorite status"
                }
            }
        }
    }

    fun loadMoreRecipes() {
        pageNumber++ // Increment the page number to load the next page
        _isLoading.value = true

        recipeService.fetchRecipes(page = pageNumber, size = pageSize, sort = "rating") { newRecipes, error ->
            if (error != null) {
                _errorMessage.value = error
                _isLoading.value = false
            } else {
                val userId = sessionManager.getUserId()
                viewModelScope.launch {
                    // If the user is logged in, fetch their favorite recipes
                    if (userId != -1) {
                        val userFavorites = userService.getUserFavorites(userId).getOrNull() ?: emptyList()

                        // Mark the new recipes as favorited or not based on the user's favorites
                        newRecipes?.forEach { recipe ->
                            recipe.isFavoritedByUser = userFavorites.any { it.id == recipe.id }
                        }
                    } else {
                        // If the user is not logged in, mark all recipes as not favorited
                        newRecipes?.forEach { recipe ->
                            recipe.isFavoritedByUser = false
                        }
                    }

                    // Combine the current list of recipes with the new ones
                    val updatedRecipes = _recipes.value?.toMutableList() ?: mutableListOf()
                    updatedRecipes.addAll(newRecipes ?: emptyList())

                    // Check if there are no more recipes to load
                    if (newRecipes.isNullOrEmpty()) {
                        _noMoreRecipes.value = true // Set this to true to show "No More Recipes Available" message
                        binding.loadMoreButton.visibility = View.GONE
                    } else {
                        _recipes.value = updatedRecipes
                        _isLoading.value = false
                    }
                }
            }
        }
    }


    fun updateSortType(newSortType: SortType) {
        // Optional: Check if the sort type has already been set to avoid unnecessary fetches
        if (_recipes.value != null) {
            fetchRecipesSortedBy(newSortType)
        } else {
            // If no recipes are currently loaded, fetch with the new sort type
            fetchRecipesSortedBy(newSortType)
        }
    }


}


