package com.hi.recipeapp.ui.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hi.recipeapp.classes.FullRecipe
import com.hi.recipeapp.classes.RecipeCard
import com.hi.recipeapp.classes.SessionManager
import com.hi.recipeapp.classes.SortType
import com.hi.recipeapp.classes.UserFullRecipe
import com.hi.recipeapp.services.RecipeService
import com.hi.recipeapp.services.UserService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import retrofit2.Response
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val recipeService: RecipeService,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _recipes = MutableLiveData<List<RecipeCard>?>()
    val recipes: LiveData<List<RecipeCard>?> get() = _recipes

    private val _isLoadingMore = MutableLiveData<Boolean>(false)
    val isLoadingMore: LiveData<Boolean> get() = _isLoadingMore

    private val _favoriteActionMessage = MutableLiveData<String?>()
    val favoriteActionMessage: LiveData<String?> get() = _favoriteActionMessage

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    private val _isAdmin = MutableLiveData<Boolean?>()
    val isAdmin: LiveData<Boolean?> get() = _isAdmin

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _noMoreRecipes = MutableLiveData<Boolean>(false)
    val noMoreRecipes: LiveData<Boolean> = _noMoreRecipes

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    private val _editableRecipe = MutableLiveData<FullRecipe>()
    val editableRecipe: LiveData<FullRecipe> = _editableRecipe

    private var pageNumber = 0
    private val pageSize = 20


    init {
        fetchRecipesSortedBy(sortType = SortType.RATING)
        checkIfAdmin()
    }

    private fun checkIfAdmin(){
        _isAdmin.value = sessionManager.isAdmin();
    }

    /**
     * A method for fetching recipes based on sort (date added or rating)
     */
    fun fetchRecipesSortedBy(sortType: SortType) {
        val userId = sessionManager.getUserId()
        _isLoading.value = true
        _recipes.value = null
        val sortString = sortType.name.lowercase()

        // Fetch recipes from the backend
        recipeService.fetchRecipes(
            page = pageNumber,
            size = pageSize,
            sort = sortString
        ) { recipes, error ->
            if (error != null) {
                _errorMessage.value = error
                _isLoading.value = false
            } else {

                if (userId != -1) {
                    recipes?.forEach { recipe ->
                        recipe.isFavoritedByUser = sessionManager.getFavoritedStatus(userId, recipe.id)
                    }
                } else {
                    recipes?.forEach { recipe ->
                        recipe.isFavoritedByUser = false
                    }
                }
                _recipes.value = recipes
                _isLoading.value = false
            }
        }
    }


    /**
     * A method for updating favourite status when a user clicks the "heart" on a RecipeCard
     * @param recipe A RecipeCard object
     * @param isFavorited: A Boolean value representing if the recipe is the users favourite or not.
     */
    fun updateFavoriteStatus(recipe: RecipeCard, isFavorited: Boolean) {
        viewModelScope.launch {
            val userId = sessionManager.getUserId()
            if (userId != -1) {
                try {
                    // Step 1: Update the favorited status in SessionManager
                    sessionManager.setFavoritedStatus(userId, recipe.id, isFavorited)

                    // Step 2: Update the backend (add or remove from favorites)
                    if (isFavorited) {
                        recipeService.addRecipeToFavorites(recipe.id)
                        _favoriteActionMessage.value = "Recipe added to favorites"
                    } else {
                        recipeService.removeRecipeFromFavorites(recipe.id)
                        sessionManager.removeRecipeFromFavourites(recipe.id)
                        _favoriteActionMessage.value = "Recipe removed from favorites"
                    }

                    // Step 3: Update the local list of recipes with the new favorited status
                    _recipes.value = _recipes.value?.map {
                        if (it.id == recipe.id) it.copy(isFavoritedByUser = isFavorited) else it
                    }

                    // Step 4: Update the favorite recipe IDs in SessionManager
                    val currentFavorites = sessionManager.getFavoriteRecipeIds().toMutableSet()
                    if (isFavorited) {
                        currentFavorites.add(recipe.id)  // Add to favorites set
                    } else {
                        currentFavorites.remove(recipe.id)  // Remove from favorites set
                    }

                    // Save the updated favorite recipe IDs to SharedPreferences
                    sessionManager.saveFavoriteRecipeIds(currentFavorites)

                } catch (e: Exception) {
                    _favoriteActionMessage.value = "Failed to update favorite status"
                    Log.e("HomeViewModel", "Error updating favorite status: ${e.message}")
                }
            }
        }
    }

    /**
     * Loads more recipes when recyclerView needs more recipes.
     * @param sortType A SortType object which holds the sorting of recipes being fetched
     */
    fun loadMoreRecipes(sortType: SortType) {
        if (_noMoreRecipes.value == true || _isLoadingMore.value == true) {
            return
        }

        pageNumber++
        _isLoadingMore.value = true
        _isLoading.value = false
        val sortString = sortType.name.lowercase()
        recipeService.fetchRecipes(
            page = pageNumber,
            size = pageSize,
            sort = sortString
        ) { newRecipes, error ->

            if (error != null) {
                _errorMessage.value = error
                _isLoadingMore.value = false
            } else {
                val userId = sessionManager.getUserId()

                // Check if there are any new recipes
                if (!newRecipes.isNullOrEmpty()) {
                    // Use the sessionManager to mark the new recipes based on the stored favorited status
                    newRecipes.forEach { recipe ->
                        // Check if the user is logged in and then use sessionManager to get the favorited status
                        if (userId != -1) {
                            val isFavorited = sessionManager.getFavoritedStatus(userId, recipe.id)
                            recipe.isFavoritedByUser = isFavorited
                        } else {
                            recipe.isFavoritedByUser = false
                        }
                    }

                    val updatedRecipes = _recipes.value?.toMutableList() ?: mutableListOf()
                    updatedRecipes.addAll(newRecipes)
                    _recipes.value = updatedRecipes
                }

                if (newRecipes.isNullOrEmpty()) {
                    _noMoreRecipes.value = true
                    _isLoadingMore.value = false
                    return@fetchRecipes
                }

                _isLoadingMore.value = false
            }
        }
    }

    /**
     *
     * Calls recipe service to handle networking for recipe posting.
     * @param recipeId: A integer representing the recipe unique id.
     */
    fun deleteRecipe(recipeId: Int) {
        viewModelScope.launch {
            val result = recipeService.deleteRecipe(recipeId)
            result
                .onSuccess {
                    fetchRecipesSortedBy(SortType.RATING)
                    Log.d("RECIPES FAIL", "NOT FAIL")
                }
                .onFailure {
                    Log.d("RECIPES FAIL", "FAIL")
                    _error.value = "Failed to delete user: ${it.message}"
                }
        }
    }

    /**
     * Fetch recipes when sort variable changes
     */
    fun updateSortType(newSortType: SortType) {
        if (_recipes.value != null) {
            fetchRecipesSortedBy(newSortType)
        } else {
        }
    }

    /**
     * Recipe Service is called to fetch the recipe that is gonna be edited
     * @param recipeId An integer representing the recipes unique id.
     */
    fun editRecipe(recipeId: Int) {
        recipeService.fetchRecipeById(recipeId) { result, _ ->
            result?.let {
                _editableRecipe.postValue(it)
            }
        }
    }

    suspend fun patchRecipe(updatedRecipe: UserFullRecipe): Response<String> {
        return recipeService.patchRecipe(updatedRecipe)
    }


}

