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
import com.hi.recipeapp.classes.FavoriteRecipesDTO
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

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    fun fetchFavoriteRecipes() {
        _isLoading.value = true  // Start loading
        viewModelScope.launch {
            try {
                // Fetch the favorite recipes wrapped in FavoriteRecipesDTO from the service
                val result = userService.getUserFavorites()  // This should return Result<FavoriteRecipesDTO>

                result.onSuccess { favoriteRecipes ->
                    // Handle the successful result
                    _favoriteRecipes.value = favoriteRecipes // List of favorite recipes
                }

                result.onFailure { error ->
                    // On failure, update the error message
                    _errorMessage.value = error.localizedMessage ?: "Unknown error"
                }
            } catch (e: Exception) {
                // Handle exceptions or network failures
                _errorMessage.value = "Network request failed: ${e.message}"
            }

            _isLoading.value = false  // End loading
        }
    }

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

}

