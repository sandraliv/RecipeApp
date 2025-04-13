package com.hi.recipeapp.ui.myrecipes


import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.widget.AppCompatImageButton
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hi.recipeapp.R
import com.hi.recipeapp.classes.CalendarRecipeCard
import com.hi.recipeapp.databinding.ItemCalendarRecipeCardBinding

/**
 * Adapter for displaying a list of recipe cards in a calendar view, supporting click actions
 * for viewing the recipe details and removing a recipe from the calendar.
 *
 * @param onRecipeClick Lambda function triggered when a recipe card is clicked.
 * @param onRemoveFromCalendarClick Lambda function triggered when the "remove from calendar" button is clicked.
 */
class CalendarRecipeCardAdapter(
    private val onRecipeClick: (CalendarRecipeCard) -> Unit,
    private val onRemoveFromCalendarClick: (CalendarRecipeCard) -> Unit
) : ListAdapter<CalendarRecipeCard, CalendarRecipeCardAdapter.CalendarRecipeCardViewHolder>(RecipeDiffCallback()) {
    /**
     * Creates a new ViewHolder for the calendar recipe card item.
     *
     * @param parent The parent view group where the item view will be attached.
     * @param viewType The type of the view (not used here but needed by the RecyclerView).
     * @return A new ViewHolder instance for the recipe card.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarRecipeCardViewHolder {
        val binding = ItemCalendarRecipeCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CalendarRecipeCardViewHolder(binding)
    }
    /**
     * Binds the data for a specific position in the adapter.
     *
     * @param holder The ViewHolder that will display the data.
     * @param position The position of the data in the list.
     */
    override fun onBindViewHolder(holder: CalendarRecipeCardViewHolder, position: Int) {
        val recipeCard = getItem(position)
        holder.bind(recipeCard)
        // Set up click listener for the remove button (or however you remove from the calendar)
        holder.itemView.findViewById<AppCompatImageButton>(R.id.remove_from_calendar_button).setOnClickListener {
            onRemoveFromCalendarClick(recipeCard)  // This is how you trigger the lambda when the remove button is clicked
        }
    }

    /**
     * ViewHolder that holds the views for a calendar recipe card.
     *
     * @param binding The view binding for the calendar recipe card item layout.
     */
    inner class CalendarRecipeCardViewHolder(private val binding: ItemCalendarRecipeCardBinding) :
        RecyclerView.ViewHolder(binding.root) {
        /**
         * Binds the recipe card data to the ViewHolder's views.
         *
         * @param recipeCard The recipe card to be displayed.
         */
        fun bind(recipeCard: CalendarRecipeCard) {
            binding.recipeCard = recipeCard
            binding.root.setOnClickListener {
                onRecipeClick(recipeCard)
            }
        }
    }
    /**
     * DiffUtil callback used to determine if two recipe cards are the same and if their contents are the same.
     */
    class RecipeDiffCallback : DiffUtil.ItemCallback<CalendarRecipeCard>() {
        /**
         * Compares two recipe cards to determine if they are the same item (based on their unique ID).
         *
         * @param oldItem The old recipe card item.
         * @param newItem The new recipe card item.
         * @return True if the items are the same, false otherwise.
         */
        override fun areItemsTheSame(oldItem: CalendarRecipeCard, newItem: CalendarRecipeCard): Boolean {
            return oldItem.id == newItem.id
        }
        /**
         * Compares the contents of two recipe cards to determine if they are the same.
         *
         * @param oldItem The old recipe card item.
         * @param newItem The new recipe card item.
         * @return True if the contents are the same, false otherwise.
         */
        override fun areContentsTheSame(oldItem: CalendarRecipeCard, newItem: CalendarRecipeCard): Boolean {
            return oldItem == newItem
        }
    }
}

