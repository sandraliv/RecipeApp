package com.hi.recipeapp.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hi.recipeapp.classes.RecipeCard
import com.hi.recipeapp.services.RecipeService
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val recipeService: RecipeService
) : ViewModel() {
    // LiveData to hold the list of recipes
    private val _recipes = MutableLiveData<List<RecipeCard>?>()
    val recipes: MutableLiveData<List<RecipeCard>?> get() = _recipes

    // LiveData for error messages
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: MutableLiveData<String?> get() = _errorMessage

    // LiveData to track loading state
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    init {
        fetchRecipes();
    }

    fun fetchRecipes() {
        _isLoading.value = true
        recipeService.fetchRecipes { recipes, error ->
            _isLoading.value = false
            if (error != null) {
                _errorMessage.value = error
            } else {
                _recipes.value = recipes
            }
        }
    }
}
