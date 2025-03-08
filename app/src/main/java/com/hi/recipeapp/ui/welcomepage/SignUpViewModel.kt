package com.hi.recipeapp.ui.welcomepage

import androidx.lifecycle.*
import com.hi.recipeapp.classes.UserDTO
import com.hi.recipeapp.services.UserService
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SignupViewModel @Inject constructor(
    private val userService: UserService
) : ViewModel(){
    private val _signupResult = MutableLiveData<UserDTO?>()
    val signupResult: LiveData<UserDTO?> get() = _signupResult

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _navigateToLogin = MutableLiveData<Boolean>()
    val navigateToLogin: LiveData<Boolean> get() = _navigateToLogin

    fun signup(role: String, name: String, email: String, password: String, username: String) {
        if (role.isBlank() || name.isBlank() || email.isBlank() || password.isBlank() || username.isBlank()) {
            _errorMessage.value = "Please fill in all fields"
            return
        }

        _isLoading.value = true
        userService.signup(role, name, email, password, username) { user, error ->
            _isLoading.postValue(false)
            if (user != null) {
                _signupResult.postValue(user)
            } else {
                _errorMessage.postValue(error ?: "Signup failed, please try again")
            }
        }
    }
    fun onNavigateToLoginComplete() {
        _navigateToLogin.value = false // Reset event
    }
}
