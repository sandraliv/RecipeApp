package com.hi.recipeapp.ui.welcomepage

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
                    sessionManager.saveUserName(user.username)
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

    // Make the function suspend so we can call suspend functions inside it
    private suspend fun saveUserFavoriteStatus(userId: Int) {
        try {
            val result = userService.getUserFavorites(userId)
            if (result.isSuccess) {
                val favoriteRecipes = result.getOrDefault(emptyList())
                val favoriteRecipeIds = favoriteRecipes.map { it.id }.toSet()
                sessionManager.saveFavoriteRecipeIds(favoriteRecipeIds)
            } else {
                _errorMessage.value = "Failed to fetch favorites"
            }
        } catch (e: Exception) {
            _errorMessage.value = "Error fetching favorites: ${e.message}"
        }
    }

}

