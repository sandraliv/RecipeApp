package com.hi.recipeapp.ui.myrecipes

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hi.recipeapp.classes.RecipeCard
import com.hi.recipeapp.classes.SessionManager
import com.hi.recipeapp.services.RecipeService
import com.hi.recipeapp.services.UserService
import com.hi.recipeapp.classes.FavoriteRecipesDTO
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

    private val _myRecipes = MutableLiveData<List<RecipeCard>?>()
    val myRecipes: LiveData<List<RecipeCard>?> = _myRecipes

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

}

