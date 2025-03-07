package com.hi.recipeapp.ui.welcomepage

import androidx.lifecycle.*
import com.hi.recipeapp.classes.SessionManager
import com.hi.recipeapp.classes.UserDTO
import com.hi.recipeapp.services.UserService
import dagger.hilt.android.lifecycle.HiltViewModel
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
        userService.login(username, password) { user, error ->
            _isLoading.postValue(false)
            if (user != null) {
                _loginResult.postValue(user) // Innskráning tókst
                // Save user data to session
                sessionManager.saveUserId(user.id)  // Save user ID
                sessionManager.saveUserName(user.username)  // Save user name

            } else {
                _errorMessage.postValue(error ?: "Unknown error, please try again") // ❌ Villa í innskráningu
            }
        }
    }
}

