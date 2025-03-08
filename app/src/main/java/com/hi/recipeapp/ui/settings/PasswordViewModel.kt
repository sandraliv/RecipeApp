package com.hi.recipeapp.ui.settings

import androidx.lifecycle.ViewModel
import com.hi.recipeapp.classes.SessionManager
import com.hi.recipeapp.services.UserService
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PasswordViewModel @Inject constructor(
    private val userService: UserService,
    private val sessionManager: SessionManager
) : ViewModel() {

}