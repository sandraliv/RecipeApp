package com.hi.recipeapp.ui.addrecipe

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hi.recipeapp.classes.FullRecipe
import com.hi.recipeapp.services.RecipeService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddRecipeViewModel @Inject constructor(
    private val recipeService: RecipeService
) : ViewModel() {

    private val _newRecipeSuccess = MutableLiveData<Boolean>()
    val newRecipeSuccess: LiveData<Boolean> = _newRecipeSuccess

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    fun createRecipe(recipe: FullRecipe) {
        viewModelScope.launch {
            try {
                val success = recipeService.createRecipe(recipe)
                if (success) {
                    _newRecipeSuccess.postValue(true)
                } else {
                    _errorMessage.postValue("Error adding recipe")
                }
            } catch (e: Exception) {
                _errorMessage.postValue("Exception: ${e.localizedMessage}")
            }
        }
    }
}

