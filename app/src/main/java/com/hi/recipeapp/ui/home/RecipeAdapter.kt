package com.hi.recipeapp.ui.home

import android.annotation.SuppressLint
import android.util.Log
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
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
        holder.bind(recipe, starSize, spaceBetweenStars)
    }

    inner class RecipeViewHolder(private val binding: ItemRecipeCardBinding) : RecyclerView.ViewHolder(binding.root) {
        private var currentIndex = 0  // Keep track of the current image index

        fun bind(recipe: RecipeCard, starSize: Int, spaceBetweenStars: Int) {
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
            // Handle item click to navigate to recipe details
            binding.root.setOnClickListener {
                onClick(recipe)
            }
            // Handle image click to navigate to recipe details (image click)
            binding.imageSwitcher.setOnClickListener {
                onClick(recipe)
            }

            // Handle image switching using ImageSwitcher and gesture detector
            loadImagesIntoImageSwitcher(recipe.imageUrls) // Load the images into ImageSwitcher
        }

        @SuppressLint("ClickableViewAccessibility")
        private fun loadImagesIntoImageSwitcher(imageUrls: List<String>?) {
            val imageSwitcher = binding.imageSwitcher

            if (!imageUrls.isNullOrEmpty()) {
                currentIndex = 0  // Initialize the image index
                loadImage(imageUrls[currentIndex])  // Load the first image

                // Setup Gesture Detector for swipe actions
                val gestureDetector = GestureDetector(binding.root.context, object : GestureDetector.OnGestureListener {
                    override fun onDown(e: MotionEvent): Boolean {
                        return true
                    }

                    override fun onFling(
                        e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float
                    ): Boolean {
                        val SWIPE_THRESHOLD = 100
                        val SWIPE_VELOCITY_THRESHOLD = 100

                        // Detecting horizontal swipe (left/right)
                        if (e1 != null) {
                            if (Math.abs(e1.y - e2.y) < SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                                if (e1.x - e2.x > SWIPE_THRESHOLD) { // Swiped left
                                    showNextImage(imageUrls)
                                } else if (e2.x - e1.x > SWIPE_THRESHOLD) { // Swiped right
                                    showPreviousImage(imageUrls)
                                }
                            }
                        }
                        return true
                    }

                    override fun onLongPress(e: MotionEvent) {}
                    override fun onScroll(
                        e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float
                    ): Boolean {
                        return true
                    }

                    override fun onShowPress(e: MotionEvent) {}
                    override fun onSingleTapUp(e: MotionEvent): Boolean {
                        return true
                    }
                })

                binding.imageSwitcher.setOnTouchListener { v, event ->
                    gestureDetector.onTouchEvent(event)
                    true
                }
            }
        }

        private fun loadImage(imageUrl: String) {
            Glide.with(binding.root.context)
                .load(imageUrl)
                .transform(CenterCrop())
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.error_image)
                .into(binding.imageSwitcher.currentView as ImageView)
        }

        private fun showNextImage(imageUrls: List<String>) {
            currentIndex = (currentIndex + 1) % imageUrls.size
            loadImage(imageUrls[currentIndex])
        }

        private fun showPreviousImage(imageUrls: List<String>) {
            currentIndex = if (currentIndex - 1 < 0) {
                imageUrls.size - 1
            } else {
                currentIndex - 1
            }
            loadImage(imageUrls[currentIndex])
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
