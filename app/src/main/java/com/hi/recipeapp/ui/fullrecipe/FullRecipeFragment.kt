package com.hi.recipeapp.ui.fullrecipe

import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentFullRecipeBinding.inflate(inflater, container, false)
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

        fullRecipeViewModel.fetchRecipeById(recipeId)
        fullRecipeViewModel.recipe.observe(viewLifecycleOwner) { recipe ->
            Log.d("FullRecipeFragment", "Observer triggered, recipe: $recipe")
            recipe?.let {
                bindRecipeData(it)
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

        // Load the image using Glide with placeholder and error image
        Glide.with(binding.root.context)
            .load(recipe.imageUrl)
            .placeholder(R.drawable.placeholder)  // Placeholder image
            .error(R.drawable.error_image)        // Error image
            .into(binding.imageView)

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
        binding.ratingStarsLayout.removeAllViews()

        for (i in 1..5) {
            val star = ImageView(requireContext()).apply {
                setImageResource(R.drawable.ic_star_empty)  // Initially empty star
                setOnClickListener {
                    updateRating(i)  // When clicked, update rating
                }
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }
            binding.ratingStarsLayout.addView(star)
        }
    }
    private fun updateRating(newRating: Int) {
        // Update the backend with the new rating
        fullRecipeViewModel.rateRecipe(recipeId, newRating)

        // Update the UI with the new rating
        binding.recipeRatingStars.text = "⭐".repeat(newRating) // Update the star UI

        // Optionally, hide the stars after a rating is submitted
        binding.ratingStarsLayout.visibility = View.GONE

        // Show a message to the user
        Toast.makeText(context, "You rated this recipe $newRating stars", Toast.LENGTH_SHORT).show()
    }

}
