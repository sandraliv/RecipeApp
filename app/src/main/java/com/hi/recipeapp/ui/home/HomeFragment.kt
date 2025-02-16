package com.hi.recipeapp.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.hi.recipeapp.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var adapter: RecipeAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentHomeBinding.inflate(inflater, container, false)

        // Initialize the ViewModel
        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        // Set up RecyclerView and Adapter
        adapter = RecipeAdapter()
        binding.recipeRecyclerView.adapter = adapter

        // Set the LayoutManager for RecyclerView
        binding.recipeRecyclerView.layoutManager = LinearLayoutManager(context)

        // Observe the LiveData from ViewModel
        homeViewModel.recipes.observe(viewLifecycleOwner) { recipeList ->
            // Submit the new list to the adapter
            adapter.submitList(recipeList)
        }

        homeViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }

        // Trigger the recipe fetch
        homeViewModel.fetchRecipes()

        return binding.root
    }
}
