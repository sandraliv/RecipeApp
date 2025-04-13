package com.hi.recipeapp.ui.fullrecipe

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hi.recipeapp.classes.UserFullRecipe
import com.hi.recipeapp.data.local.Recipe
import com.hi.recipeapp.data.local.RecipeDao
import com.hi.recipeapp.services.RecipeService
import com.hi.recipeapp.services.UserService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserFullRecipeViewModel @Inject constructor(
    private val userService: UserService,
    private val recipeService: RecipeService,
    private val recipeDao: RecipeDao
) : ViewModel() {

    private val _userrecipe = MutableLiveData<UserFullRecipe?>()
    val userrecipe: LiveData<UserFullRecipe?> = _userrecipe

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _calendarSaveStatus = MutableLiveData<String>()
    val calendarSaveStatus: LiveData<String> get() = _calendarSaveStatus


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

    fun saveRecipeToCalendar(userId: Int, recipeId: Int, date: String) {
        viewModelScope.launch {

            val result = recipeService.saveRecipeToCalendar(userId, recipeId, date)
            result.onSuccess {
                _calendarSaveStatus.value = "Recipe saved to calendar on $date"
            }.onFailure {
                _calendarSaveStatus.value = "Error saving recipe: ${it.message}"
            }
        }
    }

}
