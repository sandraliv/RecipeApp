package com.hi.recipeapp.ui.myrecipes

import CalendarAdapter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.hi.recipeapp.R
import com.hi.recipeapp.classes.CalendarEntry
import com.hi.recipeapp.classes.CalendarRecipeCard
import com.hi.recipeapp.classes.SessionManager
import com.hi.recipeapp.databinding.FragmentMyRecipesBinding
import com.hi.recipeapp.ui.home.RecipeAdapter
import dagger.hilt.android.AndroidEntryPoint
import org.threeten.bp.LocalDate
import org.threeten.bp.Month
import org.threeten.bp.YearMonth
import org.threeten.bp.format.DateTimeFormatter
import java.util.Locale
import android.content.res.Configuration

import javax.inject.Inject

@AndroidEntryPoint
class MyRecipesFragment : Fragment() {

    private var _binding: FragmentMyRecipesBinding? = null
    private val binding get() = _binding!!


    private val myRecipesViewModel: MyRecipesViewModel by viewModels()
    private lateinit var recipeAdapter: RecipeAdapter
    private lateinit var userRecipeAdapter: UserRecipeAdapter
    private lateinit var calendarAdapter: CalendarAdapter
    private lateinit var calendarRecipeCardAdapter: CalendarRecipeCardAdapter

    @Inject
    lateinit var sessionManager: SessionManager

    private var hasInitializedToday = false
    private val starSize = 30
    private val spaceBetweenStars = 3
    private val gridColumnCount = 2
    private val calendarGridColumnCount = 7
    private var isCalendarCategoryActive = false
    private var selectedDay: String = LocalDate.now().dayOfMonth.toString().padStart(2, '0')
    private var currentMonth: Int = LocalDate.now().monthValue
    private var currentYear: Int = LocalDate.now().year
    private var formattedSelectedDate: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMyRecipesBinding.inflate(inflater, container, false)

        // Initialize the adapters
        recipeAdapter = RecipeAdapter(
            onClick = { recipe ->
                val recipeId = recipe.id
                val action =
                    MyRecipesFragmentDirections.actionMyRecipesFragmentToFullRecipeFragment(recipeId)
                findNavController().navigate(action)
            },
            onFavoriteClick = { recipe, isFavorited ->
                myRecipesViewModel.updateFavoriteStatus(recipe, isFavorited)
            },
            starSize = starSize,
            spaceBetweenStars = spaceBetweenStars,
            isAdmin = false,
            onDeleteClick = {},
            onEditClick = {}
        )

        userRecipeAdapter = UserRecipeAdapter(
            onClick = { userRecipe ->
                val recipeId = userRecipe.id
                val action =
                    MyRecipesFragmentDirections.actionMyRecipesFragmentToUserFullRecipeFragment(
                        recipeId
                    )
                findNavController().navigate(action)
            },
            onDeleteClick ={recipeId -> myRecipesViewModel.deleteRecipe(recipeId)},
        )

        calendarAdapter = CalendarAdapter(
            weekHeaders = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"),
            days = listOf(), // Empty initially, we'll update this in updateCalendar()
            recipesByDay = mutableMapOf(),  // Empty initially
            currentMonth = currentMonth,
            currentYear = currentYear,
            onDayClicked = { selectedDay, recipes ->  // Correct parameter name
                // Handle the day click
                updateRecipeListForDay(selectedDay)
            }
        )

        calendarRecipeCardAdapter = CalendarRecipeCardAdapter(
            onRecipeClick = { clickedRecipe ->
                val recipeId = clickedRecipe.id
                val action = if (clickedRecipe.isUserRecipe) {
                    MyRecipesFragmentDirections.actionMyRecipesFragmentToUserFullRecipeFragment(recipeId)
                } else {
                    MyRecipesFragmentDirections.actionMyRecipesFragmentToFullRecipeFragment(recipeId)
                }
                findNavController().navigate(action)
            },
            onRemoveFromCalendarClick = onRemoveFromCalendarClick@{ clickedRecipe ->
                // Log when the remove button is clicked
                Log.d("CalendarRecipeCardAdapter", "Remove button clicked for recipe: ${clickedRecipe.id}")

                // Assuming you're passing the data to your ViewModel or API request function
                val recipeId = clickedRecipe.id
                val userRecipeId = clickedRecipe.id  // Same ID, but we'll decide which to use based on `isUserRecipe`

                // Check if it's a user recipe or a regular recipe
                val isUserRecipe = clickedRecipe.isUserRecipe
                val userId = sessionManager.getUserId()  // Get user ID

                // Find the calendar entry associated with this recipe
                val calendarEntry = findCalendarEntryForRecipe(clickedRecipe)
                val date = calendarEntry?.savedCalendarDate

                if (date.isNullOrEmpty()) {
                    Log.e("MyRecipesFragment", "Recipe date is missing!")
                    return@onRemoveFromCalendarClick
                }

                // Determine which ID to use for removal based on `isUserRecipe`
                val requestId = if (isUserRecipe) userRecipeId else recipeId

                // Make the API call to remove the recipe from the calendar using the ViewModel
                myRecipesViewModel.removeRecipeFromCalendar(userId, requestId, date)

                // Log that removal has been initiated
                Log.d("CalendarRecipeCardAdapter", "Recipe removal initiated for Recipe ID: $recipeId on date: $date")

                // Update the adapter list
                val updatedList = calendarRecipeCardAdapter.currentList.toMutableList()
                updatedList.remove(clickedRecipe)
                calendarRecipeCardAdapter.submitList(updatedList)

                // Log the updated list size
                Log.d("CalendarRecipeCardAdapter", "Updated list size after removal: ${updatedList.size}")

                // Format the date for display in Snackbar
                val formattedDate = try {
                    val dateParsed = LocalDate.parse(date)  // Parse to LocalDate
                    CalendarUtils.formattedDate(dateParsed)  // Use formattedDate function for consistency
                } catch (e: Exception) {
                    Log.e("CalendarRecipeCardAdapter", "Error formatting date: $e")
                    date  // Fallback to original date if parsing fails
                }

                // Show a Snackbar with the success message
                Snackbar.make(
                    binding.root,
                    "Recipe removed from $formattedDate",
                    Snackbar.LENGTH_SHORT
                ).show()

                // Check if the list is empty and update UI accordingly
                if (updatedList.isEmpty()) {
                    Log.d("CalendarRecipeCardAdapter", "No recipes left for this day after removal.")
                    binding.recipeListTextView.text = "No recipes for this day"
                    binding.recipeListTextView.visibility = View.VISIBLE
                    binding.calendarRecipeRecyclerView.visibility = View.GONE
                } else {
                    Log.d("CalendarRecipeCardAdapter", "There are still recipes left after removal.")
                }

                // Forcefully re-fetch calendar data from the database
                myRecipesViewModel.fetchAndDisplayCalendarRecipes()

                // Ensure the calendar adapter is updated with the new data
                myRecipesViewModel.mappedCalendarRecipes.observe(viewLifecycleOwner) { (daysList, recipesByDay) ->
                    val recipesByDayMap = mutableMapOf<String, MutableList<CalendarEntry>>()

                    daysList.forEach { day ->
                        val recipes = recipesByDay[day] ?: listOf()
                        if (recipes.isNotEmpty()) {
                            val calendarEntries = recipes.mapNotNull { calendarEntry ->
                                val calendarRecipeCard = calendarEntry.recipe ?: calendarEntry.userRecipe
                                calendarRecipeCard?.let {
                                    CalendarEntry(
                                        id = it.id,
                                        userId = sessionManager.getUserId(),
                                        recipe = it,
                                        userRecipe = it,
                                        savedCalendarDate = "${currentYear}-${currentMonth.toString().padStart(2, '0')}-${day}"
                                    )
                                }
                            }
                            recipesByDayMap[day] = calendarEntries.toMutableList()
                        }
                    }
                    val fullCalendarRecipes = mutableMapOf<String, List<CalendarEntry>>()

                    daysList.forEach { day ->
                        val recipesForDay = recipesByDayMap[day] ?: listOf()
                        fullCalendarRecipes[day] = recipesForDay
                    }

                    // Update the calendar adapter's data
                    calendarAdapter.updateCalendarData(daysList, fullCalendarRecipes)
                }
            }
        )

        setupRecyclerView()
        setupButtonListeners()
        observeViewModel()

        setInitialState()

        myRecipesViewModel.favoriteActionMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                // Show the message using Snackbar
                Snackbar.make(binding.root, it, Snackbar.LENGTH_SHORT).show()
            }
        }

        myRecipesViewModel.recipeDeleted.observe(viewLifecycleOwner) { message ->
            message?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_SHORT).show()
            }
        }

        return binding.root
    }


    /**
     * Searches for the calendar entry associated with the given recipe.
     *
     * @param recipe The recipe for which the calendar entry is being searched.
     * @return The corresponding CalendarEntry if found, null otherwise.
     */
    private fun findCalendarEntryForRecipe(recipe: CalendarRecipeCard): CalendarEntry? {
        Log.d("CalendarRecipeCardAdapter", "Searching for calendar entry for recipe: ${recipe.id}")
        // Assuming you have a method in the ViewModel that fetches the calendar entries
        val calendarEntries = myRecipesViewModel.mappedCalendarRecipes.value?.second
        return calendarEntries?.values?.flatten()?.find { it.recipe?.id == recipe.id || it.userRecipe?.id == recipe.id }
    }

    /**
     * Sets up all RecyclerViews used in this fragment by initializing them with their respective
     * layout managers and adapters.
     */
    private fun setupRecyclerView() {

        // Set up RecyclerView for the calendar recipe list
        binding.calendarRecipeRecyclerView.apply {
            layoutManager =
                LinearLayoutManager(context)
            adapter =
                calendarRecipeCardAdapter
        }

        // Ensure RecyclerView uses this adapter
        binding.calendarRecyclerView.apply {
            layoutManager = GridLayoutManager(
                context,
                calendarGridColumnCount
            )
            adapter = calendarAdapter
        }

        // Set up GridLayoutManager for other sections (e.g., favorite recipes, user recipes)
        binding.favoriteRecipeRecyclerView.apply {
            layoutManager = GridLayoutManager(context, gridColumnCount)
            adapter = recipeAdapter
        }

        binding.userRecipesRecyclerView.apply {
            layoutManager = GridLayoutManager(context, gridColumnCount)
            adapter = userRecipeAdapter
        }
    }


    /**
     * Sets up click listeners for the category buttons (Favorites, My Recipes, Calendar)
     * and the navigation buttons for changing months in the calendar.
     */
    private fun setupButtonListeners() {
        binding.favoritesButton.setOnClickListener {
            setActiveButton(binding.favoritesButton)
            toggleVisibility("favorites")
            myRecipesViewModel.fetchFavoriteRecipes()
        }

        binding.myRecipesButton.setOnClickListener {
            setActiveButton(binding.myRecipesButton)
            toggleVisibility("user")
            myRecipesViewModel.fetchUserRecipes()
        }

        binding.calendarButton.setOnClickListener {
            setActiveButton(binding.calendarButton)
            toggleVisibility("calendar")
            isCalendarCategoryActive = true
            myRecipesViewModel.fetchAndDisplayCalendarRecipes()
            initializeCalendarForCurrentMonth()
        }

        binding.previousMonthButton.setOnClickListener {
            currentMonth = if (currentMonth == 1) {
                currentMonth = 12
                currentYear -= 1
                12
            } else {
                currentMonth - 1
            }
            updateCalendarWithCurrentMonth()
        }

        binding.nextMonthButton.setOnClickListener {
            currentMonth = if (currentMonth == 12) {
                currentMonth = 1
                currentYear += 1
                1
            } else {
                currentMonth + 1
            }
            updateCalendarWithCurrentMonth()
        }

    }
    /**
     * Toggles the visibility of sections of the fragment based on the selected category.
     * This method will hide all sections and only show the one that matches the given category.
     *
     * @param category The category to be displayed. Can be "favorites", "user", or "calendar".
     */
    private fun toggleVisibility(category: String) {
        when (category) {
            "favorites" -> {
                binding.favoriteRecipeRecyclerView.visibility = View.VISIBLE
                binding.userRecipesRecyclerView.visibility = View.GONE
                binding.calendarRecyclerView.visibility = View.GONE
                binding.calendarRecipeRecyclerView.visibility = View.GONE
                binding.calendarButtonsContainer.visibility = View.GONE
                if (recipeAdapter.itemCount == 0) {  // Check if there are no favorite recipes
                    setNoRecipesMessage("favorites")
                } else {
                    binding.recipeListTextView.visibility = View.GONE  // Hide message if data exists
                }
            }
            "user" -> {
                binding.favoriteRecipeRecyclerView.visibility = View.GONE
                binding.userRecipesRecyclerView.visibility = View.VISIBLE
                binding.calendarRecyclerView.visibility = View.GONE
                binding.calendarRecipeRecyclerView.visibility = View.GONE
                binding.calendarButtonsContainer.visibility = View.GONE
                if (userRecipeAdapter.itemCount == 0) {  // Check if there are no user recipes
                    setNoRecipesMessage("user")
                } else {
                    binding.recipeListTextView.visibility = View.GONE  // Hide message if data exists
                }
            }
            "calendar" -> {
                binding.favoriteRecipeRecyclerView.visibility = View.GONE
                binding.userRecipesRecyclerView.visibility = View.GONE
                binding.calendarButtonsContainer.visibility = View.VISIBLE
                binding.calendarRecyclerView.visibility = View.VISIBLE
                binding.calendarRecipeRecyclerView.visibility = View.VISIBLE
                if (calendarRecipeCardAdapter.itemCount == 0) {  // Check if there are no calendar recipes
                    setNoRecipesMessage("calendar")
                } else {
                    binding.recipeListTextView.visibility = View.GONE  // Hide message if data exists
                }
            }
        }
    }
    /**
     * Sets the message to display when there are no recipes for the specified category.
     *
     * @param category The category for which no recipes were found. It can be one of the following:
     *                 "calendar", "favorite", or "user".
     */
    private fun setNoRecipesMessage(category: String) {
        val recipeListTextView = binding.recipeListTextView

        when (category) {
            "calendar" -> {
                recipeListTextView.text = "No recipes for ${formattedSelectedDate}"
            }
            "favorite" -> {
                recipeListTextView.text = "No favorite recipes found"
            }
            "user" -> {
                recipeListTextView.text = "No user recipes found"
            }
        }
        recipeListTextView.visibility = View.VISIBLE
    }
    /**
     * Sets the initial state of the view when the user first loads the screen.
     * It prepares the UI elements, shows the favorite recipe list, and fetches
     * the favorite recipes.
     */
    private fun setInitialState() {
        val today = LocalDate.now().dayOfMonth.toString().padStart(2, '0')

        binding.favoriteRecipeRecyclerView.visibility = View.VISIBLE
        binding.userRecipesRecyclerView.visibility = View.GONE
        binding.calendarRecyclerView.visibility = View.GONE
        binding.calendarRecipeRecyclerView.visibility = View.GONE
        binding.calendarButtonsContainer.visibility = View.GONE

        setActiveButton(binding.favoritesButton)
        myRecipesViewModel.fetchFavoriteRecipes()
        selectedDay = today
    }

    /**
     * Updates the recipe list for the selected day. If there are recipes for the day,
     * it displays them; otherwise, it shows a message indicating there are no recipes.
     *
     * @param day The selected day for which to update the recipe list. The day should
     *            be a two-digit string (e.g., "01", "02").
     */
    private fun updateRecipeListForDay(day: String) {
        val recipeRecyclerView = binding.calendarRecipeRecyclerView
        val recipeListTextView = binding.recipeListTextView
        val formattedDay = day.padStart(2, '0')

        // Convert formattedDay to LocalDate and format it to a human-readable string (e.g., "01 January")
        val dayOfMonth = LocalDate.of(currentYear, currentMonth, formattedDay.toInt())
        // Format it for display as "01 January 2021"
        formattedSelectedDate = dayOfMonth.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"))


        // Format the selected day to include the full date (Year-Month-Day)
        val selectedDate = LocalDate.of(currentYear, currentMonth, formattedDay.toInt()).toString()  // "2025-04-01"

        // Get the recipes for the selected full date from your ViewModel (CalendarEntry list)
        val calendarEntriesForToday: List<CalendarEntry> =
            myRecipesViewModel.mappedCalendarRecipes.value?.second?.get(selectedDate) ?: emptyList()



        // Convert CalendarEntry to CalendarRecipeCard
        val recipesForToday: List<CalendarRecipeCard> =
            calendarEntriesForToday.mapNotNull { calendarEntry ->
                calendarEntry.recipe ?: calendarEntry.userRecipe
            }


        // Now submit the new list to the adapter
        val newRecipeList: List<CalendarRecipeCard> = recipesForToday
        calendarRecipeCardAdapter.submitList(newRecipeList)

        if (calendarEntriesForToday.isEmpty()) {
            recipeRecyclerView.visibility = View.GONE
            setNoRecipesMessage("calendar")  // Call this for calendar
        } else {
            recipeRecyclerView.visibility = View.VISIBLE
            recipeListTextView.visibility = View.GONE
        }
    }
    /**
     * Updates the calendar view with the current month and year. The calendar is populated with
     * the appropriate days and their corresponding recipes for the selected month.
     */
    private fun updateCalendarWithCurrentMonth() {
        val monthName =
            Month.of(currentMonth).name.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
        val year = currentYear.toString()

        binding.monthTextView.text = "$monthName $year"
        updateCalendar()
        myRecipesViewModel.fetchAndDisplayCalendarRecipes()
    }
    /**
     * Sets the active button style and updates its visual appearance based on night mode
     * and the button's selected state.
     *
     * @param button The button to set as active (selected). It can be either the
     *               "favoritesButton", "myRecipesButton", or "calendarButton".
     */
    private fun setActiveButton(button: Button) {
        // Check if the app is in night mode
        val isNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

        // Select the correct ColorStateList based on night mode
        val textColorSelector = if (isNightMode) {
            ContextCompat.getColorStateList(requireContext(), R.color.button_text_selector_night)
        } else {
            ContextCompat.getColorStateList(requireContext(), R.color.button_text_selector)
        }

        val backgroundTintSelector = if (isNightMode) {
            ContextCompat.getColorStateList(requireContext(), R.color.button_background_selector_night)
        } else {
            ContextCompat.getColorStateList(requireContext(), R.color.button_background_selector)
        }

        // Apply background selector just once
        binding.favoritesButton.backgroundTintList = backgroundTintSelector
        binding.myRecipesButton.backgroundTintList = backgroundTintSelector
        binding.calendarButton.backgroundTintList = backgroundTintSelector

        // Update selection states
        binding.favoritesButton.isSelected = button == binding.favoritesButton
        binding.myRecipesButton.isSelected = button == binding.myRecipesButton
        binding.calendarButton.isSelected = button == binding.calendarButton


        // Apply text color based on the correct selector
        binding.favoritesButton.setTextColor(textColorSelector)
        binding.myRecipesButton.setTextColor(textColorSelector)
        binding.calendarButton.setTextColor(textColorSelector)

        // Add elevation if button is selected
        val selectedElevation = 8f // You can adjust this value as needed

        // Reset elevation for all buttons
        binding.favoritesButton.elevation = 0f
        binding.myRecipesButton.elevation = 0f
        binding.calendarButton.elevation = 0f

        // Add elevation to the selected button
        if (button == binding.favoritesButton) {
            binding.favoritesButton.elevation = selectedElevation
        } else if (button == binding.myRecipesButton) {
            binding.myRecipesButton.elevation = selectedElevation
        } else if (button == binding.calendarButton) {
            binding.calendarButton.elevation = selectedElevation
        }
    }


    /**
     * Updates the calendar grid with the appropriate days for the current month.
     * It calculates the number of days in the month and ensures the grid is filled
     * correctly by adding empty cells as needed to complete the 7x5 grid.
     */
    private fun updateCalendar() {
        val firstDayOfMonth = YearMonth.of(currentYear, currentMonth).atDay(1).dayOfWeek.value
        val daysInMonth = YearMonth.of(currentYear, currentMonth).lengthOfMonth()

        val days = mutableListOf<String>()
        for (i in 1 until firstDayOfMonth) {
            days.add("")  // Empty cells before the first day
        }

        for (i in 1..daysInMonth) {
            days.add(i.toString())  // Add all the days of the month
        }

        val totalCells = days.size
        val emptyCells = 7 - (totalCells % 7)
        if (emptyCells != 7) {
            for (i in 1..emptyCells) {
                days.add("")  // Add empty cells to complete the grid
            }
        }

        // Now update the adapter with the new days for the selected month
        myRecipesViewModel.mappedCalendarRecipes.observe(viewLifecycleOwner) { (daysList, recipesByDay) ->
            val recipesByDayMap = mutableMapOf<String, MutableList<CalendarEntry>>()

            daysList.forEach { day ->
                val recipes = recipesByDay[day] ?: listOf()
                if (recipes.isNotEmpty()) {
                    val calendarEntries = recipes.mapNotNull { calendarEntry ->
                        val calendarRecipeCard = calendarEntry.recipe ?: calendarEntry.userRecipe
                        calendarRecipeCard?.let {
                            CalendarEntry(
                                id = it.id,
                                userId = sessionManager.getUserId(),
                                recipe = it,
                                userRecipe = it,
                                savedCalendarDate = "${currentYear}-${currentMonth.toString().padStart(2, '0')}-${day}"
                            )
                        }
                    }
                    recipesByDayMap[day] = calendarEntries.toMutableList()
                }
            }
            val fullCalendarRecipes = mutableMapOf<String, List<CalendarEntry>>()

            daysList.forEach { day ->
                val recipesForDay = recipesByDayMap[day] ?: listOf()
                fullCalendarRecipes[day] = recipesForDay
            }
            calendarAdapter.updateCalendarData(daysList, fullCalendarRecipes)
        }
    }
    /**
     * Initializes the calendar by fetching the data for the current month, including
     * all the recipes scheduled for each day. The calendar will be displayed in a grid layout.
     */
    private fun initializeCalendarForCurrentMonth() {
        // Get days of the current month and format them
        val newDays = CalendarUtils.daysInMonthArray(LocalDate.of(currentYear, currentMonth, 1))
            .map { it?.dayOfMonth?.toString()?.padStart(2, '0') ?: " " }

        myRecipesViewModel.calendarRecipes.observe(viewLifecycleOwner) { calendarRecipes ->
            val recipesByDay = mutableMapOf<String, MutableList<CalendarEntry>>()

            calendarRecipes?.forEach { calendar ->
                val fullDate = calendar.savedCalendarDate

                val calendarEntries = mutableListOf<CalendarEntry>()

                calendar.recipe?.let { recipe ->
                    calendarEntries.add(calendar)
                }

                calendar.userRecipe?.let { userRecipe ->
                    calendarEntries.add(calendar)
                }

                // Store recipes by full date key (the fullDate here is the "yyyy-MM-dd" format)
                recipesByDay[fullDate] = calendarEntries
            }

            // Ensure we match the newDays with the recipes (fullDate format: YYYY-MM-DD)
            val updatedRecipesByDay = newDays.associateWith { day ->
                val fullDate = "${currentYear}-${currentMonth.toString().padStart(2, '0')}-$day"  // Example: "2025-04-01"

                // If the day is null or doesn't have a recipe, return an empty list
                if (day == " " || recipesByDay[fullDate].isNullOrEmpty()) {
                    emptyList()
                } else {
                    recipesByDay[fullDate] ?: emptyList()
                }
            }

            calendarAdapter.updateCalendarData(newDays, updatedRecipesByDay)
        }

        updateCalendarWithCurrentMonth()
    }
    /**
     * Observes the LiveData from the ViewModel and updates the UI accordingly.
     * This includes observing the favorite recipes, user recipes, loading state,
     * error messages, and calendar recipes. It updates the respective views based
     * on the observed data.
     */
    private fun observeViewModel() {
        myRecipesViewModel.favoriteRecipes.observe(viewLifecycleOwner) { recipes ->
            if (recipes.isNullOrEmpty()) {
                setNoRecipesMessage("favorites")
                Toast.makeText(requireContext(), "No recipes found.", Toast.LENGTH_SHORT).show()
                binding.favoriteRecipeRecyclerView.visibility = View.GONE
            } else {
                recipeAdapter.submitList(recipes)
                binding.recipeListTextView.visibility = View.GONE
                binding.favoriteRecipeRecyclerView.visibility = View.VISIBLE
            }
        }

        // Observing user recipes
        myRecipesViewModel.userRecipes.observe(viewLifecycleOwner) { userRecipes ->
            if (userRecipes.isNullOrEmpty()) {
                setNoRecipesMessage("user")
                Toast.makeText(requireContext(), "No recipes found.", Toast.LENGTH_SHORT).show()
                binding.userRecipesRecyclerView.visibility = View.GONE
            } else {
                userRecipeAdapter.submitList(userRecipes)
                binding.recipeListTextView.visibility = View.GONE
                binding.userRecipesRecyclerView.visibility = View.VISIBLE
            }
        }

        myRecipesViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        myRecipesViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }

        // Inside your observeViewModel method for calendar recipes
        myRecipesViewModel.calendarRecipes.observe(viewLifecycleOwner) { calendarRecipes ->
            calendarRecipes?.let { recipes ->
                val days = mutableListOf<String>()
                val recipesByDay = mutableMapOf<String, MutableList<CalendarEntry>>()

                recipes.forEach { calendar ->
                    val day = calendar.savedCalendarDate
                    val dayOfMonth = day.substring(8, 10)

                    if (!days.contains(dayOfMonth)) {
                        days.add(dayOfMonth)
                    }

                    val recipeList = recipesByDay[dayOfMonth] ?: mutableListOf()

                    calendar.recipe?.let { recipeList.add(calendar) }
                    calendar.userRecipe?.let { recipeList.add(calendar) }

                    recipesByDay[dayOfMonth] = recipeList
                }

                calendarAdapter.updateCalendarData(days, recipesByDay)

                val selectedDate = "$currentYear-${currentMonth.toString().padStart(2, '0')}-${selectedDay.padStart(2, '0')}"
                val recipesForSelectedDay = recipesByDay[selectedDay] ?: emptyList()

                if (isCalendarCategoryActive && recipesForSelectedDay.isEmpty()) {
                    setNoRecipesMessage("calendar")
                } else {
                    binding.recipeListTextView.visibility = View.GONE
                }

                // ✅ Auto-load today’s recipes once
                if (!hasInitializedToday && isCalendarCategoryActive) {
                    updateRecipeListForDay(selectedDay)
                    hasInitializedToday = true
                }
            }
        }


    }

    /**
     * Called when the fragment is paused. It clears the binding reference to prevent memory leaks.
     */
    override fun onPause() {
        super.onPause()
        _binding = null
    }

    /**
     * Called when the fragment is destroyed. It clears the binding reference to prevent memory leaks.
     */
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}



