package com.hi.recipeapp.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hi.recipeapp.classes.SessionManager
import com.hi.recipeapp.services.UserService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userService: UserService,
    private val sessionManager: SessionManager
) : ViewModel() {
    private val _profilePic = MutableLiveData<String?>()
    val profilePic: LiveData<String?> get() = _profilePic

    init {
        loadProfilePic()
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

}