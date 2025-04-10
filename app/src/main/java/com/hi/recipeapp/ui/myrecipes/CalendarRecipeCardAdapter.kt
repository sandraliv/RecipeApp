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


class CalendarRecipeCardAdapter(
    private val onRecipeClick: (CalendarRecipeCard) -> Unit,
    private val onRemoveFromCalendarClick: (CalendarRecipeCard) -> Unit
) : ListAdapter<CalendarRecipeCard, CalendarRecipeCardAdapter.CalendarRecipeCardViewHolder>(RecipeDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarRecipeCardViewHolder {
        val binding = ItemCalendarRecipeCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CalendarRecipeCardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CalendarRecipeCardViewHolder, position: Int) {
        val recipeCard = getItem(position)
        holder.bind(recipeCard)
        // Set up click listener for the remove button (or however you remove from the calendar)
        holder.itemView.findViewById<AppCompatImageButton>(R.id.removeFromCalendarButton).setOnClickListener {
            onRemoveFromCalendarClick(recipeCard)  // This is how you trigger the lambda when the remove button is clicked
        }
    }


    inner class CalendarRecipeCardViewHolder(private val binding: ItemCalendarRecipeCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(recipeCard: CalendarRecipeCard) {
            binding.recipeCard = recipeCard
            binding.root.setOnClickListener {
                onRecipeClick(recipeCard)
            }
        }
    }

    class RecipeDiffCallback : DiffUtil.ItemCallback<CalendarRecipeCard>() {
        override fun areItemsTheSame(oldItem: CalendarRecipeCard, newItem: CalendarRecipeCard): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: CalendarRecipeCard, newItem: CalendarRecipeCard): Boolean {
            return oldItem == newItem
        }
    }
}

