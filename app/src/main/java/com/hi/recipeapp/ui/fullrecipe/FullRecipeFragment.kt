package com.hi.recipeapp.ui.fullrecipe

import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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


@AndroidEntryPoint
class FullRecipeFragment : Fragment() {

    private val fullRecipeViewModel: FullRecipeViewModel by viewModels() // Get ViewModel instance
    private lateinit var binding: FragmentFullRecipeBinding

    // Safe Args: Retrieve arguments passed to the fragment
    private val args: FullRecipeFragmentArgs by navArgs()
    private val recipeId: Int get() = args.recipeId

    private var selectedRating = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentFullRecipeBinding.inflate(inflater, container, false)

        fullRecipeViewModel.fetchRecipeById(recipeId)

        // Initial visibility settings
        binding.contentLayout.visibility = View.GONE
        binding.progressBar.visibility = View.VISIBLE


        // Observe the loading state
        fullRecipeViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                binding.progressBar.visibility = View.VISIBLE  // Show the ProgressBar
                binding.contentLayout.visibility = View.GONE    // Hide the content layout
            } else {
                binding.progressBar.visibility = View.GONE    // Hide the ProgressBar
                binding.contentLayout.visibility = View.VISIBLE // Show the content layout
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

    private fun bindRecipeData(recipe: FullRecipe) {
        // Bind the recipe data to the UI
        binding.titleTextView.text = recipe.title
        binding.descriptionTextView.text = recipe.description

        // Set the rating stars using a method to generate stars
        setRatingStars(recipe.averageRating)

        // Set the rating count
        binding.recipeRatingCount.text = "(${recipe.ratingCount})"

        // Handle favorite status for heart button
        updateHeartButtonVisibility(recipe)

        // Handle empty heart button click (add to favorites)
        binding.emptyHeartButton.setOnClickListener {
            recipe.isFavoritedByUser = true
            updateHeartButtonVisibility(recipe)
            fullRecipeViewModel.updateFavoriteStatus(recipe.id, true) // Pass only the recipe id
        }

        // Handle filled heart button click (remove from favorites)
        binding.filledHeartButton.setOnClickListener {
            recipe.isFavoritedByUser = false
            updateHeartButtonVisibility(recipe)
            fullRecipeViewModel.updateFavoriteStatus(recipe.id, false) // Pass only the recipe id
        }

        recipe.ingredients.forEach { (ingredientName, ingredientQuantity) ->
            val formattedIngredientName = ingredientName.replace("_", " ") // Replace underscores with spaces

            // Create a TableRow to hold the components for each ingredient
            val tableRow = TableRow(requireContext()).apply {
                gravity = Gravity.CENTER_VERTICAL
                setPadding(0, 8, 0, 8) // Optional padding for each row
            }

            // Create the measurement TextView (for displaying the quantity)
            val measurementTextView = TextView(requireContext()).apply {
                text = ingredientQuantity
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                gravity = Gravity.START  // Align text to the left for the measurements
                setPadding(16, 0, 16, 0) // Adjust the padding between columns
            }

            // Create the ingredient name TextView (for displaying the ingredient name)
            val ingredientNameTextView = TextView(requireContext()).apply {
                text = formattedIngredientName
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                gravity = Gravity.START  // Align text to the left for the ingredient name
                setPadding(16, 0, 16, 0) // Adjust the padding between columns
            }

            // Create the CheckBox for the ingredient
            val ingredientCheckBox = CheckBox(requireContext()).apply {
                setOnCheckedChangeListener { buttonView, isChecked ->
                    // Apply strike-through effect to the entire row (checkbox, measurement, and name)
                    val strikeThroughFlag = if (isChecked) Paint.STRIKE_THRU_TEXT_FLAG else 0
                    measurementTextView.paintFlags = strikeThroughFlag
                    ingredientNameTextView.paintFlags = strikeThroughFlag
                }
            }

            // Add the views to the TableRow
            tableRow.addView(ingredientCheckBox) // First column (checkbox)
            tableRow.addView(measurementTextView)  // Second column (measurement only)
            tableRow.addView(ingredientNameTextView)  // Third column (ingredient name only)

            // Add the TableRow to the TableLayout
            binding.ingredientsLayout.addView(tableRow)  // Add the entire row
        }

        // Set instructions with numbering
        val instructions = recipe.instructions.split(".") // Split by periods (.)

        val formattedInstructions = StringBuilder()
        instructions.forEachIndexed { index, instruction ->
            // Ignore empty strings caused by trailing periods or extra spaces
            if (instruction.trim().isNotEmpty()) {
                // Add number and instruction step
                formattedInstructions.append("${index + 1}. ${instruction.trim()}. \n\n")
            }
        }

        binding.instructionsTextView.text = formattedInstructions.toString()

        // Display tags (if necessary)
        binding.tagsTextView.text = recipe.tags.joinToString(", ") { it.getDisplayName() }
        // Display categories using getDisplayName
        binding.categoriesTextView.text = recipe.categories.joinToString(", ") { it.getDisplayName() }

        // Handle additional images inside the HorizontalScrollView
        loadImagesIntoHorizontalScrollView(recipe.imageUrls) // All images except the first one

    }

    private fun loadImagesIntoHorizontalScrollView(imageUrls: List<String>?) {
        val imageLayout = binding.imageLayout  // LinearLayout inside HorizontalScrollView
        imageLayout.removeAllViews()  // Clear any existing images

        // If imageUrls is not null or empty, load images
        imageUrls?.forEach { url ->
            val imageView = ImageView(binding.root.context)
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,  // Set width to match parent
                binding.root.context.resources.getDimensionPixelSize(R.dimen.full_image_height)  // Set fixed height
            )

            layoutParams.setMargins(8, 0, 8, 0)  // Adjust margin between images
            imageView.layoutParams = layoutParams

            Glide.with(binding.root.context)
                .load(url)
                .transform(CenterCrop())  // Apply centerCrop transformation
                .placeholder(R.drawable.placeholder)  // Optional placeholder
                .error(R.drawable.error_image)  // Optional error image
                .into(imageView)

            imageLayout.addView(imageView)  // Add the ImageView to the layout
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
        // Show the appropriate button based on the recipe's favorite status
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
