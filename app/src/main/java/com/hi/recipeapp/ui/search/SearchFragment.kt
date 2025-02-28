package com.hi.recipeapp.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.appcompat.widget.SearchView
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.hi.recipeapp.databinding.FragmentSearchBinding
import com.hi.recipeapp.ui.search.SearchFragmentDirections
import com.hi.recipeapp.ui.home.RecipeAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private val searchViewModel: SearchViewModel by viewModels() // Inject ViewModel via Hilt
    private lateinit var recipeAdapter: RecipeAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        // Initialize the adapter with the click listener

        setupSearchView()  // Setup SearchView functionality
        observeViewModel() // Observe LiveData updates from ViewModel
        setupRecyclerView() // Set up RecyclerView to show recipe cards


        return binding.root
    }

    // Set up the SearchView and trigger search
    private fun setupSearchView() {
        val searchView: SearchView = binding.searchDashboard
        searchView.isIconified = false
        searchView.setSuggestionsAdapter(null)
        searchView.clearFocus()
        searchView.queryHint = "Search for recipes..."

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    searchViewModel.searchByQuery(it)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })
    }

    private fun setupRecyclerView() {
        // Initialize the adapter with the click listener
        recipeAdapter = RecipeAdapter { recipe ->  // recipe here is of type RecipeCard
            val recipeId = recipe.id  // Extract the id from the clicked RecipeCard
            val action = SearchFragmentDirections.actionSearchFragmentToFullRecipeFragment(recipeId)
            findNavController().navigate(action)
        }
        binding.recipeCardContainer.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = recipeAdapter
        }
    }


    // Observe search results from the ViewModel and update RecyclerView
    private fun observeViewModel() {
        searchViewModel.searchResults.observe(viewLifecycleOwner) { results ->
            if (results.isNullOrEmpty()) {
                binding.textDashboard.text = "No recipes found."
            } else {
                binding.textDashboard.text = ""  // Clear any previous "No results" text
                recipeAdapter.submitList(results) // Update the RecyclerView with the new data
            }
        }
    }

    // Reset the UI when navigating back to this fragment
    fun resetSearchState() {
        binding.textDashboard.text = ""  // Clear any previous search results
        binding.searchDashboard.setQuery("", false) // Clear the search view query
        binding.searchDashboard.clearFocus() // Remove focus from the search view
    }


    // Reset the Dashboard UI when navigating back
    override fun onResume() {
        super.onResume()
        // Clear any search results when returning to the Dashboard
        binding.textDashboard.text = ""  // Clear any previous search results
        binding.searchDashboard.setQuery("", false) // Clear the search view query
        binding.searchDashboard.clearFocus() // Remove focus from the search view
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
