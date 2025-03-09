package com.hi.recipeapp.ui.myrecipes

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hi.recipeapp.classes.RecipeCard
import com.hi.recipeapp.classes.SessionManager
import com.hi.recipeapp.services.RecipeService
import com.hi.recipeapp.services.UserService
import com.hi.recipeapp.classes.UserRecipeCard
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyRecipesViewModel @Inject constructor(
    private val userService: UserService,
    private val recipeService: RecipeService,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _favoriteRecipes = MutableLiveData<List<RecipeCard>?>()
    val favoriteRecipes: LiveData<List<RecipeCard>?> = _favoriteRecipes

    private val _userRecipes = MutableLiveData<List<UserRecipeCard>?>()
    val userRecipes: LiveData<List<UserRecipeCard>?> = _userRecipes

    private val _favoriteResult = MutableLiveData<Result<String>>()
    val favoriteResult: LiveData<Result<String>> get() = _favoriteResult

    private val _favoriteActionMessage = MutableLiveData<String?>()
    val favoriteActionMessage: LiveData<String?> get() = _favoriteActionMessage

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage
    // Function to load user recipes
    fun fetchUserRecipes(page: Int = 0, size: Int = 10) {
        viewModelScope.launch {
            val result = userService.getUserRecipes(page, size)
            result.onSuccess { recipes ->
                _userRecipes.postValue(recipes)  // Update the LiveData with fetched recipes
            }.onFailure { exception ->
                // Handle failure (e.g., show error message)
                Log.e("UserRecipes", "Failed to fetch recipes: ${exception.message}")
            }
        }
    }

    fun fetchFavoriteRecipes() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val userId = sessionManager.getUserId()
                if (userId != -1) {
                    val result = userService.getUserFavorites(userId)
                    result.onSuccess { favoriteRecipes ->
                        favoriteRecipes.forEach { recipe ->
                            recipe.isFavoritedByUser = true
                        }
                        _favoriteRecipes.value = favoriteRecipes
                        _isLoading.value = false
                    }
                    result.onFailure { error ->
                        _errorMessage.value = error.localizedMessage ?: "Failed to fetch favorite recipes"
                        _isLoading.value = false
                    }
                } else {
                    _errorMessage.value = "User not logged in"
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _errorMessage.value = "Network request failed: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun updateFavoriteStatus(recipe: RecipeCard, isFavorited: Boolean) {
        viewModelScope.launch {
            val userId = sessionManager.getUserId()
            if (userId != -1) {
                sessionManager.setFavoritedStatus(userId, recipe.id, isFavorited)

                try {
                    if (isFavorited) {
                        recipeService.addRecipeToFavorites(recipe.id)
                        _favoriteActionMessage.value = "Recipe added to favorites"
                    } else {
                        recipeService.removeRecipeFromFavorites(recipe.id)
                        _favoriteActionMessage.value = "Recipe removed from favorites"
                    }

                    _favoriteRecipes.value = _favoriteRecipes.value?.map {
                        if (it.id == recipe.id) it.copy(isFavoritedByUser = isFavorited) else it
                    }
                } catch (e: Exception) {
                    _favoriteActionMessage.value = "Failed to update favorite status"
                }
            }
        }
    }
}
