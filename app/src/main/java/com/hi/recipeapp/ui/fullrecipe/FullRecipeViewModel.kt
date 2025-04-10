package com.hi.recipeapp.ui.fullrecipe

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hi.recipeapp.classes.FullRecipe
import com.hi.recipeapp.classes.SessionManager
import com.hi.recipeapp.data.local.Recipe
import com.hi.recipeapp.data.local.RecipeDao
import com.hi.recipeapp.services.RecipeService
import com.hi.recipeapp.services.UserService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FullRecipeViewModel @Inject constructor(
    private val recipeService: RecipeService,
    private val sessionManager: SessionManager,
    private val userService: UserService,
    private val recipeDao: RecipeDao

) : ViewModel() {

    private val _formattedInstructions = MutableLiveData<String>()
    val formattedInstructions: LiveData<String> = _formattedInstructions

    private val _recipe = MutableLiveData<FullRecipe?>()
    val recipe: LiveData<FullRecipe?> = _recipe

    private val _favoriteActionMessage = MutableLiveData<String?>()
    val favoriteActionMessage: LiveData<String?> get() = _favoriteActionMessage

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _calendarSaveStatus = MutableLiveData<String>()
    val calendarSaveStatus: LiveData<String> get() = _calendarSaveStatus


    fun fetchRecipeById(id: Int) {
        val userId = sessionManager.getUserId()
        _isLoading.value = true

        viewModelScope.launch {
            try {
                val localRecipe = recipeDao.findByPrimaryKey(id)

                if (localRecipe != null) {
                    Log.d("FullRecipeViewModel", "Loaded recipe from local DB: $localRecipe")

                    _recipe.value = localRecipe.toFullRecipe()
                    _isLoading.value = false
                    return@launch
                }

                // Fetch the recipe by ID
                recipeService.fetchRecipeById(id) { result, error ->
                    if (result != null) {
                        _isLoading.value = true
                        // Log the fetched recipe for debugging
                        Log.d("FullRecipeViewModel", "Fetched recipe: $result")

                        // Check if the user is logged in
                        if (userId != -1) {
                            viewModelScope.launch {
                                // Fetch the user's favorite recipes
                                val userFavorites = userService.getUserFavorites(userId).getOrNull() ?: emptyList()

                                // Mark the recipe as favorited if the user has favorited it
                                result.isFavoritedByUser = userFavorites.any { it.id == result.id }

                                // Set the recipe to LiveData
                                _recipe.value = result
                            }
                        } else {
                            // If the user is not logged in, assume the recipe is not favorited
                            result.isFavoritedByUser = false
                            _recipe.value = result
                        }
                        _isLoading.value = false
                    } else {
                        Log.d("FullRecipeViewModel", "Error fetching recipe: $error")
                        _errorMessage.value = error
                    }
                }

            } catch (e: Exception) {
                Log.e("FullRecipeViewModel", "Exception: ${e.message}")
                _errorMessage.value = "Error loading recipe: ${e.message}"
                _isLoading.value = false
            }

        }
    }

    private fun Recipe.toFullRecipe(): FullRecipe {
        return FullRecipe(
            id = this.id,
            title = this.title,
            description = this.description,
            instructions = this.instructions,
            imageUrls = this.imageUrls.ifEmpty { emptyList() },
            ingredients = this.ingredients,
            isFavoritedByUser = this.isFavoritedByUser,
            averageRating = this.averageRating,
            ratingCount = this.ratingCount,
            categories = emptySet(),
            tags = this.tags)
    }



    fun updateFavoriteStatus(recipeId: Int, isFavorited: Boolean) {
        viewModelScope.launch {
            val userId = sessionManager.getUserId()
            if (userId != -1) {
                sessionManager.setFavoritedStatus(userId, recipeId, isFavorited)

                // Update the backend
                try {
                    if (isFavorited) {
                        recipeService.addRecipeToFavorites(recipeId)
                        _favoriteActionMessage.value = "Recipe added to favorites"
                    } else {
                        recipeService.removeRecipeFromFavorites(recipeId)
                        _favoriteActionMessage.value = "Recipe removed from favorites"
                    }
                } catch (e: Exception) {
                    _favoriteActionMessage.value = "Failed to update favorite status"
                }
            }
        }
    }

    fun rateRecipe(recipeId: Int, rating: Int) {
        viewModelScope.launch {
            try {
                // Call the method to update the rating
                val result = recipeService.addRecipeRating(recipeId, rating)
                // Handle the Result
                if (result.isSuccess) {
                    // Create a new FullRecipe object with updated rating and rating count
                    val updatedRecipe = _recipe.value?.copy(
                        averageRating = rating.toDouble(),
                        ratingCount = (_recipe.value?.ratingCount ?: 0) + 1
                    )

                    // Set the new updated recipe to LiveData
                    _recipe.value = updatedRecipe
                } else {
                    // Handle failure in adding rating
                    _errorMessage.value = result.exceptionOrNull()?.message ?: "Failed to update rating"
                }
            } catch (e: Exception) {
                // Handle any other errors
                _errorMessage.value = "An error occurred: ${e.message}"
            }
        }
    }

    fun prepareInstructions(raw: String) {
        viewModelScope.launch {
            val instructions = raw.split(".")
                .mapNotNull { it.trim().takeIf { it.isNotEmpty() } }
                .mapIndexed { index, step -> "${index + 1}. $step." }
                .joinToString("\n\n")

            _formattedInstructions.postValue(instructions)
        }
    }

    fun saveRecipeToCalendar(userId: Int, recipeId: Int, date: String) {
        viewModelScope.launch {

            val result = recipeService.saveRecipeToCalendar(userId, recipeId, date)
            result.onSuccess {
                _calendarSaveStatus.value = "Recipe saved to calendar on $date"
            }.onFailure {
                _calendarSaveStatus.value = "Error saving recipe: ${it.message}"
            }
        }
    }
}
