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
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.hi.recipeapp.R
import com.hi.recipeapp.databinding.ItemRecipeCardBinding
import com.hi.recipeapp.classes.RecipeCard
class RecipeAdapter(
    private val onClick: (RecipeCard) -> Unit,
    private val onFavoriteClick: (RecipeCard, Boolean) -> Unit,
    private val starSize: Int,
    private val spaceBetweenStars: Int
) : ListAdapter<RecipeCard, RecipeAdapter.RecipeViewHolder>(RecipeDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        // Inflate the layout using the binding object
        val binding = ItemRecipeCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        // Initialize the RecipeImageAdapter only if it hasn't been set
        val viewPager = binding.viewPagerImages
        if (viewPager.adapter == null) {
            val adapter = RecipeImageAdapter(binding.root.context, emptyList(),false) // Initially set empty list
            viewPager.adapter = adapter
        }

        return RecipeViewHolder(binding, viewPager.adapter as RecipeImageAdapter)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val recipe = getItem(position)
        val isFullRecipeView = false // Example flag for distinguishing between views
        holder.bind(recipe, starSize, spaceBetweenStars, isFullRecipeView)

        // Update images after binding the data
        val imageUrls = recipe.imageUrls ?: emptyList()
        holder.viewPagerAdapter.updateImages(imageUrls)  // Update image list for ViewPager
    }

    inner class RecipeViewHolder(
        private val binding: ItemRecipeCardBinding,
        val viewPagerAdapter: RecipeImageAdapter
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(recipe: RecipeCard, starSize: Int, spaceBetweenStars: Int, isFullRecipeView: Boolean) {
            updateHeartButtonVisibility(recipe)

            binding.emptyHeartButton.setOnClickListener {
                recipe.isFavoritedByUser = true
                onFavoriteClick(recipe, true)
            }

            binding.filledHeartButton.setOnClickListener {
                recipe.isFavoritedByUser = false
                onFavoriteClick(recipe, false)
            }

            binding.recipeName.text = recipe.title
            binding.recipeDescription.text = recipe.description

            // Handle star ratings
            val fullStars = recipe.averageRating.toInt()
            val hasHalfStar = recipe.averageRating % 1 >= 0.5
            val emptyStars = 5 - fullStars - if (hasHalfStar) 1 else 0
            updateStarRating(fullStars, hasHalfStar, emptyStars, starSize, spaceBetweenStars)

            // Handle images in ViewPager2 (Update image list)
            val imageUrls = recipe.imageUrls ?: emptyList()
            viewPagerAdapter.updateImages(imageUrls)

            binding.root.setOnClickListener { onClick(recipe) }
        }

        private fun updateHeartButtonVisibility(recipe: RecipeCard) {
            if (recipe.isFavoritedByUser) {
                binding.filledHeartButton.visibility = View.VISIBLE
                binding.emptyHeartButton.visibility = View.GONE
            } else {
                binding.filledHeartButton.visibility = View.GONE
                binding.emptyHeartButton.visibility = View.VISIBLE
            }
        }

        private fun updateStarRating(fullStars: Int, hasHalfStar: Boolean, emptyStars: Int, starSize: Int, spaceBetweenStars: Int) {
            binding.starRatingLayout.removeAllViews()

            // Add filled stars
            for (i in 0 until fullStars) {
                val filledStar = ImageView(binding.root.context).apply {
                    setImageResource(R.drawable.ic_star_filled)
                    layoutParams = LinearLayout.LayoutParams(starSize, starSize).apply {
                        setMargins(0, 0, spaceBetweenStars, 0)
                    }
                }
                binding.starRatingLayout.addView(filledStar)
            }

            // Add half star if needed
            if (hasHalfStar) {
                val halfStar = ImageView(binding.root.context).apply {
                    setImageResource(R.drawable.ic_star_half)
                    layoutParams = LinearLayout.LayoutParams(starSize, starSize).apply {
                        setMargins(0, 0, spaceBetweenStars, 0)
                    }
                }
                binding.starRatingLayout.addView(halfStar)
            }

            // Add empty stars
            for (i in 0 until emptyStars) {
                val emptyStar = ImageView(binding.root.context).apply {
                    setImageResource(R.drawable.ic_star_empty)
                    layoutParams = LinearLayout.LayoutParams(starSize, starSize).apply {
                        setMargins(0, 0, spaceBetweenStars, 0)
                    }
                }
                binding.starRatingLayout.addView(emptyStar)
            }
        }
    }

    class RecipeDiffCallback : DiffUtil.ItemCallback<RecipeCard>() {
        override fun areItemsTheSame(oldItem: RecipeCard, newItem: RecipeCard): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: RecipeCard, newItem: RecipeCard): Boolean {
            return oldItem == newItem
        }
    }
}
