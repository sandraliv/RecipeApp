package com.hi.recipeapp.ui.myrecipes

import android.annotation.SuppressLint
import android.view.GestureDetector
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import com.hi.recipeapp.classes.UserRecipeCard
import android.view.LayoutInflater
import android.view.MotionEvent
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.hi.recipeapp.R
import com.hi.recipeapp.databinding.ItemUserRecipeCardBinding

class UserRecipeAdapter(private val onClick: (UserRecipeCard) -> Unit, private val onDeleteClick: (recipeId: Int) -> Unit) : ListAdapter<UserRecipeCard, UserRecipeAdapter.UserRecipeViewHolder>(UserRecipeDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserRecipeViewHolder {
        val binding = ItemUserRecipeCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserRecipeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserRecipeViewHolder, position: Int) {
        val userRecipe = getItem(position) // Use getItem() to get the data
        holder.bind(userRecipe)
    }

    inner class UserRecipeViewHolder(private val binding: ItemUserRecipeCardBinding) :

        RecyclerView.ViewHolder(binding.root) {
        private var currentIndex = 0

        fun bind(userRecipe: UserRecipeCard) {


            binding.recipeName.text = userRecipe.title
            binding.recipeDescription.text = userRecipe.description

            // Set the click listener to pass the UserRecipeCard to the next screen
            binding.root.setOnClickListener {
                onClick(userRecipe)  // Passing the full UserRecipeCard object
            }
            // Handle image click to navigate to recipe details (image click)
            binding.imageSwitcher.setOnClickListener {
                onClick(userRecipe)
            }

            binding.deleteUserRecipe.setOnClickListener {
                AlertDialog.Builder(binding.root.context)
                    .setTitle("Delete Recipe")
                    .setMessage("Are you sure you want to delete \"${userRecipe.title}\"?")
                    .setPositiveButton("Yes") { _, _ ->
                        onDeleteClick(userRecipe.id)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }

            // Handle image switching using ImageSwitcher and gesture detector
            loadImagesIntoImageSwitcher(userRecipe.imageUrls) // Load the images into ImageSwitcher
        }
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
    }



    // DiffUtil callback to optimize list updates
    class UserRecipeDiffCallback : DiffUtil.ItemCallback<UserRecipeCard>() {
        override fun areItemsTheSame(oldItem: UserRecipeCard, newItem: UserRecipeCard): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: UserRecipeCard, newItem: UserRecipeCard): Boolean {
            return oldItem == newItem
        }
    }
}
