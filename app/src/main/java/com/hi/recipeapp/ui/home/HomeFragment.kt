package com.hi.recipeapp.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.hi.recipeapp.databinding.FragmentHomeBinding
import com.hi.recipeapp.services.UserService
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment() {
    private val homeViewModel: HomeViewModel by viewModels() // Get ViewModel instance
    private lateinit var recipeAdapter: RecipeAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentHomeBinding.inflate(inflater, container, false)

        // Initialize the adapter with the click listener and favorite click handler
        recipeAdapter = RecipeAdapter(
            onClick = { recipe ->
                val recipeId = recipe.id  // Extract the id from the clicked RecipeCard
                val action = HomeFragmentDirections.actionHomeFragmentToFullRecipeFragment(recipeId)
                findNavController().navigate(action)
            },
            onFavoriteClick = { recipe, isFavorited ->
                // When the heart button is clicked, call updateFavoriteStatus from ViewModel
                homeViewModel.updateFavoriteStatus(recipe, isFavorited)
            }
        )
        binding.recipeRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recipeRecyclerView.adapter = recipeAdapter

        // Observe the recipes LiveData from HomeViewModel
        homeViewModel.recipes.observe(viewLifecycleOwner) { recipes ->
            if (recipes != null) {
                if (recipes.isEmpty()) {
                    binding.textHome.visibility = View.VISIBLE // Show message if no recipes
                    binding.recipeRecyclerView.visibility = View.GONE
                } else {
                    binding.textHome.visibility = View.GONE // Hide message if recipes are found
                    binding.recipeRecyclerView.visibility = View.VISIBLE
                    recipeAdapter.submitList(recipes) // Update RecyclerView
                }
            }
        }

        // Observe the error message LiveData
        homeViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }

        // Observe the loading state
        homeViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                Log.d("HomeFragment", "Loading data...")
                binding.progressBar.visibility = View.VISIBLE
            } else {
                Log.d("HomeFragment", "Loading complete.")
                binding.progressBar.visibility = View.GONE
            }
        }

        // Observe the favorite action message LiveData
        homeViewModel.favoriteActionMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                // Show the message using a Snackbar
                Snackbar.make(binding.root, it, Snackbar.LENGTH_SHORT).show()
            }
        }

        // Fetch recipes when fragment is created
        homeViewModel.fetchRecipes()

        return binding.root
    }
}

