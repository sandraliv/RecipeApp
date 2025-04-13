package com.hi.recipeapp.ui.home

import android.annotation.SuppressLint
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
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
    private val onDeleteClick: (recipeId: Int) -> Unit,
    private val onEditClick: (recipeId: Int) -> Unit,
    private val starSize: Int,  // Add starSize as a parameter
    private val spaceBetweenStars: Int,  // Add spaceBetweenStars as a parameter
    private val isAdmin: Boolean

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

        /**
         * Binds the recipe data to the UI components.
         *
         * @param recipe The recipe card data to bind to the views.
         * @param starSize The size of the stars used to display the rating.
         * @param spaceBetweenStars The space between the stars used to display the rating.
         */
        fun bind(recipe: RecipeCard, starSize: Int, spaceBetweenStars: Int) {
            if(isAdmin) {
                binding.deleteRecipe.visibility = View.VISIBLE
                binding.editRecipe.visibility = View.VISIBLE
            }

            binding.editRecipe.setOnClickListener {
                onEditClick(recipe.id)
            }
            binding.deleteRecipe.setOnClickListener {
                AlertDialog.Builder(binding.root.context)
                    .setTitle("Delete Recipe")
                    .setMessage("Are you sure you want to delete \"${recipe.title}\"?")
                    .setPositiveButton("Yes") { _, _ ->
                        onDeleteClick(recipe.id)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }


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


        /**
         * Loads images into the ImageSwitcher with swipe functionality.
         *
         * @param imageUrls List of image URLs to be displayed in the ImageSwitcher.
         */
        @SuppressLint("ClickableViewAccessibility")
        private fun loadImagesIntoImageSwitcher(imageUrls: List<String>?) {

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


        /**
         * Loads an image into the ImageSwitcher.
         *
         * @param imageUrl The URL of the image to load.
         */
        private fun loadImage(imageUrl: String?) {
            // Check if the imageUrl is null or empty
            if (imageUrl.isNullOrEmpty()) {
                // If the URL is null or empty, load the placeholder image
                Glide.with(binding.root.context)
                    .load(R.drawable.placeholder) // Placeholder image
                    .transform(CenterCrop())
                    .into(binding.imageSwitcher.currentView as ImageView)
            } else {
                // If the imageUrl is valid, load it
                Glide.with(binding.root.context)
                    .load(imageUrl)
                    .transform(CenterCrop())
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.error_image)
                    .into(binding.imageSwitcher.currentView as ImageView)
            }
        }

        /**
         * Displays the next image in the list of images.
         *
         * @param imageUrls The list of image URLs.
         */
        private fun showNextImage(imageUrls: List<String>) {
            currentIndex = (currentIndex + 1) % imageUrls.size
            loadImage(imageUrls[currentIndex])
        }

        /**
         * Displays the previous image in the list of images.
         *
         * @param imageUrls The list of image URLs.
         */
        private fun showPreviousImage(imageUrls: List<String>) {
            currentIndex = if (currentIndex - 1 < 0) {
                imageUrls.size - 1
            } else {
                currentIndex - 1
            }
            loadImage(imageUrls[currentIndex])
        }

        /**
         * Updates the visibility of the heart buttons based on the favorite status of the recipe.
         *
         * @param recipe The recipe for which the heart button visibility needs to be updated.
         */
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

    /**
     * A DiffUtil callback used for efficiently comparing and updating a list of RecipeCards.
     */
    class RecipeDiffCallback : DiffUtil.ItemCallback<RecipeCard>() {
        /**
         * Checks if two items are the same.
         *
         * @param oldItem The old recipe item.
         * @param newItem The new recipe item.
         * @return True if the items are the same, false otherwise.
         */
        override fun areItemsTheSame(oldItem: RecipeCard, newItem: RecipeCard): Boolean {
            return oldItem.id == newItem.id
        }
        /**
         * Checks if the contents of two items are the same.
         *
         * @param oldItem The old recipe item.
         * @param newItem The new recipe item.
         * @return True if the contents of the items are the same, false otherwise.
         */
        override fun areContentsTheSame(oldItem: RecipeCard, newItem: RecipeCard): Boolean {
            return oldItem == newItem
        }
    }
}
