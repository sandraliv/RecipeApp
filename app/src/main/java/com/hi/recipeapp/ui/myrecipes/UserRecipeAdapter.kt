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
    /**
     * Creates and returns a new ViewHolder for the user recipe item.
     *
     * @param parent The parent ViewGroup that will hold the ViewHolder.
     * @param viewType The view type of the item being created.
     * @return A new instance of UserRecipeViewHolder.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserRecipeViewHolder {
        val binding = ItemUserRecipeCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserRecipeViewHolder(binding)
    }
    /**
     * Binds data to the ViewHolder at the specified position.
     *
     * @param holder The ViewHolder to bind data to.
     * @param position The position in the list of items.
     */
    override fun onBindViewHolder(holder: UserRecipeViewHolder, position: Int) {
        val userRecipe = getItem(position) // Use getItem() to get the data
        holder.bind(userRecipe)
    }
    /**
     * ViewHolder that holds the individual user recipe item view and binds data to it.
     */
    inner class UserRecipeViewHolder(private val binding: ItemUserRecipeCardBinding) :

        RecyclerView.ViewHolder(binding.root) {
        private var currentIndex = 0
        /**
         * Binds the user recipe data to the item view and sets up click listeners.
         *
         * @param userRecipe The UserRecipeCard object containing recipe data.
         */
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

        /**
         * Loads the recipe images into an ImageSwitcher and sets up swipe gestures for image switching.
         *
         * @param imageUrls The list of image URLs to be loaded into the ImageSwitcher.
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

                binding.imageSwitcher.setOnTouchListener { _, event ->
                    gestureDetector.onTouchEvent(event)
                    true
                }
            }
        }
        /**
         * Loads the image URL into the ImageSwitcher.
         *
         * @param imageUrl The URL of the image to be loaded.
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
         * Displays the next image in the ImageSwitcher by updating the current index.
         *
         * @param imageUrls The list of image URLs.
         */
        private fun showNextImage(imageUrls: List<String>) {
            currentIndex = (currentIndex + 1) % imageUrls.size
            loadImage(imageUrls[currentIndex])
        }
        /**
         * Displays the previous image in the ImageSwitcher by updating the current index.
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
    }



    /**
     * DiffUtil callback to optimize list updates in the RecyclerView.
     * Compares old and new user recipe items to determine if they are the same.
     */
    class UserRecipeDiffCallback : DiffUtil.ItemCallback<UserRecipeCard>() {
        /**
         * Compares if two items represent the same recipe (based on their unique ID).
         *
         * @param oldItem The old item in the list.
         * @param newItem The new item to compare.
         * @return True if the items are the same.
         */
        override fun areItemsTheSame(oldItem: UserRecipeCard, newItem: UserRecipeCard): Boolean {
            return oldItem.id == newItem.id
        }
        /**
         * Compares if the contents of two items are the same.
         *
         * @param oldItem The old item in the list.
         * @param newItem The new item to compare.
         * @return True if the contents are the same.
         */
        override fun areContentsTheSame(oldItem: UserRecipeCard, newItem: UserRecipeCard): Boolean {
            return oldItem == newItem
        }
    }
}
