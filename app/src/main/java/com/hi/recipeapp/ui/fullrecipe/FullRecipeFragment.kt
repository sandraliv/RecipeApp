package com.hi.recipeapp.ui.fullrecipe

import android.annotation.SuppressLint
import android.app.DatePickerDialog
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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.google.android.material.snackbar.Snackbar
import com.hi.recipeapp.R
import com.hi.recipeapp.classes.FullRecipe
import com.hi.recipeapp.classes.RecipeTag
import com.hi.recipeapp.classes.SessionManager
import com.hi.recipeapp.databinding.FragmentFullRecipeBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class FullRecipeFragment : Fragment() {

    private val fullRecipeViewModel: FullRecipeViewModel by viewModels() // Get ViewModel instance
    private lateinit var binding: FragmentFullRecipeBinding
    @Inject
    lateinit var sessionManager: SessionManager

    // Safe Args: Retrieve arguments passed to the fragment
    private val args: FullRecipeFragmentArgs by navArgs()
    private val recipeId: Int get() = args.recipeId

    private var selectedRating = 0
    private var currentIndex = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFullRecipeBinding.inflate(inflater, container, false)

        // Initial visibility settings
        binding.nestedScrollView.visibility = View.GONE
        binding.contentLayout.visibility = View.GONE
        binding.progressBar.visibility = View.VISIBLE

        // Setup gesture detector for image swipe
        setupGestureDetector()

        fullRecipeViewModel.recipe.observe(viewLifecycleOwner) { recipe ->
            Log.d("FullRecipeFragment", "Observer triggered, recipe: $recipe")
            recipe?.let {
                bindRecipeData(it)
                // Once data is fetched, show content and hide ProgressBar
                binding.progressBar.visibility = View.GONE
                binding.nestedScrollView.visibility = View.VISIBLE
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

        fullRecipeViewModel.calendarSaveStatus.observe(viewLifecycleOwner) { message ->
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }

        binding.saveToCalendarButton.setOnClickListener {
            showDatePickerAndSave()
        }


        // Handle rate recipe button click
        binding.rateRecipeButton.setOnClickListener {
            showRatingStars()
        }

        return binding.root
    }
    /**
     * Opens a DatePicker dialog to save the recipe to the user's calendar.
     */
    private fun showDatePickerAndSave() {
        val today = org.threeten.bp.LocalDate.now()
        val year = today.year
        val month = today.monthValue - 1
        val day = today.dayOfMonth

        val datePickerDialog = DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
            val selectedDate = org.threeten.bp.LocalDate.of(selectedYear, selectedMonth + 1, selectedDay)
            val formattedDate = selectedDate.toString()

            val userId = sessionManager.getUserId()
            val recipeId = this.recipeId

            fullRecipeViewModel.saveRecipeToCalendar(userId, recipeId, formattedDate)
        }, year, month, day)

        datePickerDialog.show()
    }

    /**
     * Initializes the GestureDetector for swipe gestures to change images.
     */
    @SuppressLint("ClickableViewAccessibility")
    private fun setupGestureDetector() {
        val gestureDetector = GestureDetector(requireContext(), object : GestureDetector.OnGestureListener {

            override fun onDown(e: MotionEvent): Boolean {
                return true
            }

            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                val SWIPE_THRESHOLD = 100
                val SWIPE_VELOCITY_THRESHOLD = 100

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

        binding.imageSwitcher.setOnTouchListener { v, event ->
            v.performClick()
            gestureDetector.onTouchEvent(event)
        }
    }

    /**
     * Binds the recipe data to the UI elements.
     *
     * @param recipe The recipe to be displayed.
     */
    private fun bindRecipeData(recipe: FullRecipe) {
        binding.titleTextView.text = recipe.title
        binding.descriptionTextView.text = recipe.description
        setRatingStars(recipe.averageRating)
        binding.recipeRatingCount.text = "(${recipe.ratingCount})"

        updateHeartButtonVisibility(recipe)

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


        viewLifecycleOwner.lifecycleScope.launch {
            delay(40)

            binding.ingredientsLayout.removeAllViews()

            recipe.ingredients.forEach { (ingredientName, quantity) ->
                val formattedIngredientName = ingredientName
                    .replace("_", " ")
                    .split(" ")
                    .joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }

                val tableRow = TableRow(requireContext()).apply {
                    gravity = Gravity.CENTER_VERTICAL
                    setPadding(0, 8, 0, 8)
                    layoutParams = TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.WRAP_CONTENT
                    )
                }

                val measurementTextView = TextView(requireContext()).apply {
                    text = quantity
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                    gravity = Gravity.CENTER
                    setPadding(16, 0, 16, 0)
                    layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
                }

                val ingredientNameTextView = TextView(requireContext()).apply {
                    text = formattedIngredientName
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                    gravity = Gravity.START
                    setPadding(16, 0, 16, 0)
                    layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 2f)
                    maxLines = 2
                    ellipsize = TextUtils.TruncateAt.END
                }

                val checkBox = CheckBox(requireContext()).apply {
                    setOnCheckedChangeListener { _, isChecked ->
                        val flag = if (isChecked) Paint.STRIKE_THRU_TEXT_FLAG else 0
                        measurementTextView.paintFlags = flag
                        ingredientNameTextView.paintFlags = flag
                    }
                    setPadding(16, 0, 16, 0)
                    layoutParams = TableRow.LayoutParams(
                        TableRow.LayoutParams.WRAP_CONTENT,
                        TableRow.LayoutParams.WRAP_CONTENT
                    ).apply {
                        gravity = Gravity.CENTER
                    }
                }

                tableRow.addView(checkBox)
                tableRow.addView(measurementTextView)
                tableRow.addView(ingredientNameTextView)
                binding.ingredientsLayout.addView(tableRow)
            }
        }

        // Format instructions in ViewModel
        fullRecipeViewModel.prepareInstructions(recipe.instructions)

        // Observe formatted instructions here
        fullRecipeViewModel.formattedInstructions.observe(viewLifecycleOwner) {
            binding.instructionsTextView.text = it
        }

        // Format tags & categories
        binding.tagsTextView.text = recipe.tags.mapNotNull {
            try { RecipeTag.valueOf(it).getDisplayName() } catch (e: Exception) { null }
        }.joinToString(", ")

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

        // After showing the rating stars, scroll the ScrollView to the bottom
        binding.nestedScrollView.post {
            binding.nestedScrollView.smoothScrollTo(0, binding.nestedScrollView.height)
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fullRecipeViewModel.fetchRecipeById(recipeId)

    }
}
