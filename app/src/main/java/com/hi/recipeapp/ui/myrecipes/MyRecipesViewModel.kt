package com.hi.recipeapp.ui.myrecipes

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.hi.recipeapp.classes.CalendarEntry
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
import org.threeten.bp.LocalDate
import javax.inject.Inject

@HiltViewModel
class MyRecipesViewModel @Inject constructor(
    private val userService: UserService,
    private val recipeService: RecipeService,
    private val sessionManager: SessionManager,
    private val recipeDao: RecipeDao
) : ViewModel() {
    // LiveData for mapped calendar recipes with full recipe cards (not just titles)
    private val _mappedCalendarRecipes =
        MutableLiveData<Pair<List<String>, Map<String, List<CalendarEntry>>>>()
    val mappedCalendarRecipes: LiveData<Pair<List<String>, Map<String, List<CalendarEntry>>>> =
        _mappedCalendarRecipes

    // LiveData for the list of recipe titles
    private val _mappedCalendarRecipesTitles = MutableLiveData<List<CalendarRecipeCard>>()
    val mappedCalendarRecipesTitles: LiveData<List<CalendarRecipeCard>> = _mappedCalendarRecipesTitles


    private val _favoriteRecipes = MutableLiveData<List<RecipeCard>?>()
    val favoriteRecipes: LiveData<List<RecipeCard>?> = _favoriteRecipes

    // Add this to the existing LiveData declarations in MyRecipesViewModel
    private val _calendarRecipes = MutableLiveData<List<CalendarEntry>?>()
    val calendarRecipes: LiveData<List<CalendarEntry>?> = _calendarRecipes


    private val _recipeDeleted = MutableLiveData<String>()
    val recipeDeleted: LiveData<String> = _recipeDeleted

    private val _userRecipes = MutableLiveData<List<UserRecipeCard>?>()
    val userRecipes: LiveData<List<UserRecipeCard>?> = _userRecipes

    private val _favoriteResult = MutableLiveData<Result<String>>()
    val favoriteResult: LiveData<Result<String>> get() = _favoriteResult

    private val _favoriteActionMessage = MutableLiveData<String?>()
    val favoriteActionMessage: LiveData<String?> get() = _favoriteActionMessage

    private val _removeRecipeResult = MutableLiveData<Result<String>>()
    val removeRecipeResult: LiveData<Result<String>> = _removeRecipeResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    /**
     * Fetches user recipes from the server and updates the LiveData accordingly.
     * @param page The page number to fetch (default is 0).
     * @param size The number of recipes per page (default is 10).
     */
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
    /**
     * Fetches favorite recipes from either the local database or server and updates the LiveData.
     */
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
    /**
     * Fetches the most recent favorite recipes from the server.
     */
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

    /**
     * Fetches favorite recipes from the server and updates the LiveData.
     */
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
                        _favoriteRecipes.value = favoriteRecipes

                        val recipeEntities = favoriteRecipes.map { it.toEntity() }
                        recipeDao.insertAll(recipeEntities)

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
    /**
     * Updates the favorite status of a recipe, either adding or removing it from the user's favorites.
     * @param recipe The recipe whose favorite status is to be updated.
     * @param isFavorited The new favorite status.
     */
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
    /**
     * Fetches and displays calendar recipes for the current user.
     */
    fun fetchAndDisplayCalendarRecipes() {
        val userId = sessionManager.getUserId()

        if (userId != -1) {
            _isLoading.value = true
            viewModelScope.launch {
                try {

                    // Fetch the user object (assuming we can fetch user by ID from the service)
                    val currentUser = userService.getUserProfileById(userId)  // Or use sessionManager if the user is in session

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

    /**
     * Processes a list of calendar entries and returns a list of recipe titles.
     * @param entries A list of calendar entries to process.
     * @return A list of recipe titles for each calendar entry.
     */
    fun processCalendarEntries(entries: List<CalendarEntry>): List<String> {
        return entries.map { entry ->
            // Safely access title from recipe or userRecipe
            entry.recipe?.title ?: entry.userRecipe?.title ?: "Unknown Recipe"
        }
    }

    /**
     * Maps calendar recipes to specific days and updates the LiveData.
     * It processes the calendar entries by date and creates a mapping of days to recipe entries.
     */
    private fun mapCalendarRecipesToDays(calendarRecipes: List<CalendarEntry>) {
        val days = mutableListOf<String>()
        val recipesByDay = mutableMapOf<String, MutableList<CalendarEntry>>() // Stores CalendarEntry for each day

        // Get the current date dynamically (current year and month)
        val currentDate = LocalDate.now()
        val currentYear = currentDate.year
        val currentMonth = currentDate.monthValue

        // Process each calendar entry
        calendarRecipes.forEach { calendarEntry ->
            val savedDate = calendarEntry.savedCalendarDate  // Format: "yyyy-MM-dd"

            // Extract year and month from the saved date for comparison
            val savedYear = savedDate.substring(0, 4).toInt()
            val savedMonth = savedDate.substring(5, 7).toInt()

            // Only include recipes that match the current year and month
            if (currentYear == savedYear && currentMonth == savedMonth) {
                // Add the full date to the list of days if it's not already included
                if (!days.contains(savedDate)) {
                    days.add(savedDate)
                }

                // Get or create the list for the current day
                val recipeCards = recipesByDay.getOrPut(savedDate) { mutableListOf() }

                // Add recipe and userRecipe to the list for this day
                calendarEntry.recipe?.let { recipe ->
                    recipeCards.add(calendarEntry)  // Add to the day's list of recipes
                }

                calendarEntry.userRecipe?.let { userRecipe ->
                    recipeCards.add(calendarEntry)  // Add user-created recipe if present
                }

                // Update the map with the day's recipes
                recipesByDay[savedDate] = recipeCards
            }
        }

        // Process the titles for all calendar entries
        val processedTitles = calendarRecipes.map { entry ->
            CalendarRecipeCard(
                id = entry.recipe?.id ?: entry.userRecipe?.id ?: 0,
                title = entry.recipe?.title ?: entry.userRecipe?.title ?: "Unknown Recipe",
                isUserRecipe = entry.userRecipe != null
            )
        }

        // Post the results to LiveData
        _mappedCalendarRecipes.value = Pair(
            days.toList(),  // List of days in "yyyy-MM-dd" format
            recipesByDay.toMap()  // Map of each date to its list of recipes
        )

        // Also post the titles to be used in the adapter
        _mappedCalendarRecipesTitles.value = processedTitles
        // Log the mapped data for debugging
        Log.d("CalendarMapping", "Mapped calendar recipes: $recipesByDay")
    }



    private fun RecipeCard.toEntity(): Recipe {
        return Recipe(
            id = id,
            title = title,
            description = description,
            imageUrls = imageUrls,
            averageRating = averageRating,
            instructions = instructions,
            ratingCount = ratingCount,
            tags = tags,
            isFavoritedByUser = isFavoritedByUser,
            ingredients = ingredients
        )
    }
    /**
     * Deletes a recipe by its ID and updates the user recipe list.
     * @param recipeId The ID of the recipe to delete.
     */
    fun deleteRecipe(recipeId: Int) {
        viewModelScope.launch {
            try {
                val result = recipeService.deleteUserRecipe(recipeId, sessionManager.getUserId())
                if(result.isSuccessful) {
                    _recipeDeleted.value = "Recipe deleted"
                    fetchUserRecipes()
                } else {
                    _recipeDeleted.value = "There was an error, try again"
                }
            } catch (e: Exception) {
                _recipeDeleted.value = "There was an error, try again"
            }

        }
    }
    /**
     * Removes a recipe from the user's calendar by recipe ID, user recipe ID, and the date.
     * @param recipeId The ID of the recipe to remove.
     * @param userRecipeId The ID of the user-created recipe to remove.
     * @param date The date of the recipe in the calendar.
     */
    fun removeRecipeFromCalendar(recipeId: Int?, userRecipeId: Int?, date: String) {
        // Set loading state to true while processing
        _isLoading.value = true

        // Get userId from sessionManager
        val userId = sessionManager.getUserId()

        // Only proceed if the user is logged in
        if (userId != -1) {
            viewModelScope.launch {
                try {
                    // Call the recipe service function to remove the recipe
                    val result = recipeService.removeRecipeFromCalendar(userId, recipeId, userRecipeId, date)

                    // Handle the result of the network call
                    result.onSuccess { message ->
                        _removeRecipeResult.value = Result.success(message)
                    }.onFailure { exception ->
                        _removeRecipeResult.value = Result.failure(exception)
                    }
                } catch (e: Exception) {
                    // Handle any exceptions (e.g., network error)
                    _removeRecipeResult.value = Result.failure(e)
                } finally {
                    // Reset the loading state after the operation is complete
                    _isLoading.value = false
                }
            }
        } else {
            // If user is not logged in, show error message
            _errorMessage.value = "User not logged in"
            _isLoading.value = false
        }
    }
}
