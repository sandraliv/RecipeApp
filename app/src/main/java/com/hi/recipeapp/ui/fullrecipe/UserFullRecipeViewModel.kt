package com.hi.recipeapp.ui.fullrecipe

import android.util.Log
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
    private val userService: UserService // Inject your service here
) : ViewModel() {

    // LiveData to hold the fetched recipe
    private val _userrecipe = MutableLiveData<UserFullRecipe?>()
    val userrecipe: LiveData<UserFullRecipe?> = _userrecipe

    // LiveData to hold error messages
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    // Fetch the user recipe by ID (suspend function now used directly)
    fun fetchUserRecipeById(id: Int) {
        // Launch a coroutine to fetch data
        viewModelScope.launch {
            try {
                // Call the suspend function directly
                val result = userService.fetchUserRecipeById(id)

                // Update the LiveData with the result
                if (result != null) {
                    _userrecipe.value = result
                } else {
                    _errorMessage.value = "Error fetching recipe"
                }
            } catch (e: Exception) {
                // Handle any exceptions (network errors, etc.)
                _errorMessage.value = "Error fetching recipe: ${e.localizedMessage}"
            }
        }
    }

    // Optional: You can also implement a function to clear data if needed
    fun clearData() {
        _userrecipe.value = null
        _errorMessage.value = null
    }
}
