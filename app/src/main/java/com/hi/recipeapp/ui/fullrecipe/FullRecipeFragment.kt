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
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.hi.recipeapp.R
import com.hi.recipeapp.classes.FullRecipe
import com.hi.recipeapp.databinding.FragmentFullRecipeBinding
import com.hi.recipeapp.ui.home.RecipeImageAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FullRecipeFragment : Fragment() {

    private val fullRecipeViewModel: FullRecipeViewModel by viewModels() // Get ViewModel instance
    private lateinit var binding: FragmentFullRecipeBinding

    private val args: FullRecipeFragmentArgs by navArgs()
    private val recipeId: Int get() = args.recipeId

    private var selectedRating = 0
    private val isFullRecipeView = true
    private lateinit var imageAdapter: RecipeImageAdapter // Declare the adapter once

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentFullRecipeBinding.inflate(inflater, container, false)

        // Initialize the image adapter only once
        imageAdapter = RecipeImageAdapter(binding.root.context, emptyList(), isFullRecipeView)

        // Set the adapter to ViewPager2
        binding.viewPagerImages.adapter = imageAdapter

        fullRecipeViewModel.fetchRecipeById(recipeId)

        // Observe loading state
        fullRecipeViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.contentLayout.visibility = if (isLoading) View.GONE else View.VISIBLE
        }

        // Observe recipe data
        fullRecipeViewModel.recipe.observe(viewLifecycleOwner) { recipe ->
            recipe?.let {
                bindRecipeData(it)
                binding.progressBar.visibility = View.GONE
                binding.contentLayout.visibility = View.VISIBLE
            } ?: run {
                // Handle null recipe case here (optional)
                Toast.makeText(requireContext(), "Failed to load recipe", Toast.LENGTH_SHORT).show()
            }
        }

        // Observe error messages
        fullRecipeViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            }
        }

        return binding.root
    }

    private fun bindRecipeData(recipe: FullRecipe) {
        binding.titleTextView.text = recipe.title
        binding.descriptionTextView.text = recipe.description

        // Set rating stars
        setRatingStars(recipe.averageRating)

        binding.recipeRatingCount.text = "(${recipe.ratingCount})"
        updateHeartButtonVisibility(recipe)

        // Handle heart button click for favorite
        binding.emptyHeartButton.setOnClickListener {
            recipe.isFavoritedByUser = true
            updateHeartButtonVisibility(recipe)
            fullRecipeViewModel.updateFavoriteStatus(recipe.id, true)
        }

        binding.filledHeartButton.setOnClickListener {
            recipe.isFavoritedByUser = false
            updateHeartButtonVisibility(recipe)
            fullRecipeViewModel.updateFavoriteStatus(recipe.id, false)
        }

        // Load ingredients dynamically
        recipe.ingredients.forEach { (ingredientName, ingredientQuantity) ->
            val formattedIngredientName = ingredientName.replace("_", " ")

            val tableRow = TableRow(requireContext()).apply {
                gravity = Gravity.CENTER_VERTICAL
                setPadding(0, 8, 0, 8)
            }

            val measurementTextView = TextView(requireContext()).apply {
                text = ingredientQuantity
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                gravity = Gravity.START
                setPadding(16, 0, 16, 0)
            }

            val ingredientNameTextView = TextView(requireContext()).apply {
                text = formattedIngredientName
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                gravity = Gravity.START
                setPadding(16, 0, 16, 0)
            }

            val ingredientCheckBox = CheckBox(requireContext()).apply {
                setOnCheckedChangeListener { _, isChecked ->
                    val strikeThroughFlag = if (isChecked) Paint.STRIKE_THRU_TEXT_FLAG else 0
                    measurementTextView.paintFlags = strikeThroughFlag
                    ingredientNameTextView.paintFlags = strikeThroughFlag
                }
            }

            tableRow.addView(ingredientCheckBox)
            tableRow.addView(measurementTextView)
            tableRow.addView(ingredientNameTextView)

            binding.ingredientsLayout.addView(tableRow)
        }

        // Format and load instructions
        val instructions = recipe.instructions.split(".")
        val formattedInstructions = StringBuilder()
        instructions.forEachIndexed { index, instruction ->
            if (instruction.trim().isNotEmpty()) {
                formattedInstructions.append("${index + 1}. ${instruction.trim()}. \n\n")
            }
        }

        binding.instructionsTextView.text = formattedInstructions.toString()
        binding.tagsTextView.text = recipe.tags.joinToString(", ") { it.getDisplayName() }
        binding.categoriesTextView.text = recipe.categories.joinToString(", ") { it.getDisplayName() }

        // Load images into ViewPager2
        loadImagesIntoViewPager(recipe.imageUrls)
    }

    private fun loadImagesIntoViewPager(imageUrls: List<String>?) {
        // Just update the images in the existing adapter
        imageAdapter.updateImages(imageUrls ?: emptyList())
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
        binding.ratingStarsLayout.visibility = View.VISIBLE
        binding.ratingTextView.visibility = View.VISIBLE
        binding.ratingStarsLayout.removeAllViews()

        binding.ratingTextView.text = "Rate this recipe:"

        for (i in 1..5) {
            val star = ImageView(requireContext()).apply {
                setImageResource(R.drawable.ic_star_empty)

                setOnClickListener {
                    selectedRating = i
                    updateStars()
                    updateRatingText()
                    showSubmitButton()
                }

                setOnTouchListener { _, event ->
                    when (event.action) {
                        MotionEvent.ACTION_MOVE -> {
                            for (j in 1..5) {
                                val starToUpdate = binding.ratingStarsLayout.getChildAt(j - 1) as ImageView
                                if (j <= i) {
                                    starToUpdate.setImageResource(R.drawable.ic_star_filled)
                                } else {
                                    starToUpdate.setImageResource(R.drawable.ic_star_empty)
                                }
                            }
                        }
                        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                            updateStars()
                        }
                        MotionEvent.ACTION_DOWN -> {
                            performClick()
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

    private fun updateStars() {
        for (i in 0 until binding.ratingStarsLayout.childCount) {
            val star = binding.ratingStarsLayout.getChildAt(i) as ImageView
            if (i < selectedRating) {
                star.setImageResource(R.drawable.ic_star_filled)
            } else {
                star.setImageResource(R.drawable.ic_star_empty)
            }
        }
    }

    private fun updateRatingText() {
        if (selectedRating > 0) {
            binding.ratingTextView.text = "Rate this recipe: $selectedRating stars"
        }
    }

    private fun showSubmitButton() {
        binding.rateRecipeButton.text = "Submit Rating"
        binding.rateRecipeButton.setOnClickListener {
            submitRating()
        }
    }

    private fun submitRating() {
        fullRecipeViewModel.rateRecipe(recipeId, selectedRating)
        binding.rateRecipeButton.isEnabled = false
        Toast.makeText(requireContext(), "You rated this recipe $selectedRating stars", Toast.LENGTH_SHORT).show()
    }
}
