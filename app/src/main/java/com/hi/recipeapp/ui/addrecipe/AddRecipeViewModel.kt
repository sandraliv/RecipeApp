package com.hi.recipeapp.ui.addrecipe

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hi.recipeapp.classes.UserFullRecipe
import com.hi.recipeapp.services.RecipeService
import com.hi.recipeapp.classes.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddRecipeViewModel @Inject constructor(
    private val recipeService: RecipeService,
    private val sessionManager: SessionManager
) : ViewModel() {

    // Holds the user ID retrieved from session manager
    private val _userId = MutableLiveData<Int?>()
    val userId: LiveData<Int?> = _userId

    private val _newRecipeSuccess = MutableLiveData<Boolean>()
    val newRecipeSuccess: LiveData<Boolean> = _newRecipeSuccess

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    init {
        fetchUserId()
    }

    // Fetch the user ID from session manager when the ViewModel is created
    private fun fetchUserId() {
        viewModelScope.launch {
            val id = sessionManager.getUserId() // Directly get the userId as an Int
            _userId.postValue(id)
        }
    }

    fun uploadRecipe(recipe: UserFullRecipe) {
        viewModelScope.launch {
            val userIdValue = _userId.value
            if (userIdValue == null) {
                _errorMessage.postValue("User ID not found")
                return@launch
            }

            try {
                val success = recipeService.uploadUserRecipe(userIdValue, recipe)
                if (success) {
                    _newRecipeSuccess.postValue(true)
                } else {
                    _errorMessage.postValue("Error uploading recipe")
                }
            } catch (e: Exception) {
                _errorMessage.postValue("Exception: ${e.localizedMessage}")
            }
        }
    }



}

