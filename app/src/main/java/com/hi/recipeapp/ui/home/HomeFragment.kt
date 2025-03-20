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
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.hi.recipeapp.R
import com.hi.recipeapp.classes.Category
import com.hi.recipeapp.databinding.FragmentHomeBinding
import com.hi.recipeapp.services.UserService
import com.hi.recipeapp.ui.bycategory.CategoryButtonAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment() {
    private val homeViewModel: HomeViewModel by viewModels()
    private lateinit var recipeAdapter: RecipeAdapter
    private lateinit var categoryButtonAdapter:CategoryButtonAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentHomeBinding.inflate(inflater, container, false)

        // Initialize the adapter with the click listener and favorite click handler
        recipeAdapter = RecipeAdapter(
            onClick = { recipe ->
                val recipeId = recipe.id
                val action = HomeFragmentDirections.actionHomeFragmentToFullRecipeFragment(recipeId)
                findNavController().navigate(action)
            },
            onFavoriteClick = { recipe, isFavorited ->
                homeViewModel.updateFavoriteStatus(recipe, isFavorited)
            }
        )
        binding.recipeRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recipeRecyclerView.adapter = recipeAdapter

        // Set up Category Button RecyclerView
        setupCategoryRecyclerView(binding)

        // Observe the recipes LiveData from HomeViewModel
        homeViewModel.recipes.observe(viewLifecycleOwner) { recipes ->
            if (recipes != null) {
                if (recipes.isEmpty()) {
                    binding.textHome.visibility = View.VISIBLE
                    binding.recipeRecyclerView.visibility = View.GONE
                } else {
                    binding.textHome.visibility = View.GONE
                    binding.recipeRecyclerView.visibility = View.VISIBLE
                    recipeAdapter.submitList(recipes)
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
                Snackbar.make(binding.root, it, Snackbar.LENGTH_SHORT).show()
            }
        }

        // Fetch recipes when fragment is created
        homeViewModel.fetchRecipes()

        return binding.root
    }

    private fun setupCategoryRecyclerView(binding: FragmentHomeBinding) {
        // Prepare the list of categories
        val categories = Category.values().toList()

        Log.d("CategoryRecyclerView", "Categories: $categories")

        categoryButtonAdapter = CategoryButtonAdapter(
            categories = categories,
            onCategoryClick = { category ->
                Log.d("CategoryRecyclerView", "Category clicked: ${category.name}")
                navigateToCategoryFragment(category)
            }
        )

        // Set up the RecyclerView for category buttons
        binding.categoryRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.categoryRecyclerView.adapter = categoryButtonAdapter


    }

    private fun navigateToCategoryFragment(category: Category) {
        val action = HomeFragmentDirections.actionHomeFragmentToCategoryFragment(category.name)  // Pass category name
        findNavController().navigate(action)
    }


}

