package com.hi.recipeapp.ui.myrecipes

import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime
import org.threeten.bp.YearMonth
import org.threeten.bp.format.DateTimeFormatter

object CalendarUtils {

    // Get a list of days in the current month
    fun daysInMonthArray(date: LocalDate): List<LocalDate?> {
        val daysInMonthArray = mutableListOf<LocalDate?>()
        val yearMonth = YearMonth.from(date)
        val daysInMonth = yearMonth.lengthOfMonth()

        val firstOfMonth = date.withDayOfMonth(1)
        val dayOfWeek = firstOfMonth.dayOfWeek.value

        // Fill in the days of the month
        for (i in 1..42) { // 6 rows for a calendar
            if (i <= dayOfWeek || i > daysInMonth + dayOfWeek) {
                daysInMonthArray.add(null)
            } else {
                daysInMonthArray.add(LocalDate.of(date.year, date.month, i - dayOfWeek))
            }
        }

        return daysInMonthArray
    }

    // Format a LocalDate to a human-readable string (e.g., 01 January 2025)
    fun formattedDate(date: LocalDate): String {
        val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy")
        return date.format(formatter)
    }

    // Format a LocalTime to a string (e.g., 10:30 AM)
    fun formattedTime(time: LocalTime): String {
        val formatter = DateTimeFormatter.ofPattern("hh:mm a")
        return time.format(formatter)
    }

    // Helper to check if a given date is today's date
    fun isToday(date: LocalDate): Boolean {
        val today = LocalDate.now()
        return today.year == date.year && today.month == date.month && today.dayOfMonth == date.dayOfMonth
    }

    // This function is optional: Generate a list of weekday headers (Mon, Tue, Wed, ...)
    fun weekHeaders(): List<String> {
        return listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    }

    // Fetch recipes for the given month and year (this can be replaced with actual data fetching logic)
    fun fetchRecipesForMonth(month: Int, year: Int): Map<String, List<String>> {
        // Placeholder example: Replace with actual logic to fetch recipes for the month
        return mapOf(
            "1" to listOf("Scrambled Eggs"),
            "2" to listOf("Pancakes"),
            "3" to listOf("Salad"),
            "4" to listOf("Spaghetti"),
            "5" to listOf("Chicken Curry"),
            "6" to listOf("Salmon"),
            // Add more days here...
        )
    }
}
