package com.hi.recipeapp.ui.myrecipes

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.hi.recipeapp.R
import com.hi.recipeapp.databinding.FragmentMyRecipesBinding
import com.hi.recipeapp.ui.home.RecipeAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MyRecipesFragment : Fragment() {

    private lateinit var binding: FragmentMyRecipesBinding
    private val myRecipesViewModel: MyRecipesViewModel by viewModels()
    private lateinit var recipeAdapter: RecipeAdapter
    private lateinit var userRecipeAdapter: UserRecipeAdapter


    private val starSize = 30
    private val spaceBetweenStars = 3


    private val gridColumnCount = 2

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMyRecipesBinding.inflate(inflater, container, false)

        // Initialize the adapter for both RecyclerViews
        recipeAdapter = RecipeAdapter(
            onClick = { recipe ->
                val recipeId = recipe.id
                val action = MyRecipesFragmentDirections.actionMyRecipesFragmentToFullRecipeFragment(recipeId)
                findNavController().navigate(action)
            },
            onFavoriteClick = { recipe, isFavorited ->
                myRecipesViewModel.updateFavoriteStatus(recipe, isFavorited)
            },
            starSize = starSize,  // Pass starSize
            spaceBetweenStars = spaceBetweenStars,
            isAdmin = false,
            onDeleteClick = {}

        )

        userRecipeAdapter = UserRecipeAdapter { userRecipe ->
            val recipeId = userRecipe.id
            val action = MyRecipesFragmentDirections.actionMyRecipesFragmentToUserFullRecipeFragment(recipeId)
            findNavController().navigate(action)
        }


        setupRecyclerView()
        setupButtonListeners()
        observeViewModel()

        // Observe favorite action message
        myRecipesViewModel.favoriteActionMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                // Show the message using Snackbar
                Snackbar.make(binding.root, it, Snackbar.LENGTH_SHORT).show()
            }
        }

        // Initially, show the Favorite recipes and fetch them
        binding.favoriteRecipeRecyclerView.visibility = View.VISIBLE
        binding.userRecipesRecyclerView.visibility = View.GONE
        binding.favoritesButton.setTextColor(ContextCompat.getColorStateList(requireContext(), R.color.button_text_selector))
        binding.myRecipesButton.setTextColor(ContextCompat.getColorStateList(requireContext(), R.color.button_text_selector))

        // Set the Favorites button as active (and My Recipes button as inactive)
        setActiveButton(binding.favoritesButton)
        // Fetch the favorite recipes by default
        myRecipesViewModel.fetchFavoriteRecipes()

        return binding.root
    }

    private fun setupButtonListeners() {
        binding.favoritesButton.setOnClickListener {
            // Set the active/inactive states of the buttons
            setActiveButton(binding.favoritesButton)

            // Show the favorite recipes and hide the user recipes
            binding.favoriteRecipeRecyclerView.visibility = View.VISIBLE
            binding.userRecipesRecyclerView.visibility = View.GONE
            myRecipesViewModel.fetchFavoriteRecipes()
        }

        binding.myRecipesButton.setOnClickListener {
            // Set the active/inactive states of the buttons
            setActiveButton(binding.myRecipesButton)

            // Show the user recipes and hide the favorite recipes
            binding.favoriteRecipeRecyclerView.visibility = View.GONE
            binding.userRecipesRecyclerView.visibility = View.VISIBLE
            myRecipesViewModel.fetchUserRecipes()
        }
    }


    private fun setupRecyclerView() {
        // Set up GridLayoutManager for favorite recipes
        binding.favoriteRecipeRecyclerView.apply {
            layoutManager = GridLayoutManager(context, gridColumnCount)
            adapter = recipeAdapter
        }

        // Set up GridLayoutManager for user recipes
        binding.userRecipesRecyclerView.apply {
            layoutManager = GridLayoutManager(context, gridColumnCount)
            adapter = userRecipeAdapter
        }
    }

    private fun observeViewModel() {
        myRecipesViewModel.favoriteRecipes.observe(viewLifecycleOwner) { recipes ->
            if (recipes != null) {
                recipeAdapter.submitList(recipes)
            } else {
                Toast.makeText(requireContext(), "No favorite recipes found.", Toast.LENGTH_SHORT).show()
            }
        }

        myRecipesViewModel.userRecipes.observe(viewLifecycleOwner) { userrecipes ->
            if (userrecipes != null) {
                userRecipeAdapter.submitList(userrecipes)
            } else {
                Toast.makeText(requireContext(), "No recipes found.", Toast.LENGTH_SHORT).show()
            }
        }

        myRecipesViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        myRecipesViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setActiveButton(button: Button) {
        // Log for debugging
        Log.d("ButtonState", "Setting button background for: ${button.text}")
        // Create ColorStateList for background tint based on button selected state
        val selectedTint = ContextCompat.getColorStateList(requireContext(), R.color.button_selected_tint)
        val defaultTint = ContextCompat.getColorStateList(requireContext(), R.color.button_default_tint)


        // Mark the selected button as active and the other as inactive
        if (button == binding.favoritesButton) {
            binding.favoritesButton.isSelected = true
            binding.favoritesButton.elevation = 0f

            binding.myRecipesButton.isSelected = false
            binding.myRecipesButton.elevation = 2f

            // Set background tint for the active button
            binding.favoritesButton.backgroundTintList = selectedTint
            binding.myRecipesButton.backgroundTintList = defaultTint


        } else if (button == binding.myRecipesButton) {
            binding.myRecipesButton.isSelected = true
            binding.myRecipesButton.elevation = 0f

            binding.favoritesButton.isSelected = false
            binding.favoritesButton.elevation = 2f

            // Set background tint for the active button
            binding.myRecipesButton.backgroundTintList = selectedTint
            binding.favoritesButton.backgroundTintList = defaultTint

        }

        // Set text color selector for the buttons
        binding.favoritesButton.setTextColor(ContextCompat.getColorStateList(requireContext(), R.color.button_text_selector))
        binding.myRecipesButton.setTextColor(ContextCompat.getColorStateList(requireContext(), R.color.button_text_selector))
    }




}
