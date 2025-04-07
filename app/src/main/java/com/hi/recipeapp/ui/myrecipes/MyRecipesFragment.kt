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
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.hi.recipeapp.R
import com.hi.recipeapp.classes.Calendar
import com.hi.recipeapp.classes.RecipeCard
import com.hi.recipeapp.classes.UserRecipeCard
import com.hi.recipeapp.data.local.Recipe
import com.hi.recipeapp.databinding.FragmentMyRecipesBinding
import com.hi.recipeapp.ui.home.RecipeAdapter
import dagger.hilt.android.AndroidEntryPoint
import org.threeten.bp.LocalDate
import org.threeten.bp.YearMonth
@AndroidEntryPoint
class MyRecipesFragment : Fragment() {

    private lateinit var binding: FragmentMyRecipesBinding
    private val myRecipesViewModel: MyRecipesViewModel by viewModels()
    private lateinit var recipeAdapter: RecipeAdapter
    private lateinit var userRecipeAdapter: UserRecipeAdapter
    private lateinit var calendarAdapter: CalendarAdapter

    private val starSize = 30
    private val spaceBetweenStars = 3

    private val gridColumnCount = 2
    private val calendarGridColumnCount = 7

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

        userRecipeAdapter = UserRecipeAdapter { userRecipe ->
            val recipeId = userRecipe.id
            val action =
                MyRecipesFragmentDirections.actionMyRecipesFragmentToUserFullRecipeFragment(recipeId)
            findNavController().navigate(action)
        }
        // Initializing the calendar view
        Log.d("CalendarFragment", "Initializing recycler view.")
        // Set up RecyclerView
        setupRecyclerView()

        // Set up button listeners
        setupButtonListeners()

        // Observe ViewModel
        observeViewModel()

        // Set the initial state
        setInitialState()

        return binding.root
    }

    private fun setupRecyclerView() {
        // Set up the calendar adapter
        calendarAdapter = CalendarAdapter(
            weekHeaders = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"),
            days = listOf(), // Empty initially, we'll update this in updateCalendar()
            recipesByDay = mutableMapOf(),  // Empty initially
            currentMonth = currentMonth,
            currentYear = currentYear
        ) { selectedDay, recipes ->
            // Handle the day click
            Log.d("Calendar", "Day clicked: $selectedDay")

            // Update the TextView to display recipes for the selected day
            val recipeListTextView = binding.recipeListTextView
            if (recipes.isEmpty()) {
                recipeListTextView.text = "No recipes for this day"
            } else {
                recipeListTextView.text = "Recipes for $selectedDay:\n" + recipes.joinToString("\n")
            }
        }

        // Ensure RecyclerView uses this adapter
        binding.calendarRecyclerView.apply {
            layoutManager = GridLayoutManager(context, calendarGridColumnCount)  // Use calendarGridColumnCount for 7 columns
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
            toggleVisibility(View.VISIBLE, View.GONE, View.GONE, View.GONE)
            myRecipesViewModel.fetchFavoriteRecipes()
        }

        binding.myRecipesButton.setOnClickListener {
            setActiveButton(binding.myRecipesButton)
            toggleVisibility(View.GONE, View.VISIBLE, View.GONE, View.GONE)
            myRecipesViewModel.fetchUserRecipes()
        }

        binding.calendarButton.setOnClickListener {
            setActiveButton(binding.calendarButton)
            toggleVisibility(View.GONE, View.GONE, View.VISIBLE, View.VISIBLE)
            myRecipesViewModel.fetchAndDisplayCalendarRecipes()
            // Call a function to initialize the calendar for the current month
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
            updateCalendar()
        }

        binding.nextMonthButton.setOnClickListener {
            currentMonth = if (currentMonth == 12) {
                currentMonth = 1
                currentYear += 1
                1
            } else {
                currentMonth + 1
            }
            updateCalendar()
        }
    }

    private fun toggleVisibility(favVis: Int, userVis: Int, calendarVis: Int, calendarBtnsVis: Int) {
        binding.favoriteRecipeRecyclerView.visibility = favVis
        binding.userRecipesRecyclerView.visibility = userVis
        binding.calendarRecyclerView.visibility = calendarVis
        binding.previousMonthButton.visibility = calendarBtnsVis
        binding.nextMonthButton.visibility = calendarBtnsVis
        binding.calendarButtonsContainer.visibility = calendarBtnsVis
    }

    private fun setInitialState() {
        binding.favoriteRecipeRecyclerView.visibility = View.VISIBLE
        binding.userRecipesRecyclerView.visibility = View.GONE
        binding.calendarRecyclerView.visibility = View.GONE
        binding.previousMonthButton.visibility = View.GONE
        binding.nextMonthButton.visibility = View.GONE

        setActiveButton(binding.favoritesButton)
        myRecipesViewModel.fetchFavoriteRecipes()
    }

    private fun setActiveButton(button: Button) {
        val selectedTint = ContextCompat.getColorStateList(requireContext(), R.color.button_selected_tint)
        val defaultTint = ContextCompat.getColorStateList(requireContext(), R.color.button_default_tint)

        binding.favoritesButton.isSelected = button == binding.favoritesButton
        binding.myRecipesButton.isSelected = button == binding.myRecipesButton
        binding.calendarButton.isSelected = button == binding.calendarButton

        binding.favoritesButton.backgroundTintList = if (button == binding.favoritesButton) selectedTint else defaultTint
        binding.myRecipesButton.backgroundTintList = if (button == binding.myRecipesButton) selectedTint else defaultTint
        binding.calendarButton.backgroundTintList = if (button == binding.calendarButton) selectedTint else defaultTint

        binding.favoritesButton.setTextColor(ContextCompat.getColorStateList(requireContext(), R.color.button_text_selector))
        binding.myRecipesButton.setTextColor(ContextCompat.getColorStateList(requireContext(), R.color.button_text_selector))
        binding.calendarButton.setTextColor(ContextCompat.getColorStateList(requireContext(), R.color.button_text_selector))
    }

    private fun updateCalendar() {
        // Get the first day of the month and the number of days in the month
        val firstDayOfMonth = YearMonth.of(currentYear, currentMonth).atDay(1).dayOfWeek.value
        val daysInMonth = YearMonth.of(currentYear, currentMonth).lengthOfMonth()

        val days = mutableListOf<String>()

        // Add empty days before the first day of the month (ensure proper start)
        for (i in 1 until firstDayOfMonth) {
            days.add("") // Empty day to fill the first row
        }

        // Add actual days of the current month (1 to 31)
        for (i in 1..daysInMonth) {
            days.add(i.toString())  // Add the actual day numbers to the grid
        }

        // Add empty cells to complete the last row in the calendar grid
        val totalCells = days.size
        val emptyCells = 7 - (totalCells % 7)  // Ensure full 7-column weeks
        if (emptyCells != 7) {
            for (i in 1..emptyCells) {
                days.add("")  // Add empty days at the end to fill the calendar
            }
        }

        // Log the generated calendar days (for debugging)
        Log.d("Calendar", "Generated days: $days")

        // Observe the ViewModel to get recipes by day
        myRecipesViewModel.mappedCalendarRecipes.observe(viewLifecycleOwner) { (daysList, recipesByDay) ->
            val recipesByDayMap = mutableMapOf<String, MutableList<String>>()

            // Map each day to its corresponding recipes (by date)
            daysList.forEach { day ->
                val recipes = recipesByDay[day] ?: listOf()
                if (recipes.isNotEmpty()) {
                    recipesByDayMap[day] = recipes.toMutableList()  // Store the recipes for each day
                }
            }

            // Log the mapped recipes (for debugging)
            Log.d("Calendar", "Mapped calendar recipes: $recipesByDayMap")

            // Now match the full calendar days with the recipes
            val fullCalendarRecipes = mutableMapOf<String, List<String>>()

            // Fill all days with recipes or empty list if no recipes for that day
            for (day in days) {
                val recipesForDay = recipesByDayMap[day] ?: listOf()
                fullCalendarRecipes[day] = recipesForDay
            }

            // Log the final full calendar recipes (for debugging)
            Log.d("Calendar", "Full calendar with recipes: $fullCalendarRecipes")

            // Update the adapter with the complete calendar data
            calendarAdapter.updateCalendarData(days, fullCalendarRecipes)
        }
    }

    private fun initializeCalendarForCurrentMonth() {
        // Get the days in the current month
        val newDays = CalendarUtils.daysInMonthArray(LocalDate.of(currentYear, currentMonth, 1))
            .map { it?.dayOfMonth?.toString()?.padStart(2, '0') ?: " " }  // If it's null, use a placeholder (space)

        // Fetch the calendar recipes from the ViewModel
        myRecipesViewModel.calendarRecipes.observe(viewLifecycleOwner) { calendarRecipes ->
            val recipesByDay = mutableMapOf<String, List<String>>()

            // Here, we're mapping the calendar data into a format for the calendar (days and recipes)
            calendarRecipes?.forEach { calendar ->
                val fullDate = calendar.savedCalendarDate // e.g., "2025-04-01"
                val recipeTitles = mutableListOf<String>()

                calendar.recipe?.let { recipe ->
                    recipeTitles.add(recipe.title)
                }

                calendar.userRecipe?.let { userRecipe ->
                    recipeTitles.add(userRecipe.title)
                }

                // Add the recipes to the map with the fullDate as the key
                recipesByDay[fullDate] = recipeTitles
            }

            // Now, ensure we match the newDays with the recipes
            val updatedRecipesByDay = newDays.associateWith { day ->
                val fullDate = "2025-${currentMonth.toString().padStart(2, '0')}-$day" // Example: "2025-04-01"

                // If the day is null or doesn't have a recipe, return an empty list
                if (day == " " || recipesByDay[fullDate].isNullOrEmpty()) {
                    emptyList()  // Return an empty list if no recipes are found or the date is null
                } else {
                    recipesByDay[fullDate] ?: emptyList()  // Return an empty list if no recipes are found
                }
            }

            // Now, update the calendar adapter with the actual data
            calendarAdapter.updateCalendarData(newDays, updatedRecipesByDay)
        }
    }


    private fun observeViewModel() {
        myRecipesViewModel.favoriteRecipes.observe(viewLifecycleOwner) { recipes ->
            recipes?.let { recipeAdapter.submitList(it) } ?: Toast.makeText(requireContext(), "No favorite recipes found.", Toast.LENGTH_SHORT).show()
        }

        myRecipesViewModel.userRecipes.observe(viewLifecycleOwner) { userRecipes ->
            userRecipes?.let { userRecipeAdapter.submitList(it) } ?: Toast.makeText(requireContext(), "No recipes found.", Toast.LENGTH_SHORT).show()
        }

        myRecipesViewModel.calendarRecipes.observe(viewLifecycleOwner) { calendarRecipes ->
            calendarRecipes?.let { recipes ->
                val days = mutableListOf<String>()
                val recipesByDay = mutableMapOf<String, MutableList<String>>()

                recipes.forEach { calendar ->
                    val day = calendar.savedCalendarDate
                    val dayOfMonth = day.substring(8, 10)

                    if (!days.contains(dayOfMonth)) {
                        days.add(dayOfMonth)
                    }

                    val recipeList = recipesByDay[dayOfMonth] ?: mutableListOf()
                    calendar.recipe?.let { recipeList.add(it.title) }
                    calendar.userRecipe?.let { recipeList.add(it.title) }
                    recipesByDay[dayOfMonth] = recipeList
                }

                // Call the method to update the calendar adapter
                calendarAdapter.updateCalendarData(days, recipesByDay)
            }
        }
    }
}



