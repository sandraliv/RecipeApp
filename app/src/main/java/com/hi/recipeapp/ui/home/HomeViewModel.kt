package com.hi.recipeapp.ui.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hi.recipeapp.classes.FullRecipe
import com.hi.recipeapp.classes.RecipeCard
import com.hi.recipeapp.classes.SessionManager
import com.hi.recipeapp.classes.SortType
import com.hi.recipeapp.services.RecipeService
import com.hi.recipeapp.services.UserService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val recipeService: RecipeService,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _recipes = MutableLiveData<List<RecipeCard>?>()
    val recipes: LiveData<List<RecipeCard>?> get() = _recipes

    private val _isLoadingMore = MutableLiveData<Boolean>(false)
    val isLoadingMore: LiveData<Boolean> get() = _isLoadingMore

    private val _favoriteActionMessage = MutableLiveData<String?>()
    val favoriteActionMessage: LiveData<String?> get() = _favoriteActionMessage

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    private val _isAdmin = MutableLiveData<Boolean?>()
    val isAdmin: LiveData<Boolean?> get() = _isAdmin

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    // Add this to your ViewModel
    private val _noMoreRecipes = MutableLiveData<Boolean>(false)
    val noMoreRecipes: LiveData<Boolean> = _noMoreRecipes

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    private val _editableRecipe = MutableLiveData<FullRecipe>()
    val editableRecipe: LiveData<FullRecipe> = _editableRecipe

    private var pageNumber = 0  // Track the page number for pagination
    private val pageSize = 20    // Define how many items to load per page


    init {
        fetchRecipesSortedBy(sortType = SortType.RATING)
        checkIfAdmin()
    }

    private fun checkIfAdmin(){
        _isAdmin.value = sessionManager.isAdmin();
    }

    fun fetchRecipesSortedBy(sortType: SortType) {
        val userId = sessionManager.getUserId()
        _isLoading.value = true
        _recipes.value = null
        val sortString = sortType.name.lowercase()

        // Fetch recipes from the backend
        recipeService.fetchRecipes(
            page = pageNumber,
            size = pageSize,
            sort = sortString
        ) { recipes, error ->
            if (error != null) {
                _errorMessage.value = error
                _isLoading.value = false
            } else {
                // Assign the "isFavoritedByUser" status from SessionManager (local data)
                if (userId != -1) {
                    recipes?.forEach { recipe ->
                        // Set the favorited status from SessionManager
                        recipe.isFavoritedByUser = sessionManager.getFavoritedStatus(userId, recipe.id)
                    }
                } else {
                    recipes?.forEach { recipe ->
                        recipe.isFavoritedByUser = false // Default to false if not logged in
                    }
                }

                // Update the LiveData with the recipes
                _recipes.value = recipes
                _isLoading.value = false
            }
        }
    }




    fun updateFavoriteStatus(recipe: RecipeCard, isFavorited: Boolean) {
        viewModelScope.launch {
            val userId = sessionManager.getUserId()
            if (userId != -1) {
                try {
                    // Step 1: Update the favorited status in SessionManager
                    sessionManager.setFavoritedStatus(userId, recipe.id, isFavorited)

                    // Step 2: Update the backend (add or remove from favorites)
                    if (isFavorited) {
                        recipeService.addRecipeToFavorites(recipe.id)
                        _favoriteActionMessage.value = "Recipe added to favorites"
                    } else {
                        recipeService.removeRecipeFromFavorites(recipe.id)
                        _favoriteActionMessage.value = "Recipe removed from favorites"
                    }

                    // Step 3: Update the local list of recipes with the new favorited status
                    _recipes.value = _recipes.value?.map {
                        if (it.id == recipe.id) it.copy(isFavoritedByUser = isFavorited) else it
                    }

                    // Step 4: Update the favorite recipe IDs in SessionManager
                    val currentFavorites = sessionManager.getFavoriteRecipeIds().toMutableSet()
                    if (isFavorited) {
                        currentFavorites.add(recipe.id)  // Add to favorites set
                    } else {
                        currentFavorites.remove(recipe.id)  // Remove from favorites set
                    }

                    // Save the updated favorite recipe IDs to SharedPreferences
                    sessionManager.saveFavoriteRecipeIds(currentFavorites)

                } catch (e: Exception) {
                    _favoriteActionMessage.value = "Failed to update favorite status"
                    Log.e("HomeViewModel", "Error updating favorite status: ${e.message}")
                }
            }
        }
    }

    fun loadMoreRecipes(sortType: SortType) {
        // Prevent multiple load requests if no more recipes are available
        if (_noMoreRecipes.value == true || _isLoadingMore.value == true) {
            return // Exit early if there are no more recipes or it's already loading
        }

        pageNumber++
        _isLoadingMore.value = true  // Set isLoadingMore to true when loading more recipes
        _isLoading.value =
            false    // Set isLoading to false since we're loading more, not initial recipes

        // Use the passed-in sortType directly rather than fetching it from sessionManager
        val sortString =
            sortType.name.lowercase()  // Convert SortType enum to string (e.g., "rating", "date")

        // Fetch recipes based on current sort type
        recipeService.fetchRecipes(
            page = pageNumber,
            size = pageSize,
            sort = sortString
        ) { newRecipes, error ->

            if (error != null) {
                _errorMessage.value = error
                _isLoadingMore.value = false  // Reset loading more state
            } else {
                val userId = sessionManager.getUserId()

                // Check if there are any new recipes
                if (!newRecipes.isNullOrEmpty()) {
                    // Use the sessionManager to mark the new recipes based on the stored favorited status
                    newRecipes.forEach { recipe ->
                        // Check if the user is logged in and then use sessionManager to get the favorited status
                        if (userId != -1) {
                            val isFavorited = sessionManager.getFavoritedStatus(userId, recipe.id)
                            recipe.isFavoritedByUser = isFavorited
                        } else {
                            // If not logged in, mark the recipe as not favorited
                            recipe.isFavoritedByUser = false
                        }
                    }

                    // Combine the current list of recipes with the new ones
                    val updatedRecipes = _recipes.value?.toMutableList() ?: mutableListOf()
                    updatedRecipes.addAll(newRecipes)

                    // Update the recipes list
                    _recipes.value = updatedRecipes
                }

                // Check if there are no more recipes to load
                if (newRecipes.isNullOrEmpty()) {
                    _noMoreRecipes.value = true // No more recipes to load
                    _isLoadingMore.value = false // Stop loading
                    return@fetchRecipes // Exit the function early
                }

                _isLoadingMore.value =
                    false // Reset the isLoadingMore flag after loading is complete
            }
        }
    }

    fun deleteRecipe(recipeId: Int) {
        viewModelScope.launch {
            val result = recipeService.deleteRecipe(recipeId)
            result
                .onSuccess {
                    fetchRecipesSortedBy(SortType.RATING)
                    Log.d("RECIPES FAIL", "NOT FAIL")
                }
                .onFailure {
                    Log.d("RECIPES FAIL", "FAIL")
                    _error.value = "Failed to delete user: ${it.message}"
                }
        }
    }

    fun updateSortType(newSortType: SortType) {
        // Optional: Check if the sort type has already been set to avoid unnecessary fetches
        if (_recipes.value != null) {
            fetchRecipesSortedBy(newSortType)
        } else {
            // If no recipes are currently loaded, fetch with the new sort type
        }
    }

    fun editRecipe(recipeId: Int) {
        recipeService.fetchRecipeById(recipeId) { result, _ ->
            result?.let {
                _editableRecipe.postValue(it)
            }
        }
    }



}

