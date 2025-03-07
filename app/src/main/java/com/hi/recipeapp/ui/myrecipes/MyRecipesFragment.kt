package com.hi.recipeapp.ui.myrecipes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.hi.recipeapp.classes.UserRecipeCard
import com.hi.recipeapp.databinding.FragmentMyRecipesBinding
import com.hi.recipeapp.ui.home.HomeViewModel
import com.hi.recipeapp.ui.home.RecipeAdapter
import com.hi.recipeapp.ui.search.SearchFragmentDirections
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MyRecipesFragment : Fragment() {

    private lateinit var binding: FragmentMyRecipesBinding
    private val myRecipesViewModel: MyRecipesViewModel by viewModels()
    private lateinit var recipeAdapter: RecipeAdapter
    private lateinit var userRecipeAdapter: UserRecipeAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMyRecipesBinding.inflate(inflater, container, false)

        // Initialize the adapter for both RecyclerViews
        recipeAdapter = RecipeAdapter { recipe ->
            val recipeId = recipe.id
            val action = MyRecipesFragmentDirections.actionMyRecipesFragmentToFullRecipeFragment(recipeId)
            findNavController().navigate(action)
        }

        // Initialize userRecipeAdapter here
        userRecipeAdapter = UserRecipeAdapter { userRecipe ->
            val recipeId = userRecipe.id
            val action = MyRecipesFragmentDirections.actionMyRecipesFragmentToUserFullRecipeFragment(recipeId)
            findNavController().navigate(action)
        }


        // Set up the RecyclerViews with LayoutManager and Adapter
        setupRecyclerView()

        // Setup button listeners
        setupButtonListeners()

        // Observe live data from ViewModel
        observeViewModel()

        return binding.root
    }

    private fun setupButtonListeners() {
        // Favorites Button Clicked
        binding.favoritesButton.setOnClickListener {
            // Display Favorites and hide My Recipes
            binding.favoriteRecipeRecyclerView.visibility = View.VISIBLE
            binding.userRecipesRecyclerView.visibility = View.GONE

            // Fetch user's favorite recipes
            myRecipesViewModel.fetchFavoriteRecipes()
        }

        // My Recipes Button Clicked
        binding.myRecipesButton.setOnClickListener {
            // Display My Recipes and hide Favorites
            binding.favoriteRecipeRecyclerView.visibility = View.GONE
            binding.userRecipesRecyclerView.visibility = View.VISIBLE

            // Fetch user's own recipes
            myRecipesViewModel.fetchUserRecipes( 0 , 10 )
        }
    }

    private fun setupRecyclerView() {
        // Set up LayoutManagers and adapters for both RecyclerViews
        binding.favoriteRecipeRecyclerView.apply {
            layoutManager = LinearLayoutManager(context) // Ensure LayoutManager is set
            adapter = recipeAdapter
        }

        binding.userRecipesRecyclerView.apply {
            layoutManager = LinearLayoutManager(context) // Ensure LayoutManager is set
            adapter = userRecipeAdapter
        }
    }

    private fun observeViewModel() {
        // Observe the list of favorite recipes
        myRecipesViewModel.favoriteRecipes.observe(viewLifecycleOwner) { recipes ->
            if (recipes != null) {
                recipeAdapter.submitList(recipes) // Update the RecyclerView
            } else {
                Toast.makeText(requireContext(), "No favorite recipes found.", Toast.LENGTH_SHORT).show()
            }
        }

        myRecipesViewModel.userRecipes.observe(viewLifecycleOwner) { userrecipes ->
            if (userrecipes != null) {
                userRecipeAdapter.submitList(userrecipes) // Update the RecyclerView
            } else {
                Toast.makeText(requireContext(), "No recipes found.", Toast.LENGTH_SHORT).show()
            }
        }

        // Observe loading state (progress bar visibility)
        myRecipesViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                binding.progressBar.visibility = View.VISIBLE
            } else {
                binding.progressBar.visibility = View.GONE
            }
        }

        // Observe error messages
        myRecipesViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }
    }
}