package com.hi.recipeapp.ui.settings

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hi.recipeapp.classes.SessionManager
import com.hi.recipeapp.services.UserService
import com.hi.recipeapp.ui.networking.NetworkService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PasswordViewModel @Inject constructor(
    private val userService: UserService,
    private val sessionManager: SessionManager
) : ViewModel() {
    private val _passwordChangeState = MutableLiveData<Result<String>>()
    val passwordChangeState: LiveData<Result<String>> = _passwordChangeState

    /**
     * Calls userService with the password, and the confirmation of new password.
     * Users current password is fetched from SessionManager.
     */
    fun updatePassword(newPassword: String, againNewPassword: String){
        viewModelScope.launch {
            val result = userService.changePassword(
                userId = sessionManager.getUserId(),
                currentPassword = sessionManager.getPassword() ?: "",
                newPassword = newPassword,
                confirmNewPassword = againNewPassword
            )
            _passwordChangeState.value = result
        }
    }

}