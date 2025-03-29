package com.hi.recipeapp.ui.fullrecipe

import android.annotation.SuppressLint
import android.graphics.Paint
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.util.TypedValue
import android.view.GestureDetector
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.google.android.material.snackbar.Snackbar
import com.hi.recipeapp.R
import com.hi.recipeapp.classes.FullRecipe
import com.hi.recipeapp.databinding.FragmentFullRecipeBinding
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
class FullRecipeFragment : Fragment() {

    private val fullRecipeViewModel: FullRecipeViewModel by viewModels() // Get ViewModel instance
    private lateinit var binding: FragmentFullRecipeBinding

    // Safe Args: Retrieve arguments passed to the fragment
    private val args: FullRecipeFragmentArgs by navArgs()
    private val recipeId: Int get() = args.recipeId

    private var selectedRating = 0
    private var currentIndex = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        fullRecipeViewModel.fetchRecipeById(recipeId)
        binding = FragmentFullRecipeBinding.inflate(inflater, container, false)


        // Initial visibility settings
        binding.contentLayout.visibility = View.GONE
        binding.progressBar.visibility = View.VISIBLE

        // Setup gesture detector for image swipe
        setupGestureDetector()

        // Observe the loading state
        fullRecipeViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                binding.progressBar.visibility = View.VISIBLE
                binding.contentLayout.visibility = View.GONE
            } else {
                binding.progressBar.visibility = View.GONE
                binding.contentLayout.visibility = View.VISIBLE
            }
        }

        fullRecipeViewModel.recipe.observe(viewLifecycleOwner) { recipe ->
            Log.d("FullRecipeFragment", "Observer triggered, recipe: $recipe")
            recipe?.let {
                bindRecipeData(it)
                // Once data is fetched, show content and hide ProgressBar
                binding.progressBar.visibility = View.GONE
                binding.contentLayout.visibility = View.VISIBLE
            }
        }

        // Observe error messages
        fullRecipeViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            }
        }

        // Observe the favorite action message LiveData
        fullRecipeViewModel.favoriteActionMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_SHORT).show()
            }
        }

        // Handle rate recipe button click
        binding.rateRecipeButton.setOnClickListener {
            showRatingStars()
        }

        return binding.root
    }


    @SuppressLint("ClickableViewAccessibility")
    private fun setupGestureDetector() {
        val gestureDetector = GestureDetector(requireContext(), object : GestureDetector.OnGestureListener {

            // onDown is required for gesture detection, even if you don't use it
            override fun onDown(e: MotionEvent): Boolean {
                return true
            }

            // onFling should now work properly with this method signature
            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                val SWIPE_THRESHOLD = 100
                val SWIPE_VELOCITY_THRESHOLD = 100

                // Detecting horizontal swipe (left/right)
                if (e1 != null) {
                    if (Math.abs(e1.y - e2.y) < SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (e1.x - e2.x > SWIPE_THRESHOLD) { // Swiped left
                            showNextImage()
                        } else if (e2.x - e1.x > SWIPE_THRESHOLD) { // Swiped right
                            showPreviousImage()
                        }
                    }
                }
                return true
            }

            // Implement other required methods from the interface, even if you don't use them
            override fun onLongPress(e: MotionEvent) {}
            override fun onScroll(
                e1: MotionEvent?,
                e2: MotionEvent,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                return true
            }
            override fun onShowPress(e: MotionEvent) {}
            override fun onSingleTapUp(e: MotionEvent): Boolean {
                return true
            }
        })

        // Set the GestureDetector to the ImageSwitcher
        binding.imageSwitcher.setOnTouchListener { v, event ->
            v.performClick()  // Ensure accessibility by triggering performClick
            gestureDetector.onTouchEvent(event)  // Handle the gesture event
        }
    }




    private fun bindRecipeData(recipe: FullRecipe) {
        binding.titleTextView.text = recipe.title
        binding.descriptionTextView.text = recipe.description
        setRatingStars(recipe.averageRating)
        binding.recipeRatingCount.text = "(${recipe.ratingCount})"

        // Handle favorite status for heart button
        updateHeartButtonVisibility(recipe)

        // Handle empty heart button click (add to favorites)
        binding.emptyHeartButton.setOnClickListener {
            recipe.isFavoritedByUser = true
            updateHeartButtonVisibility(recipe)
            fullRecipeViewModel.updateFavoriteStatus(recipe.id, true)
        }

        // Handle filled heart button click (remove from favorites)
        binding.filledHeartButton.setOnClickListener {
            recipe.isFavoritedByUser = false
            updateHeartButtonVisibility(recipe)
            fullRecipeViewModel.updateFavoriteStatus(recipe.id, false)
        }

        recipe.ingredients.forEach { (ingredientName, ingredientQuantity) ->
            val formattedIngredientName = ingredientName
                .replace("_", " ")  // Replace underscores with spaces
                .split(" ")  // Split the string into words by spaces
                .joinToString(" ") { it.replaceFirstChar { char -> char.uppercase(Locale.ROOT) } }

            // Create a TableRow to hold the components for each ingredient
            val tableRow = TableRow(requireContext()).apply {
                gravity = Gravity.CENTER_VERTICAL
                setPadding(0, 8, 0, 8)
                layoutParams = TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT
                )
            }

            // Create the measurement TextView
            val measurementTextView = TextView(requireContext()).apply {
                text = ingredientQuantity
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                gravity = Gravity.CENTER
                setPadding(16, 0, 16, 0)
                layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f) // Distribute space equally
            }

            // Create the ingredient name TextView
            val ingredientNameTextView = TextView(requireContext()).apply {
                text = formattedIngredientName
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                gravity = Gravity.START
                setPadding(16, 0, 16, 0)
                layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 2f) // Ingredient takes more space
                maxLines = 2  // Allows wrapping into 2 lines if needed
                ellipsize = TextUtils.TruncateAt.END  // Handle overflow text gracefully
            }

            // Create the CheckBox for the ingredient
            val ingredientCheckBox = CheckBox(requireContext()).apply {
                setOnCheckedChangeListener { buttonView, isChecked ->
                    // Apply strike-through effect to the entire row (checkbox, measurement, and name)
                    val strikeThroughFlag = if (isChecked) Paint.STRIKE_THRU_TEXT_FLAG else 0
                    measurementTextView.paintFlags = strikeThroughFlag
                    ingredientNameTextView.paintFlags = strikeThroughFlag
                }
                setPadding(16, 0, 16, 0)

                // Use WRAP_CONTENT for CheckBox and avoid setting weight to 0f
                layoutParams = TableRow.LayoutParams(
                    TableRow.LayoutParams.WRAP_CONTENT,
                    TableRow.LayoutParams.WRAP_CONTENT
                ).apply {
                    gravity = Gravity.CENTER
                }
            }



            tableRow.addView(ingredientCheckBox)
            tableRow.addView(measurementTextView)
            tableRow.addView(ingredientNameTextView)
            binding.ingredientsLayout.addView(tableRow)
        }

        // Set instructions with numbering
        val instructions = recipe.instructions.split(".")

        val formattedInstructions = StringBuilder()
        instructions.forEachIndexed { index, instruction ->
            // Ignore empty strings caused by trailing periods or extra spaces
            if (instruction.trim().isNotEmpty()) {

                // Add number and instruction step
                formattedInstructions.append("${index + 1}. ${instruction.trim()}. \n\n")
            }
        }


        binding.instructionsTextView.text = formattedInstructions.toString()


        binding.tagsTextView.text = recipe.tags.joinToString(", ") { it.getDisplayName() }

        binding.categoriesTextView.text = recipe.categories.joinToString(", ") { it.getDisplayName() }

        loadImagesIntoImageSwitcher(recipe.imageUrls)
    }

    private fun loadImagesIntoImageSwitcher(imageUrls: List<String>?) {
        // Reference to the ImageSwitcher
        val imageSwitcher = binding.imageSwitcher
        val context = binding.root.context

        if (!imageUrls.isNullOrEmpty()) {
            currentIndex = 0
            loadImage(imageUrls[currentIndex])

            // Optionally add swipe listener if needed (handled by gesture detector)
        }
    }

    private fun loadImage(imageUrl: String) {
        val context = requireContext()

        // Load the image using Glide
        Glide.with(context)
            .load(imageUrl)
            .transform(CenterCrop())  // Optional transformation
            .placeholder(R.drawable.placeholder)
            .error(R.drawable.error_image) // Handle error image gracefully
            .into(binding.imageSwitcher.currentView as ImageView)  // ImageSwitcher is the target view
    }

    private fun showNextImage() {
        val imageUrls = fullRecipeViewModel.recipe.value?.imageUrls ?: return
        if (imageUrls.isNotEmpty()) {
            currentIndex = (currentIndex + 1) % imageUrls.size
            loadImage(imageUrls[currentIndex])
        }
    }

    private fun showPreviousImage() {
        val imageUrls = fullRecipeViewModel.recipe.value?.imageUrls ?: return
        if (imageUrls.isNotEmpty()) {
            currentIndex = if (currentIndex - 1 < 0) {
                imageUrls.size - 1
            } else {
                currentIndex - 1
            }
            loadImage(imageUrls[currentIndex])
        }
    }

    private fun setRatingStars(averageRating: Double) {
        val fullStars = averageRating.toInt()
        val hasHalfStar = averageRating % 1 >= 0.5
        val emptyStars = 5 - fullStars - if (hasHalfStar) 1 else 0
        val starRating = StringBuilder().apply {
            append("⭐".repeat(fullStars))
            if (hasHalfStar) append("🌟")
            append("☆".repeat(emptyStars))
        }
        binding.recipeRatingStars.text = starRating.toString()
    }

    private fun updateHeartButtonVisibility(recipe: FullRecipe) {
        if (recipe.isFavoritedByUser) {
            binding.filledHeartButton.visibility = View.VISIBLE
            binding.emptyHeartButton.visibility = View.GONE
        } else {
            binding.filledHeartButton.visibility = View.GONE
            binding.emptyHeartButton.visibility = View.VISIBLE
        }
    }

    private fun showRatingStars() {
        // Show the rating stars layout and dynamically add 5 clickable stars
        binding.ratingStarsLayout.visibility = View.VISIBLE
        binding.ratingTextView.visibility = View.VISIBLE  // Show the rating text as well
        binding.ratingStarsLayout.removeAllViews()

        binding.ratingTextView.text = "Rate this recipe:"  // Initially just show "Rate this recipe:"

        for (i in 1..5) {
            val star = ImageView(requireContext()).apply {
                setImageResource(R.drawable.ic_star_empty)  // Initially empty star

                setOnClickListener {
                    selectedRating = i  // When clicked, set selected rating
                    updateStars()  // Update the filled stars
                    updateRatingText()  // Update the rating text
                    showSubmitButton()  // Change the "Rate this recipe" button to "Submit"
                }

                // Adding hover-like effect for touch (shows filled stars)
                setOnTouchListener { v, event ->
                    when (event.action) {
                        MotionEvent.ACTION_MOVE -> {
                            // Fill stars up to the hovered index
                            for (j in 1..5) {
                                val starToUpdate = binding.ratingStarsLayout.getChildAt(j - 1) as ImageView
                                if (j <= i) {
                                    starToUpdate.setImageResource(R.drawable.ic_star_filled) // Filled star
                                } else {
                                    starToUpdate.setImageResource(R.drawable.ic_star_empty) // Empty star
                                }
                            }
                        }
                        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                            // Reset all stars to their current state when the touch ends
                            updateStars()
                        }
                        MotionEvent.ACTION_DOWN -> {
                            // Perform the click when the user taps down
                            performClick() // This is important for accessibility
                        }
                    }
                    true
                }

                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }

            binding.ratingStarsLayout.addView(star)
        }
    }

    // Helper function to update the stars based on selected rating
    private fun updateStars() {
        // Iterate through all child views in the ratingStarsLayout
        for (i in 0 until binding.ratingStarsLayout.childCount) {
            val star = binding.ratingStarsLayout.getChildAt(i) as ImageView
            if (i < selectedRating) {
                star.setImageResource(R.drawable.ic_star_filled) // Set the star as filled
            } else {
                star.setImageResource(R.drawable.ic_star_empty) // Set the star as empty
            }
        }
    }

    private fun updateRatingText() {
        // Update the rating text with the number of stars selected
        if (selectedRating > 0) {
            binding.ratingTextView.text = "Rate this recipe: $selectedRating stars"
        }
    }

    private fun showSubmitButton() {
        // Hide the "Rate this recipe" button and show the "Submit" button
        binding.rateRecipeButton.text = "Submit Rating"
        binding.rateRecipeButton.setOnClickListener {
            submitRating()  // Call the submit function
        }
    }

    private fun submitRating() {
        // Code to handle rating submission (e.g., network call or saving to a database)
        fullRecipeViewModel.rateRecipe(recipeId, selectedRating)

        // Optionally, hide the submit button after the rating is submitted
        binding.rateRecipeButton.isEnabled = false

        // Show a message to the user
        Toast.makeText(requireContext(), "You rated this recipe $selectedRating stars", Toast.LENGTH_SHORT).show()
    }
}
