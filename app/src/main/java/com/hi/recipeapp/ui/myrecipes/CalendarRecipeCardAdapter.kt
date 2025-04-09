package com.hi.recipeapp.ui.myrecipes


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hi.recipeapp.classes.CalendarRecipeCard
import com.hi.recipeapp.databinding.ItemCalendarRecipeCardBinding


class CalendarRecipeCardAdapter(
    private val onRecipeClick: (CalendarRecipeCard) -> Unit
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

