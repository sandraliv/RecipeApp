package com.hi.recipeapp.ui.myrecipes

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hi.recipeapp.R
import com.hi.recipeapp.classes.CalendarRecipeCard
import com.hi.recipeapp.databinding.ItemCalendarRecipeCardBinding


class CalendarRecipeCardAdapter(
    private var recipeList: List<CalendarRecipeCard>,  // List of recipes for a particular day
    private val onRecipeClick: (CalendarRecipeCard) -> Unit  // A callback function to handle recipe clicks
) : ListAdapter<CalendarRecipeCard, CalendarRecipeCardAdapter.CalendarRecipeCardViewHolder>(RecipeDiffCallback()) {

    // Create the ViewHolder instance
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CalendarRecipeCardViewHolder {
        val binding = ItemCalendarRecipeCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CalendarRecipeCardViewHolder(binding)
    }

    // Bind the data to the ViewHolder
    override fun onBindViewHolder(holder: CalendarRecipeCardViewHolder, position: Int) {
        val recipeCard = getItem(position)  // Use getItem instead of accessing the list directly
        holder.bind(recipeCard)
    }

    // Create a ViewHolder for the calendar recipe cards
    inner class CalendarRecipeCardViewHolder(private val binding: ItemCalendarRecipeCardBinding) :

        RecyclerView.ViewHolder(binding.root) {

        fun bind(recipeCard: CalendarRecipeCard) {
            Log.d("CalendarRecipeCardAdapter", "Binding recipeCard: $recipeCard")
            // Bind the recipeCard to the binding
            binding.recipeCard = recipeCard

            // Handling recipe click
            binding.root.setOnClickListener {
                onRecipeClick(recipeCard)  // Pass the recipe card on click
            }
        }
    }

    // DiffUtil callback to efficiently compare and update the list
    class RecipeDiffCallback : DiffUtil.ItemCallback<CalendarRecipeCard>() {
        override fun areItemsTheSame(oldItem: CalendarRecipeCard, newItem: CalendarRecipeCard): Boolean {
            return oldItem.id == newItem.id  // Assuming each recipe has a unique ID
        }

        override fun areContentsTheSame(oldItem: CalendarRecipeCard, newItem: CalendarRecipeCard): Boolean {
            return oldItem == newItem
        }
    }

    // Return the number of items in the recipe list
    override fun getItemCount(): Int = recipeList.size


}
