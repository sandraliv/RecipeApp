package com.hi.recipeapp.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hi.recipeapp.R
import com.hi.recipeapp.databinding.ItemRecipeCardBinding
import com.hi.recipeapp.classes.RecipeCard

class RecipeAdapter(private val onClick: (RecipeCard) -> Unit) : ListAdapter<RecipeCard, RecipeAdapter.RecipeViewHolder>(RecipeDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val binding = ItemRecipeCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecipeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val recipe = getItem(position) // Use getItem() to get the data
        holder.bind(recipe)
    }

    inner class RecipeViewHolder(private val binding: ItemRecipeCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(recipe: RecipeCard) {
            binding.recipeName.text = recipe.title
            binding.recipeDescription.text = recipe.description
            // Handling fractional rating (e.g., showing full and half stars)
            val fullStars = recipe.averageRating.toInt() // The integer part
            val hasHalfStar = recipe.averageRating % 1 >= 0.5 // If there's a fractional part (>= 0.5, show half star)
            val emptyStars = 5 - fullStars - if (hasHalfStar) 1 else 0 // Remaining empty stars

            // Build the star rating string
            val starRating = StringBuilder()
            starRating.append("⭐".repeat(fullStars))  // Full stars
            if (hasHalfStar) starRating.append("🌟")  // Half star
            starRating.append("☆".repeat(emptyStars))  // Empty stars

            binding.recipeRatingStars.text = starRating.toString()
            binding.recipeRatingCount.text = "(${recipe.ratingCount})"

            Glide.with(binding.root.context)
                .load(recipe.imageUrl)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.error_image)
                .into(binding.recipeImage)

            binding.root.setOnClickListener {
                onClick(recipe)  // Passing the full RecipeCard object
            }
        }
    }

    // DiffUtil callback to optimize list updates
    class RecipeDiffCallback : DiffUtil.ItemCallback<RecipeCard>() {
        override fun areItemsTheSame(oldItem: RecipeCard, newItem: RecipeCard): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: RecipeCard, newItem: RecipeCard): Boolean {
            return oldItem == newItem
        }
    }
}

