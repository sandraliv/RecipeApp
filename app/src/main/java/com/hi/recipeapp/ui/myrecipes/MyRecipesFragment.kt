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
import org.threeten.bp.format.TextStyle
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class MyRecipesFragment : Fragment() {

    private lateinit var binding: FragmentMyRecipesBinding
    private val myRecipesViewModel: MyRecipesViewModel by viewModels()
    private lateinit var recipeAdapter: RecipeAdapter
    private lateinit var userRecipeAdapter: UserRecipeAdapter
    private lateinit var calendarAdapter: CalendarAdapter
    private lateinit var calendarRecipeCardAdapter: CalendarRecipeCardAdapter

    @Inject
    lateinit var sessionManager: SessionManager

    private val starSize = 30
    private val spaceBetweenStars = 3
    private val gridColumnCount = 2
    private val calendarGridColumnCount = 7
    private var isCalendarCategoryActive = false
    private var selectedDay: String = LocalDate.now().dayOfMonth.toString().padStart(2, '0')
    private var currentMonth: Int = LocalDate.now().monthValue
    private var currentYear: Int = LocalDate.now().year

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMyRecipesBinding.inflate(inflater, container, false)

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

        calendarRecipeCardAdapter = CalendarRecipeCardAdapter { clickedRecipe ->
            val recipeId = clickedRecipe.id
            val action = if (clickedRecipe.isUserRecipe) {
                MyRecipesFragmentDirections.actionMyRecipesFragmentToUserFullRecipeFragment(recipeId)
            } else {
                MyRecipesFragmentDirections.actionMyRecipesFragmentToFullRecipeFragment(recipeId)
            }
            findNavController().navigate(action)
        }


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
                currentYear -= 1  // Go to the previous year if it's January
                12
            } else {
                currentMonth - 1
            }
            updateCalendarWithCurrentMonth()  // Update month and year display
        }

        binding.nextMonthButton.setOnClickListener {
            currentMonth = if (currentMonth == 12) {
                currentMonth = 1
                currentYear += 1  // Go to the next year if it's December
                1
            } else {
                currentMonth + 1
            }
            updateCalendarWithCurrentMonth()  // Update month and year display
        }

    }

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

    private fun setNoRecipesMessage(category: String) {
        val recipeListTextView = binding.recipeListTextView

        when (category) {
            "calendar" -> {
                recipeListTextView.text = "No recipes for this day"
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

    private fun setInitialState() {
        val today = LocalDate.now().dayOfMonth.toString().padStart(2, '0')

        binding.favoriteRecipeRecyclerView.visibility = View.VISIBLE
        binding.userRecipesRecyclerView.visibility = View.GONE
        binding.calendarRecyclerView.visibility = View.GONE
        binding.calendarRecipeRecyclerView.visibility = View.GONE
        binding.calendarButtonsContainer.visibility = View.GONE

        setActiveButton(binding.favoritesButton)
        myRecipesViewModel.fetchFavoriteRecipes()
        updateRecipeListForDay(today)
        selectedDay = today
    }

    private fun updateRecipeListForDay(day: String) {
        val recipeRecyclerView = binding.calendarRecipeRecyclerView
        val recipeListTextView = binding.recipeListTextView
        val formattedDay = day.padStart(2, '0')

        // Convert formattedDay to LocalDate and format it to a human-readable string (e.g., "01 January")
        val dayOfMonth = LocalDate.of(currentYear, currentMonth, formattedDay.toInt())
        val formattedDayString = CalendarUtils.formattedDate(dayOfMonth)

        // Format the selected day to include the full date (Year-Month-Day)
        val selectedDate = LocalDate.of(currentYear, currentMonth, formattedDay.toInt()).toString()  // "2025-04-01"
        Log.d("MY RECIPES FRAGMENT", "Selected Date: $selectedDate")
        // Get the recipes for the selected full date from your ViewModel (CalendarEntry list)
        val calendarEntriesForToday: List<CalendarEntry> =
            myRecipesViewModel.mappedCalendarRecipes.value?.second?.get(selectedDate) ?: emptyList()

        Log.d("MY RECIPES FRAGMENT", "Found recipes for $selectedDate: ${calendarEntriesForToday.size}")  // Log the number of recipes found

        // Convert CalendarEntry to CalendarRecipeCard
        val recipesForToday: List<CalendarRecipeCard> =
            calendarEntriesForToday.mapNotNull { calendarEntry ->
                // Assuming each CalendarEntry contains either a recipe or a userRecipe that is a CalendarRecipeCard
                calendarEntry.recipe ?: calendarEntry.userRecipe
            }
        Log.d("MY RECIPES FRAGMENT", "Mapped recipes to CalendarRecipeCard for $selectedDate: ${recipesForToday.size}")

        // Now submit the new list to the adapter
        val newRecipeList: List<CalendarRecipeCard> = recipesForToday // Get your new list of recipes
        calendarRecipeCardAdapter.submitList(newRecipeList)  // Use submitList to update the data
        Log.d("CalendarRecipeCardAdapter", "Submitted ${newRecipeList.size} recipes")


        if (calendarEntriesForToday.isEmpty()) {
            recipeRecyclerView.visibility = View.GONE
            setNoRecipesMessage("calendar")  // Call this for calendar
        } else {
            recipeRecyclerView.visibility = View.VISIBLE
            recipeListTextView.visibility = View.GONE
        }
    }

    private fun updateCalendarWithCurrentMonth() {
        val monthName =
            Month.of(currentMonth).name.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }

        val year = currentYear.toString()

        binding.monthTextView.text = "$monthName $year"

        updateCalendar()  // Update the calendar grid with the new month and year

        myRecipesViewModel.fetchAndDisplayCalendarRecipes()
    }

    private fun setActiveButton(button: Button) {
        val selectedTint =
            ContextCompat.getColorStateList(requireContext(), R.color.button_selected_tint)
        val defaultTint =
            ContextCompat.getColorStateList(requireContext(), R.color.button_default_tint)

        binding.favoritesButton.isSelected = button == binding.favoritesButton
        binding.myRecipesButton.isSelected = button == binding.myRecipesButton
        binding.calendarButton.isSelected = button == binding.calendarButton

        binding.favoritesButton.backgroundTintList =
            if (button == binding.favoritesButton) selectedTint else defaultTint
        binding.myRecipesButton.backgroundTintList =
            if (button == binding.myRecipesButton) selectedTint else defaultTint
        binding.calendarButton.backgroundTintList =
            if (button == binding.calendarButton) selectedTint else defaultTint

        binding.favoritesButton.setTextColor(
            ContextCompat.getColorStateList(
                requireContext(),
                R.color.button_text_selector
            )
        )
        binding.myRecipesButton.setTextColor(
            ContextCompat.getColorStateList(
                requireContext(),
                R.color.button_text_selector
            )
        )
        binding.calendarButton.setTextColor(
            ContextCompat.getColorStateList(
                requireContext(),
                R.color.button_text_selector
            )
        )
    }

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

            // Once the data is updated, make sure the adapter is also updated
            calendarAdapter.updateCalendarData(daysList, fullCalendarRecipes)
        }
    }

    private fun initializeCalendarForCurrentMonth() {

        // Get days of the current month and format them
        val newDays = CalendarUtils.daysInMonthArray(LocalDate.of(currentYear, currentMonth, 1))
            .map { it?.dayOfMonth?.toString()?.padStart(2, '0') ?: " " }

        // Observe calendar recipes from ViewModel
        myRecipesViewModel.calendarRecipes.observe(viewLifecycleOwner) { calendarRecipes ->
            val recipesByDay = mutableMapOf<String, MutableList<CalendarEntry>>()

            // Loop through the calendar recipes and add them to recipesByDay
            calendarRecipes?.forEach { calendar ->
                val fullDate = calendar.savedCalendarDate

                // Create a list of CalendarEntry objects
                val calendarEntries = mutableListOf<CalendarEntry>()

                // Add the recipe to the list if available
                calendar.recipe?.let { recipe ->
                    calendarEntries.add(calendar)  // Add the current calendar entry
                }

                // Add the user-created recipe if available
                calendar.userRecipe?.let { userRecipe ->
                    calendarEntries.add(calendar)  // Add the user recipe
                }

                // Store recipes by full date key (the fullDate here is the "yyyy-MM-dd" format)
                recipesByDay[fullDate] = calendarEntries
            }

            // Ensure we match the newDays with the recipes (fullDate format: YYYY-MM-DD)
            val updatedRecipesByDay = newDays.associateWith { day ->
                val fullDate = "${currentYear}-${currentMonth.toString().padStart(2, '0')}-$day"  // Example: "2025-04-01"


                // If the day is null or doesn't have a recipe, return an empty list
                if (day == " " || recipesByDay[fullDate].isNullOrEmpty()) {
                    emptyList()  // Return an empty list if no recipes are found or the date is null
                } else {
                    recipesByDay[fullDate] ?: emptyList()  // Return an empty list if no recipes are found
                }
            }

            // Update the adapter with the new days and associated recipes
            calendarAdapter.updateCalendarData(newDays, updatedRecipesByDay)
        }

        // Ensure you update the calendar view with the current month
        updateCalendarWithCurrentMonth()
    }

    private fun observeViewModel() {

        myRecipesViewModel.favoriteRecipes.observe(viewLifecycleOwner) { recipes ->
            if (recipes.isNullOrEmpty()) {
                setNoRecipesMessage("favorites")  // Call this for favorites
                Toast.makeText(requireContext(), "No recipes found.", Toast.LENGTH_SHORT).show()
                binding.favoriteRecipeRecyclerView.visibility = View.GONE
            } else {
                recipeAdapter.submitList(recipes)
                binding.recipeListTextView.visibility = View.GONE  // Hide message if data exists
                binding.favoriteRecipeRecyclerView.visibility = View.VISIBLE
            }
        }

        // Observing user recipes
        myRecipesViewModel.userRecipes.observe(viewLifecycleOwner) { userRecipes ->
            if (userRecipes.isNullOrEmpty()) {
                setNoRecipesMessage("user")  // Call this for user recipes
                Toast.makeText(requireContext(), "No recipes found.", Toast.LENGTH_SHORT).show()
                binding.userRecipesRecyclerView.visibility = View.GONE
            } else {
                userRecipeAdapter.submitList(userRecipes)
                binding.recipeListTextView.visibility = View.GONE  // Hide message if data exists
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
                val recipesByDay = mutableMapOf<String, MutableList<CalendarEntry>>() // Use CalendarEntry instead of String

                recipes.forEach { calendar ->
                    val day = calendar.savedCalendarDate
                    val dayOfMonth = day.substring(8, 10)

                    if (!days.contains(dayOfMonth)) {
                        days.add(dayOfMonth)
                    }

                    // Initialize the recipe list for the specific day
                    val recipeList = recipesByDay[dayOfMonth] ?: mutableListOf()

                    // Add recipe and user recipe if available
                    calendar.recipe?.let { recipeList.add(calendar) }
                    calendar.userRecipe?.let { recipeList.add(calendar) }

                    // Update the map with the list of CalendarEntry objects for that day
                    recipesByDay[dayOfMonth] = recipeList
                }

                // Update the adapter with the days and recipes for each day
                calendarAdapter.updateCalendarData(days, recipesByDay)

                // Now check if there are no recipes for the selected day, and display message accordingly
                val selectedDate = "$currentYear-${currentMonth.toString().padStart(2, '0')}-${selectedDay.padStart(2, '0')}"
                val recipesForSelectedDay = recipesByDay[selectedDay] ?: emptyList()

                if (isCalendarCategoryActive && recipesForSelectedDay.isEmpty()) {
                    setNoRecipesMessage("calendar") // Show "No recipes for this day" if no recipes are found
                } else {
                    binding.recipeListTextView.visibility = View.GONE // Hide the message when there are recipes
                }
            }
        }


    }
}



