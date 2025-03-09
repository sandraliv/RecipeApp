package com.hi.recipeapp.ui.home

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hi.recipeapp.R
import com.hi.recipeapp.databinding.ItemRecipeCardBinding
import com.hi.recipeapp.classes.RecipeCard

class RecipeAdapter(
    private val onClick: (RecipeCard) -> Unit,
    private val onFavoriteClick: (RecipeCard, Boolean) -> Unit
) : ListAdapter<RecipeCard, RecipeAdapter.RecipeViewHolder>(RecipeDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val binding = ItemRecipeCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecipeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val recipe = getItem(position)
        holder.bind(recipe)
    }

    inner class RecipeViewHolder(private val binding: ItemRecipeCardBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(recipe: RecipeCard) {
            Log.d("RecipeViewHolder", "isFavoritedByUser: ${recipe.isFavoritedByUser}")

            // Handle heart icon visibility based on whether the recipe is favorited
            updateHeartButtonVisibility(recipe)

            // Handle empty heart button click (add to favorites)
            binding.emptyHeartButton.setOnClickListener {
                recipe.isFavoritedByUser = true
                onFavoriteClick(recipe, true)
            }

            // Handle filled heart button click (remove from favorites)
            binding.filledHeartButton.setOnClickListener {
                recipe.isFavoritedByUser = false
                onFavoriteClick(recipe, false)
            }

            binding.recipeName.text = recipe.title
            binding.recipeDescription.text = recipe.description

            // Handling fractional rating (stars)
            val fullStars = recipe.averageRating.toInt()
            val hasHalfStar = recipe.averageRating % 1 >= 0.5
            val emptyStars = 5 - fullStars - if (hasHalfStar) 1 else 0
            val starRating = StringBuilder().apply {
                append("⭐".repeat(fullStars))
                if (hasHalfStar) append("🌟")
                append("☆".repeat(emptyStars))
            }
            binding.recipeRatingStars.text = starRating.toString()
            binding.recipeRatingCount.text = "(${recipe.ratingCount})"

            Glide.with(binding.root.context)
                .load(recipe.imageUrl)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.error_image)
                .into(binding.recipeImage)

            // Handle item click to navigate to recipe details
            binding.root.setOnClickListener {
                onClick(recipe)
            }
        }
        private fun updateHeartButtonVisibility(recipe: RecipeCard) {
            // Show the appropriate button based on the recipe's favorite status
            if (recipe.isFavoritedByUser) {
                binding.filledHeartButton.visibility = View.VISIBLE
                binding.emptyHeartButton.visibility = View.GONE
            } else {
                binding.filledHeartButton.visibility = View.GONE
                binding.emptyHeartButton.visibility = View.VISIBLE
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



