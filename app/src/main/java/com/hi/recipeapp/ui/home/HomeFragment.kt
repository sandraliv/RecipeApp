package com.hi.recipeapp.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.hi.recipeapp.databinding.FragmentHomeBinding
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

        // Initialize the adapter with the click listener
        recipeAdapter = RecipeAdapter { recipeId ->
            // This is where you handle the click event (navigate to FullRecipeFragment)
            val action = HomeFragmentDirections.actionHomeFragmentToFullRecipeFragment(recipeId)
            findNavController().navigate(action)
        }


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

        // Observe progress bar visibility (assuming you want to use it while fetching data)
        homeViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                binding.progressBar.visibility = View.VISIBLE // Show progress bar when loading
            } else {
                binding.progressBar.visibility = View.GONE // Hide progress bar when done
            }
        }


        homeViewModel.fetchRecipes()
        return binding.root
    }

}


