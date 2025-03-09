package com.hi.recipeapp.ui.fullrecipe

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hi.recipeapp.classes.UserFullRecipe
import com.hi.recipeapp.services.UserService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserFullRecipeViewModel @Inject constructor(
    private val userService: UserService
) : ViewModel() {

    private val _userrecipe = MutableLiveData<UserFullRecipe?>()
    val userrecipe: LiveData<UserFullRecipe?> = _userrecipe

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    fun fetchUserRecipeById(id: Int) {
        viewModelScope.launch {
            try {
                val result = userService.fetchUserRecipeById(id)
                if (result != null) {
                    _userrecipe.value = result
                } else {
                    _errorMessage.value = "Error fetching recipe"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error fetching recipe: ${e.localizedMessage}"
            }
        }
    }

    fun clearData() {
        _userrecipe.value = null
        _errorMessage.value = null
    }
}
