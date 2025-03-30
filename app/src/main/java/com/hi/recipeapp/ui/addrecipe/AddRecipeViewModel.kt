package com.hi.recipeapp.ui.addrecipe

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hi.recipeapp.classes.FullRecipe
import com.hi.recipeapp.classes.SessionManager
import com.hi.recipeapp.classes.UserFullRecipe
import com.hi.recipeapp.services.RecipeService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddRecipeViewModel @Inject constructor(
    private val recipeService: RecipeService,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _newRecipeSuccess = MutableLiveData<Boolean>()
    val newRecipeSuccess: LiveData<Boolean> = _newRecipeSuccess

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    fun uploadRecipe(recipe: UserFullRecipe) {
        viewModelScope.launch {
            try {
                val userId = sessionManager.getUserId()
                val success = recipeService.uploadUserRecipe(userId, recipe)
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


