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
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.hi.recipeapp.R
import com.hi.recipeapp.classes.UserFullRecipe
import com.hi.recipeapp.databinding.FragmentUserFullRecipeBinding
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
class UserFullRecipeFragment : Fragment() {

    private val userFullRecipeViewModel: UserFullRecipeViewModel by viewModels() // Get ViewModel instance
    private lateinit var binding: FragmentUserFullRecipeBinding


    // Safe Args: Retrieve arguments passed to the fragment
    private val args: UserFullRecipeFragmentArgs by navArgs()
    private val recipeId: Int get() = args.recipeId

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUserFullRecipeBinding.inflate(inflater, container, false)



        // Fetch full recipe data
        userFullRecipeViewModel.fetchUserRecipeById(recipeId)

        // Observe the full recipe data
        userFullRecipeViewModel.userrecipe.observe(viewLifecycleOwner) { userrecipe ->
            Log.d("FullRecipeFragment", "Observer triggered, recipe: $userrecipe")

            userrecipe?.let {
                // Bind the full recipe data to the UI
                bindRecipeData(it)
            }
        }

        // Observe error messages
        userFullRecipeViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                // Handle error (show toast, etc.)
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            }
        }

        return binding.root
    }

    private fun bindRecipeData(recipe: UserFullRecipe) {
        // Bind the recipe data to the UI
        binding.titleTextView.text = recipe.title
        binding.descriptionTextView.text = recipe.description

        // Load the image using Glide with placeholder and error image
        Glide.with(binding.root.context)
            .load(recipe.imageUrl)
            .placeholder(R.drawable.placeholder)  // Placeholder image
            .error(R.drawable.error_image)        // Error image
            .into(binding.imageView)

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



    }

}
