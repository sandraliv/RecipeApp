package com.hi.recipeapp.ui.myrecipes

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hi.recipeapp.classes.FullRecipe
import com.hi.recipeapp.classes.RecipeCard
import com.hi.recipeapp.classes.SessionManager
import com.hi.recipeapp.services.RecipeService
import com.hi.recipeapp.services.UserService
import com.hi.recipeapp.classes.UserRecipeCard
import com.hi.recipeapp.data.local.Recipe
import com.hi.recipeapp.data.local.RecipeDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.forEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyRecipesViewModel @Inject constructor(
    private val userService: UserService,
    private val recipeService: RecipeService,
    private val sessionManager: SessionManager,
    private val recipeDao: RecipeDao
) : ViewModel() {

    private val _favoriteRecipes = MutableLiveData<List<RecipeCard>?>()
    val favoriteRecipes: LiveData<List<RecipeCard>?> = _favoriteRecipes

    private val _recipeDeleted = MutableLiveData<String>()
    val recipeDeleted: LiveData<String> = _recipeDeleted

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
                val localFavorites = recipeDao.getAll().first()
                if (localFavorites.isNotEmpty()) {
                    _favoriteRecipes.value = localFavorites
                    _isLoading.value = false
                    Log.d("RECIPES TESTING", "I AM TAKING FROM DATABASE HELLO")
                    getNewestFavouriteRecipes()
                } else {
                    fetchMyFavoriteRecipes()
                }
            } catch (e: Exception) {
                _errorMessage.value = "Database load failed: ${e.message}"
                Log.d("DATABASE ERROR", "${e.message}")
                _isLoading.value = false
            }
        }
    }

    private fun getNewestFavouriteRecipes() {
        viewModelScope.launch {
            try {
                fetchMyFavoriteRecipes() // just call the same suspend method
            } catch (e: Exception) {
                // If refresh fails, we still have the cached data
                Log.e("FavoriteVM", "Refresh failed: ${e.message}")
            }
        }
    }

    private fun fetchMyFavoriteRecipes() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val userId = sessionManager.getUserId()
                if (userId != -1) {

                    val result = userService.getUserFavorites(userId)
                    result.onSuccess { favoriteRecipes ->

                        Log.d("TEST", "EKKI VILLA Í VM")
                        _isLoading.value = false
                        favoriteRecipes.forEach { it.isFavoritedByUser = true }
                        favoriteRecipes.forEach { recipe ->
                            Log.d("Recipe", recipe.title)
                        }

                        _favoriteRecipes.value = favoriteRecipes

                        val recipeEntities = favoriteRecipes.map { it.toEntity() }
                        recipeDao.insertAll(recipeEntities)

                        Log.d("HALLOHEIMUR", "ÉG ER Í DABASE")

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

    private fun RecipeCard.toEntity(): Recipe {
        return Recipe(
            id = id,
            title = title,
            description = description,
            imageUrls = imageUrls,
            averageRating = averageRating,
            ratingCount = ratingCount,
            tags = tags,
            isFavoritedByUser = isFavoritedByUser
        )
    }

    fun deleteRecipe(recipeId: Int) {
        viewModelScope.launch {
            try {
                val result = recipeService.deleteUserRecipe(recipeId, sessionManager.getUserId())
                if(result.isSuccessful) {
                    _recipeDeleted.value = "Recipe deleted"
                } else {
                    _recipeDeleted.value = "There was an error, try again"
                }
            } catch (e: Exception) {
                _recipeDeleted.value = "There was an error, try again"
            }

        }
    }



}
