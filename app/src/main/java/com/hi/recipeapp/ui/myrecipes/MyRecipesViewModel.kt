package com.hi.recipeapp.ui.myrecipes

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hi.recipeapp.classes.Calendar
import com.hi.recipeapp.classes.CalendarRecipeCard
import com.hi.recipeapp.classes.FullRecipe
import com.hi.recipeapp.classes.RecipeCard
import com.hi.recipeapp.classes.SessionManager
import com.hi.recipeapp.services.RecipeService
import com.hi.recipeapp.services.UserService
import com.hi.recipeapp.classes.UserRecipeCard
import com.hi.recipeapp.data.local.Recipe
import com.hi.recipeapp.data.local.RecipeDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.forEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyRecipesViewModel @Inject constructor(
    private val userService: UserService,
    private val recipeService: RecipeService,
    private val sessionManager: SessionManager,
    private val recipeDao: RecipeDao
) : ViewModel() {
    // Change to store recipes as List<String> (titles of recipes)
    private val _mappedCalendarRecipes =
        MutableLiveData<Pair<List<String>, Map<String, List<String>>>>()
    val mappedCalendarRecipes: LiveData<Pair<List<String>, Map<String, List<String>>>> =
        _mappedCalendarRecipes


    private val _favoriteRecipes = MutableLiveData<List<RecipeCard>?>()
    val favoriteRecipes: LiveData<List<RecipeCard>?> = _favoriteRecipes

    // Add this to the existing LiveData declarations in MyRecipesViewModel
    private val _calendarRecipes = MutableLiveData<List<Calendar>?>()
    val calendarRecipes: LiveData<List<Calendar>?> = _calendarRecipes


    private val _userRecipes = MutableLiveData<List<UserRecipeCard>?>()
    val userRecipes: LiveData<List<UserRecipeCard>?> = _userRecipes

    private val _favoriteResult = MutableLiveData<Result<String>>()
    val favoriteResult: LiveData<Result<String>> get() = _favoriteResult

    private val _favoriteActionMessage = MutableLiveData<String?>()
    val favoriteActionMessage: LiveData<String?> get() = _favoriteActionMessage

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    // Function to load user recipes
    fun fetchUserRecipes(page: Int = 0, size: Int = 10) {
        viewModelScope.launch {
            val result = userService.getUserRecipes(page, size)
            result.onSuccess { recipes ->
                _userRecipes.postValue(recipes)  // Update the LiveData with fetched recipes
            }.onFailure { exception ->
                // Handle failure (e.g., show error message)
                Log.e("UserRecipes", "Failed to fetch recipes: ${exception.message}")
            }
        }
    }

    fun fetchFavoriteRecipes() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val localFavorites = recipeDao.getAll().first()
                if (localFavorites.isNotEmpty()) {
                    _favoriteRecipes.value = localFavorites
                    _isLoading.value = false
                    Log.d("RECIPES TESTING", "I AM TAKING FROM DATABASE HELLO")
                    getNewestFavouriteRecipes()
                } else {
                    fetchMyFavoriteRecipes()
                }
            } catch (e: Exception) {
                _errorMessage.value = "Database load failed: ${e.message}"
                Log.d("DATABASE ERROR", "${e.message}")
                _isLoading.value = false
            }
        }
    }

    private fun getNewestFavouriteRecipes() {
        viewModelScope.launch {
            try {
                fetchMyFavoriteRecipes() // just call the same suspend method
            } catch (e: Exception) {
                // If refresh fails, we still have the cached data
                Log.e("FavoriteVM", "Refresh failed: ${e.message}")
            }
        }
    }

    private fun fetchMyFavoriteRecipes() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val userId = sessionManager.getUserId()
                if (userId != -1) {

                    val result = userService.getUserFavorites(userId)
                    result.onSuccess { favoriteRecipes ->

                        Log.d("TEST", "EKKI VILLA Í VM")
                        _isLoading.value = false
                        favoriteRecipes.forEach { it.isFavoritedByUser = true }
                        favoriteRecipes.forEach { recipe ->
                            Log.d("Recipe", recipe.title)
                        }

                        _favoriteRecipes.value = favoriteRecipes

                        val recipeEntities = favoriteRecipes.map { it.toEntity() }
                        recipeDao.insertAll(recipeEntities)

                        Log.d("HALLOHEIMUR", "ÉG ER Í DABASE")

                    }
                    result.onFailure { error ->
                        _errorMessage.value =
                            error.localizedMessage ?: "Failed to fetch favorite recipes"
                        _isLoading.value = false
                    }
                } else {
                    _errorMessage.value = "User not logged in"
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _errorMessage.value = "Network request failed: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun updateFavoriteStatus(recipe: RecipeCard, isFavorited: Boolean) {
        viewModelScope.launch {
            val userId = sessionManager.getUserId()
            if (userId != -1) {
                sessionManager.setFavoritedStatus(userId, recipe.id, isFavorited)

                try {
                    if (isFavorited) {
                        recipeService.addRecipeToFavorites(recipe.id)
                        _favoriteActionMessage.value = "Recipe added to favorites"
                    } else {
                        recipeService.removeRecipeFromFavorites(recipe.id)
                        _favoriteActionMessage.value = "Recipe removed from favorites"
                    }

                    _favoriteRecipes.value = _favoriteRecipes.value?.map {
                        if (it.id == recipe.id) it.copy(isFavoritedByUser = isFavorited) else it
                    }
                } catch (e: Exception) {
                    _favoriteActionMessage.value = "Failed to update favorite status"
                }
            }
        }
    }

    fun fetchAndDisplayCalendarRecipes() {
        val userId = sessionManager.getUserId()

        if (userId != -1) {
            _isLoading.value = true
            viewModelScope.launch {
                try {
                    val result = userService.getUserSavedToCalendarRecipes(userId)
                    result.onSuccess { calendarRecipes ->
                        // Log the fetched recipes
                        Log.d("CalendarFetch", "Fetched calendar recipes: $calendarRecipes")
                        // Save the fetched recipes to session
                        sessionManager.saveCalendarRecipes(calendarRecipes)
                        // Update the LiveData with the raw calendar recipes
                        _calendarRecipes.postValue(calendarRecipes)
                        // Map the calendar recipes and post to mapped LiveData
                        mapCalendarRecipesToDays(calendarRecipes)
                    }.onFailure { exception ->
                        _errorMessage.postValue("Failed to fetch saved calendar recipes: ${exception.message}")
                        Log.e("CalendarFetch", "Error fetching calendar recipes: ${exception.message}")
                    }
                } catch (exception: Exception) {
                    _errorMessage.postValue("An error occurred: ${exception.message}")
                    Log.e("CalendarFetch", "Exception occurred: ${exception.message}")
                } finally {
                    _isLoading.value = false
                }
            }
        } else {
            _errorMessage.postValue("User is not logged in.")
            Log.e("CalendarFetch", "User not logged in")
            _isLoading.value = false
        }
    }


    private fun mapCalendarRecipesToDays(calendarRecipes: List<Calendar>) {
        val days = mutableListOf<String>()
        val recipesByDay = mutableMapOf<String, MutableList<String>>()  // Map from date (yyyy-MM-dd) -> list of recipe titles

        // Process each calendar entry
        calendarRecipes.forEach { calendarRecipe ->
            val fullDate = calendarRecipe.savedCalendarDate // full date string "yyyy-MM-dd"

            // Add the full date to the days list if it's not already added
            if (!days.contains(fullDate)) {
                days.add(fullDate)  // Adding full date in "yyyy-MM-dd" format
            }

            // Get the current list of recipe titles for the given day
            val recipeTitles = recipesByDay.getOrPut(fullDate) { mutableListOf() }

            // Add the recipe title (if present)
            calendarRecipe.recipe?.let { recipe ->
                recipeTitles.add(recipe.title)
            }

            // Add the user recipe title (if present)
            calendarRecipe.userRecipe?.let { userRecipe ->
                recipeTitles.add(userRecipe.title)
            }

            // Update the map with the current day's recipes
            recipesByDay[fullDate] = recipeTitles
        }

        // Post the results to LiveData
        _mappedCalendarRecipes.value = Pair(
            days.toList(),  // List of days (as full dates "yyyy-MM-dd")
            recipesByDay.toMap()  // Map of date -> list of recipe titles
        )

        // Log for debugging
        Log.d("CalendarMapping", "Mapped calendar recipes: $recipesByDay")
    }



    private fun RecipeCard.toEntity(): Recipe {
        return Recipe(
            id = id,
            title = title,
            description = description,
            imageUrls = imageUrls,
            averageRating = averageRating,
            ratingCount = ratingCount,
            tags = tags,
            isFavoritedByUser = isFavoritedByUser
        )
    }

}
