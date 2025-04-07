import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import androidx.recyclerview.widget.RecyclerView
import com.hi.recipeapp.R
import com.hi.recipeapp.classes.RecipeCard
import com.hi.recipeapp.classes.UserRecipeCard
import com.hi.recipeapp.databinding.ItemCalendarBinding
import org.threeten.bp.LocalDate
import java.util.Locale

class CalendarAdapter(
    private val weekHeaders: List<String>,  // Weekdays like Mon, Tue, Wed
    private var days: List<String>,         // Days of the month (1, 2, 3, etc.)
    private var recipesByDay: Map<String, List<String>>,  // Recipes for each day
    private val currentMonth: Int,         // Current month for highlighting today's date
    private val currentYear: Int,          // Current year for checking if it's today's date
    private val onDayClicked: (String, List<String>) -> Unit // Pass day and recipes to Fragment
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val dayItemType = 1
    private val headerItemType = 2

    fun updateCalendarData(newDays: List<String>, newRecipesByDay: Map<String, List<String>>) {
        val updatedDays = mutableListOf<String>()
        val updatedRecipesByDay = mutableMapOf<String, List<String>>()

        // Add all the new days (ensure no days are missing)
        updatedDays.addAll(newDays)

        // Add recipes for each day, default to empty list if no recipes exist for a day
        newDays.forEach { day ->
            updatedRecipesByDay[day] = newRecipesByDay[day] ?: emptyList()
        }
        // Log the new days and recipes
        Log.d("CalendarAdapter", "Updated days: $updatedDays")
        Log.d("CalendarAdapter", "Updated recipes by day: $updatedRecipesByDay")
        // Update the data
        this.days = updatedDays
        this.recipesByDay = updatedRecipesByDay

        // Notify the adapter that the data has changed
        notifyDataSetChanged()
    }


    override fun getItemViewType(position: Int): Int {
        return if (position < weekHeaders.size) headerItemType else dayItemType
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == headerItemType) {
            // Inflate the week header layout
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_calendar_week_header, parent, false)
            WeekHeaderViewHolder(view)
        } else {
            // Inflate the calendar day layout
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_calendar_day, parent, false)
            DayViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is WeekHeaderViewHolder -> {
                // Bind weekday names to the week header
                val header = weekHeaders[position]
                holder.bind(header)
            }
            is DayViewHolder -> {
                // Bind day data
                val day = days[position - weekHeaders.size] // Subtract the header size to get the day
                val recipes = recipesByDay[day] ?: emptyList()

                // Check if the day is today's date
                val isToday = isToday(day)

                holder.bind(day, isToday)

                // Set click listener to pass selected day and recipes to the fragment
                holder.itemView.setOnClickListener {
                    onDayClicked(day, recipes)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        // Return total count (week headers + days)
        return weekHeaders.size + days.size
    }

    // ViewHolder for Week Header (Mon, Tue, Wed, ...)
    inner class WeekHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val weekHeaderTextView: TextView = itemView.findViewById(R.id.weekHeaderTextView)

        fun bind(header: String) {
            weekHeaderTextView.text = header
        }
    }

    // ViewHolder for Day Item (1, 2, 3, ...)
    inner class DayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dayTextView: TextView = itemView.findViewById(R.id.dayTextView)

        fun bind(day: String, isToday: Boolean) {
            dayTextView.text = day
            dayTextView.setBackgroundColor(
                if (isToday) itemView.context.getColor(R.color.rosybrown_400) else Color.TRANSPARENT
            )
        }
    }

    // Helper function to check if the day is today's date
    private fun isToday(day: String): Boolean {
        val currentDate = LocalDate.now()
        return try {
            val dayOfMonth = day.toIntOrNull() ?: return false
            currentDate.dayOfMonth == dayOfMonth &&
                    currentDate.monthValue == currentMonth &&
                    currentDate.year == currentYear
        } catch (e: Exception) {
            false
        }
    }
}
