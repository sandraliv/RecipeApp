
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.hi.recipeapp.R
import org.threeten.bp.LocalDate
import android.content.res.Configuration
import com.hi.recipeapp.classes.CalendarEntry

class CalendarAdapter(
    private val weekHeaders: List<String>,
    private var days: List<String>,
    private var recipesByDay: Map<String, List<CalendarEntry>>,
    private val currentMonth: Int,
    private val currentYear: Int,
    private val onDayClicked: (String, List<CalendarEntry>) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val dayItemType = 1
    private val headerItemType = 2
    private var selectedDay: String = LocalDate.now().dayOfMonth.toString().padStart(2, '0')

    /**
     * Updates the calendar data with new days and recipes for each day.
     *
     * @param newDays A list of new days to update the calendar.
     * @param newRecipesByDay A map of recipes associated with each day.
     */
    fun updateCalendarData(newDays: List<String>, newRecipesByDay: Map<String, List<CalendarEntry>>) {
        val updatedDays = mutableListOf<String>()
        val updatedRecipesByDay = mutableMapOf<String, List<CalendarEntry>>()

        // Add all the new days (ensure no days are missing)
        updatedDays.addAll(newDays)

        // Add recipes for each day, default to empty list if no recipes exist for a day
        newDays.forEach { day ->
            updatedRecipesByDay[day] = newRecipesByDay[day] ?: emptyList()
            Log.d("CalendarAdapter", "Day: $day, Recipes: ${updatedRecipesByDay[day]}")
        }

        val today = LocalDate.now().dayOfMonth.toString().padStart(2, '0')  // Today's date, formatted (e.g., "07")
        Log.d("CalendarAdapter", "Today's Date: $today")

        // Update the data for the adapter
        this.days = updatedDays
        this.recipesByDay = updatedRecipesByDay

        // Notify the adapter that the data has changed
        notifyDataSetChanged()  // Ensure the adapter is notified about the data update
    }

    /**
     * Determines the view type for the current item (header or day).
     */
    override fun getItemViewType(position: Int): Int {
        return if (position < weekHeaders.size) headerItemType else dayItemType
    }
    /**
     * Creates a view holder based on the item view type (either header or day).
     */
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
    /**
     * Binds data to the appropriate view holder (either the week header or day).
     */
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is WeekHeaderViewHolder -> {
                // Bind weekday names to the week header
                val header = weekHeaders[position]
                holder.bind(header)
            }
            is DayViewHolder -> {
                val day = days[position - weekHeaders.size]  // Subtract the header size to get the day
                val recipes = recipesByDay[day] ?: emptyList()
                val isToday = isToday(day)
                val isSelected = day == selectedDay

                Log.d("CalendarAdapter", "Initially Selected Day: $selectedDay")

                // Log the selected day for debugging
                Log.d("CalendarAdapter", "Day: $day, isToday: $isToday, isSelected: $isSelected")

                holder.bind(day, isToday, isSelected)
                holder.itemView.setOnClickListener {
                    selectedDay = day
                    onDayClicked(day, recipes)

                    // Log the selected day when clicked
                    Log.d("CalendarAdapter", "Selected Day: $selectedDay")
                    notifyDataSetChanged()  // Refresh the adapter to reflect the new selected day
                }
            }
        }
    }


    /**
     * Returns the total item count for the calendar (week headers + days).
     */
    override fun getItemCount(): Int {
        return weekHeaders.size + days.size
    }

    /**
     * View holder for the week header that displays the weekday names.
     */
    inner class WeekHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val weekHeaderTextView: TextView = itemView.findViewById(R.id.weekHeaderTextView)
        /**
         * Binds the weekday name to the header.
         */
        fun bind(header: String) {
            weekHeaderTextView.text = header
        }
    }
    /**
     * View holder for the day items that display the days of the month.
     */
    inner class DayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dayTextView: TextView = itemView.findViewById(R.id.dayTextView)
        /**
         * Binds the day and its status (today/selected) to the day view.
         *
         * @param day The day number (e.g., "01", "15", etc.).
         * @param isToday Whether the day is today's date.
         * @param isSelected Whether the day is selected.
         */
        fun bind(day: String, isToday: Boolean, isSelected: Boolean) {
            dayTextView.text = day

            // Determine if the app is in night mode or day mode
            val isNightMode = (itemView.context.resources.configuration.uiMode and
                    Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

            // Initialize color variables
            var textColor: Int
            var backgroundColor: Int

            if (isNightMode) {
                // Night Mode: Apply colors specific to night mode
                if (isToday && isSelected) {
                    // Today's date is selected in night mode, apply white text with dark background
                    textColor = ContextCompat.getColor(itemView.context, R.color.white)
                    backgroundColor = ContextCompat.getColor(itemView.context, R.color.selected_today_night_background_color)

                } else if (isSelected) {
                    // Other selected date in night mode, apply white text with lighter dark background
                    textColor = ContextCompat.getColor(itemView.context, R.color.white)
                    backgroundColor = ContextCompat.getColor(itemView.context, R.color.selected_night_background_color)

                } else if (isToday) {
                    // Today is not selected in night mode, apply light rosybrown text with transparent background
                    textColor = ContextCompat.getColor(itemView.context, R.color.selected_today_night_text_color)
                    backgroundColor = ContextCompat.getColor(itemView.context, R.color.transparent)

                } else {
                    // Non-selected date in night mode, apply light gray text with transparent background
                    textColor = ContextCompat.getColor(itemView.context, R.color.selected_night_text_color)
                    backgroundColor = ContextCompat.getColor(itemView.context, R.color.transparent)
                }
            } else {
                // Day Mode: Apply colors specific to day mode
                if (isToday && isSelected) {
                    // Today's date is selected in day mode, apply black text with rosybrown 400 background
                    textColor = ContextCompat.getColor(itemView.context, R.color.selected_day_text_color)
                    backgroundColor = ContextCompat.getColor(itemView.context, R.color.selected_today_day_background_color)

                } else if (isSelected) {
                    // Other selected date in day mode, apply black text with rosybrown 200 background
                    textColor = ContextCompat.getColor(itemView.context, R.color.selected_day_text_color)
                    backgroundColor = ContextCompat.getColor(itemView.context, R.color.selected_day_background_color)

                } else if (isToday) {
                    // Today is not selected in day mode, apply rosýbrown 400 text color with transparent background
                    textColor = ContextCompat.getColor(itemView.context, R.color.rosybrown)
                    backgroundColor = ContextCompat.getColor(itemView.context, R.color.transparent)

                } else {
                    // Non-selected date in day mode, apply black text with transparent background
                    textColor = ContextCompat.getColor(itemView.context, R.color.black)
                    backgroundColor = ContextCompat.getColor(itemView.context, R.color.transparent)
                }
            }

            // Apply the text color and background color
            dayTextView.setTextColor(textColor)
            dayTextView.setBackgroundColor(backgroundColor)

        }

    }

    /**
     * Checks if a given day is today's date.
     *
     * @param day The day to check.
     * @return True if the day is today's date, false otherwise.
     */
    private fun isToday(day: String): Boolean {
        val currentDate = LocalDate.now()
        return try {
            // Ensure day is a two-digit string
            val dayOfMonth = day.padStart(2, '0').toIntOrNull() ?: return false
            currentDate.dayOfMonth == dayOfMonth &&
                    currentDate.monthValue == currentMonth &&
                    currentDate.year == currentYear
        } catch (e: Exception) {
            false
        }
    }

}
