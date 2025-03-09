package com.hi.recipeapp.ui.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.material.snackbar.Snackbar
import com.hi.recipeapp.classes.RecipeCard
import com.hi.recipeapp.classes.SessionManager
import com.hi.recipeapp.services.RecipeService
import com.hi.recipeapp.services.UserService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import retrofit2.Call
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val recipeService: RecipeService,
    private val userService: UserService,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _recipes = MutableLiveData<List<RecipeCard>?>()
    val recipes: LiveData<List<RecipeCard>?> get() = _recipes

    private val _favoriteActionMessage = MutableLiveData<String?>() // LiveData for favorite action message
    val favoriteActionMessage: LiveData<String?> get() = _favoriteActionMessage

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    init {
        fetchRecipes()
    }

    fun fetchRecipes() {
        val userId = sessionManager.getUserId()  // Get the user ID from session
        _isLoading.value = true

        // Step 1: Fetch all recipes
        recipeService.fetchRecipes { recipes, error ->
            if (error != null) {
                _errorMessage.value = error
                _isLoading.value = false
            } else {
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
                    // If no user is logged in, set all favorites to false
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
                // Save the favorited status to the session or backend
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
}

