package com.hi.recipeapp.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hi.recipeapp.classes.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val sessionManager: SessionManager
) : ViewModel() {
    private val _profilePic = MutableLiveData<String?>()
    val profilePic: LiveData<String?> get() = _profilePic
    private val _isAdmin = MutableLiveData<Boolean>()
    val isAdmin: LiveData<Boolean> = _isAdmin

    init {
        loadProfilePic()
        checkIfUserIsAdmin()
    }

    private fun loadProfilePic() {
        val pic = sessionManager.getProfilePic()
        _profilePic.value = if (!pic.isNullOrEmpty()) pic else null
    }

    fun logout() {
        viewModelScope.launch {
            sessionManager.clearSession()
        }
    }

    private fun checkIfUserIsAdmin() {
        // Assume you have a session manager or user info
        val user = sessionManager.isAdmin()
        _isAdmin.value = user == true
    }

}