package com.hi.recipeapp.ui.welcomepage

import android.util.Log
import androidx.lifecycle.*
import com.hi.recipeapp.classes.SessionManager
import com.hi.recipeapp.classes.UserDTO
import com.hi.recipeapp.services.UserService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val userService: UserService,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _loginResult = MutableLiveData<UserDTO?>()
    val loginResult: LiveData<UserDTO?> get() = _loginResult

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    fun login(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            _errorMessage.value = "Please enter both username and password"
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            try {
                val user = userService.login(username, password)

                _isLoading.value = false

                if (user != null) {
                    _loginResult.value = user // Successful login
                    // Save user data to session
                    sessionManager.savePassword(user.password)
                    sessionManager.saveUserId(user.id)
                    sessionManager.saveUserNameAndRole(user.username, user.role)
                    sessionManager.saveProfilePic(user.profilePictureUrl)

                    saveUserFavoriteStatus(user.id)
                } else {
                    _errorMessage.value = "Login failed"
                }
            } catch (e: Exception) {
                _isLoading.value = false
                _errorMessage.value = e.message ?: "Unknown error"
            }
        }
    }

    // Suspend function to fetch and save user favorite status
    private suspend fun saveUserFavoriteStatus(userId: Int) {
        try {
            // Log the user ID before starting the process
            Log.d("UserFavoriteStatus", "Fetching favorites for user ID: $userId")

            // Fetch user favorites using the suspend function
            val result = userService.getUserFavorites(userId)
            if (result.isSuccess) {
                val favoriteRecipes = result.getOrDefault(emptyList())

                // Log the fetched favorite recipes
                Log.d("UserFavoriteStatus", "Fetched favorite recipes for user ID $userId: ${favoriteRecipes.map { it.id }}")

                // Get the IDs of the favorite recipes
                val favoriteRecipeIds = favoriteRecipes.map { it.id }.toSet()

                // Save the favorite recipe IDs to session (SharedPreferences)
                sessionManager.saveFavoriteRecipeIds(favoriteRecipeIds)

                // Set the favorited status for each recipe in session
                favoriteRecipes.forEach { recipe ->
                    // Log each individual recipe ID being set as favorited
                    Log.d("UserFavoriteStatus", "Setting favorited status for recipe ID ${recipe.id} for user ID $userId")
                    sessionManager.setFavoritedStatus(userId, recipe.id, true)
                }

                // Log successful completion
                Log.d("UserFavoriteStatus", "Successfully saved favorite status for user ID $userId")
            } else {
                _errorMessage.value = "Failed to fetch favorites"
                Log.e("UserFavoriteStatus", "Failed to fetch favorites for user ID: $userId")
            }
        } catch (e: Exception) {
            // Log the exception and set the error message
            _errorMessage.value = "Error fetching favorites: ${e.message}"
            Log.e("UserFavoriteStatus", "Error fetching favorites for user ID $userId: ${e.message}")
        }
    }




}

