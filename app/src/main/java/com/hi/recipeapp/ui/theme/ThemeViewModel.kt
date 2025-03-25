package com.hi.recipeapp.ui.theme

import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hi.recipeapp.classes.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _isDarkMode = MutableLiveData<Boolean>()
    val isDarkMode: LiveData<Boolean> get() = _isDarkMode

    init {
        val savedMode = sessionManager.isDarkModeEnabled()
        _isDarkMode.value = savedMode
        applyTheme(savedMode)
    }

    fun toggleTheme() {
        val newMode = !_isDarkMode.value!!
        _isDarkMode.value = newMode
        sessionManager.setDarkMode(newMode)
        applyTheme(newMode)
    }

    private fun applyTheme(enable: Boolean) {
        AppCompatDelegate.setDefaultNightMode(
            if (enable) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }
}



