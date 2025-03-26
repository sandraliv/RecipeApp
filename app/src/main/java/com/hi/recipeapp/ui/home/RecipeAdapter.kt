package com.hi.recipeapp.ui.home

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hi.recipeapp.R
import com.hi.recipeapp.databinding.ItemRecipeCardBinding
import com.hi.recipeapp.classes.RecipeCard

class RecipeAdapter(
    private val onClick: (RecipeCard) -> Unit,
    private val onFavoriteClick: (RecipeCard, Boolean) -> Unit,
    private val starSize: Int,  // Add starSize as a parameter
    private val spaceBetweenStars: Int  // Add spaceBetweenStars as a parameter
) : ListAdapter<RecipeCard, RecipeAdapter.RecipeViewHolder>(RecipeDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val binding = ItemRecipeCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecipeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val recipe = getItem(position)

        // Call bind() with custom size and space
        holder.bind(recipe, starSize, spaceBetweenStars)
    }

    inner class RecipeViewHolder(private val binding: ItemRecipeCardBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(recipe: RecipeCard,starSize: Int, spaceBetweenStars: Int) {
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
            // Clear the existing stars in the layout
            binding.starRatingLayout.removeAllViews()
            // Add full stars to the layout
            for (i in 0 until fullStars) {
                val filledStar = ImageView(binding.root.context)
                filledStar.setImageResource(R.drawable.ic_star_filled) // Full star drawable
                val layoutParams = LinearLayout.LayoutParams(starSize, starSize)
                layoutParams.setMargins(0, 0, spaceBetweenStars, 0) // Set space between stars
                filledStar.layoutParams = layoutParams
                binding.starRatingLayout.addView(filledStar)
            }

            // Add half star if needed
            if (hasHalfStar) {
                val halfStar = ImageView(binding.root.context)
                halfStar.setImageResource(R.drawable.ic_star_half) // Half star drawable
                val layoutParams = LinearLayout.LayoutParams(starSize, starSize)
                layoutParams.setMargins(0, 0, spaceBetweenStars, 0) // Set space between stars
                halfStar.layoutParams = layoutParams
                binding.starRatingLayout.addView(halfStar)
            }

            // Add empty stars
            for (i in 0 until emptyStars) {
                val emptyStar = ImageView(binding.root.context)
                emptyStar.setImageResource(R.drawable.ic_star_empty) // Empty star drawable
                val layoutParams = LinearLayout.LayoutParams(starSize, starSize)
                layoutParams.setMargins(0, 0, spaceBetweenStars, 0) // Set space between stars
                emptyStar.layoutParams = layoutParams
                binding.starRatingLayout.addView(emptyStar)
            }


            binding.recipeRatingCount.text = "(${recipe.ratingCount})"

            // Handle the main image (first image in the list of image URLs)
            val mainImageUrl = recipe.imageUrls?.firstOrNull() // First image
            Glide.with(binding.root.context)
                .load(mainImageUrl)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.error_image)
                .into(binding.recipeImage)

            // Handle item click to navigate to recipe details
            binding.root.setOnClickListener {
                onClick(recipe)
            }
            // Handle additional images inside the HorizontalScrollView
            loadImagesIntoHorizontalScrollView(recipe.imageUrls?.drop(1)) // All images except the first one
        }
        private fun loadImagesIntoHorizontalScrollView(imageUrls: List<String>?) {
            val imageLayout = binding.imageLayout  // LinearLayout inside HorizontalScrollView
            imageLayout.removeAllViews()  // Clear any existing images

            // If imageUrls is not null or empty, load images
            imageUrls?.forEach { url ->
                val imageView = ImageView(binding.root.context)
                val layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
                )
                imageView.layoutParams = layoutParams

                Glide.with(binding.root.context)
                    .load(url)
                    .placeholder(R.drawable.placeholder)  // Optional placeholder
                    .error(R.drawable.error_image)  // Optional error image
                    .into(imageView)

                imageLayout.addView(imageView)  // Add the ImageView to the layout
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



