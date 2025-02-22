package com.hi.recipeapp.ui.fullrecipe

import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.hi.recipeapp.R
import com.hi.recipeapp.classes.FullRecipe
import com.hi.recipeapp.databinding.FragmentFullRecipeBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FullRecipeFragment : Fragment() {

    private val fullRecipeViewModel: FullRecipeViewModel by viewModels() // Get ViewModel instance
    private lateinit var binding: FragmentFullRecipeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFullRecipeBinding.inflate(inflater, container, false)

        // Get the recipe ID from arguments
        val recipeId = arguments?.getInt("recipeId") ?: return null

        // Fetch full recipe data
        fullRecipeViewModel.fetchRecipeById(recipeId)

        // Observe the full recipe data
        fullRecipeViewModel.recipe.observe(viewLifecycleOwner) { recipe ->
            Log.d("FullRecipeFragment", "Observer triggered, recipe: $recipe")

            recipe?.let {
                // Bind the full recipe data to the UI
                bindRecipeData(it)
            }
        }

        // Observe error messages
        fullRecipeViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                // Handle error (show toast, etc.)
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            }
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


        // Handle ingredients (now it's a Map<String, String>)
        recipe.ingredients.forEach { (ingredientName, ingredientQuantity) ->
            val formattedIngredientName = ingredientName.replace("_", " ") // Replace underscores with spaces
            val ingredientCheckBox = CheckBox(requireContext()).apply {
                // Replace underscores with spaces in the ingredient name
                text = "$ingredientQuantity: $formattedIngredientName"  // Show both ingredient name and quantity
                setOnCheckedChangeListener { buttonView, isChecked ->
                    if (isChecked) {
                        buttonView.paintFlags = buttonView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    } else {
                        buttonView.paintFlags = buttonView.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                    }
                }
            }
            binding.ingredientsLayout.addView(ingredientCheckBox) // Add checkbox to the layout
        }

        // Set instructions
        binding.instructionsTextView.text = recipe.instructions

        // Set rating and count
        binding.ratingBar.rating = recipe.averageRating.toFloat()
        binding.ratingCountTextView.text = "${recipe.ratingCount} ratings"
        // Display tags (if necessary)
        binding.tagsTextView.text = recipe.tags.joinToString(", ") { it.name } // Display tags as comma-separated
        // Display categories (if necessary)
        binding.categoriesTextView.text = recipe.categories.joinToString(", ") { it.name } // Display categories as comma-separated
    }

}
