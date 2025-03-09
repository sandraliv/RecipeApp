package com.hi.recipeapp.ui.search

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.appcompat.widget.SearchView
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.hi.recipeapp.databinding.FragmentSearchBinding
import com.hi.recipeapp.ui.home.RecipeAdapter
import dagger.hilt.android.AndroidEntryPoint
import com.hi.recipeapp.classes.RecipeTag

@AndroidEntryPoint
class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private val searchViewModel: SearchViewModel by viewModels()
    private lateinit var recipeAdapter: RecipeAdapter
    private val selectedTags = mutableSetOf<RecipeTag>() // Only tags now

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)

        setupSearchView()
        observeViewModel()
        setupRecyclerView()
        setupTagSelection()

        // Observe the result of adding recipe to favorites
        searchViewModel.favoriteResult.observe(viewLifecycleOwner) { result ->
            result.onSuccess { successMessage ->
                // Handle success (e.g., show a success message)
                Toast.makeText(requireContext(), successMessage, Toast.LENGTH_SHORT).show()
            }.onFailure { exception ->
                // Handle failure (e.g., show error message)
                Toast.makeText(requireContext(), exception.message ?: "An error occurred.", Toast.LENGTH_SHORT).show()
            }
        }

        // Observe the result of adding recipe to favorites
        searchViewModel.favoriteActionMessage.observe(viewLifecycleOwner) { message ->
            if (message != null) {
                // Show the Snackbar with the success or error message
                Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
            }
        }
        return binding.root
    }

    private fun setupSearchView() {
        val searchView: SearchView = binding.searchDashboard
        searchView.isIconified = false
        searchView.setSuggestionsAdapter(null)
        searchView.clearFocus()
        searchView.queryHint = "Search for recipes..."

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // Convert selectedTags (Set<RecipeTag>) to Set<String> before passing it
                val tagNames = selectedTags.map { it.name }.toSet() // Converts to Set<String>
                searchViewModel.searchByQuery(query ?: "", tagNames)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Convert selectedTags (Set<RecipeTag>) to Set<String> before passing it
                val tagNames = selectedTags.map { it.name }.toSet() // Converts to Set<String>
                searchViewModel.searchByQuery(newText ?: "", tagNames)
                return true
            }
        })
    }

    private fun setupTagSelection() {
        val chipGroup = binding.chipGroupTags // Assuming you have a ChipGroup for tags
        chipGroup.removeAllViews()  // Remove old chips

        // Add chips dynamically for tags
        RecipeTag.values().forEach { tag ->
            val chip = Chip(context)
            chip.text = tag.name
            chip.isCheckable = true
            chip.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedTags.add(tag)
                } else {
                    selectedTags.remove(tag)
                }
                // Convert selectedTags (Set<RecipeTag>) to Set<String> before passing it
                val tagNames = selectedTags.map { it.name }.toSet() // Converts to Set<String>
                searchViewModel.searchByQuery(binding.searchDashboard.query.toString(), tagNames)
            }
            chipGroup.addView(chip)
        }
    }


    private fun setupRecyclerView() {
        recipeAdapter = RecipeAdapter(
            onClick = { recipe ->
                // Handle recipe click (navigate to detailed recipe page)
                val recipeId = recipe.id
                val action = SearchFragmentDirections.actionSearchFragmentToFullRecipeFragment(recipeId)
                findNavController().navigate(action)
            },
            onFavoriteClick = { recipe, isFavorited ->
                // When the heart button is clicked, call updateFavoriteStatus from ViewModel
                searchViewModel.updateFavoriteStatus(recipe, isFavorited)
            }
        )
        binding.recipeCardContainer.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = recipeAdapter
        }
    }

    private fun observeViewModel() {
        // Observe search results from the ViewModel and update RecyclerView
        searchViewModel.searchResults.observe(viewLifecycleOwner) { results ->
            if (results.isNullOrEmpty()) {
                binding.textDashboard.text = "No recipes found."  // Show "No recipes found" message
                binding.recipeCardContainer.visibility = View.GONE  // Hide RecyclerView
            } else {
                binding.textDashboard.text = ""  // Clear any previous "No results" text
                binding.recipeCardContainer.visibility = View.VISIBLE  // Show RecyclerView
                recipeAdapter.submitList(results)  // Submit the new results to the adapter
                Log.d("SEARCH_RESULTS", "Submitted list to adapter: $results")
            }
        }

        // Observe any error messages
        searchViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                binding.textDashboard.text = error  // Show the error message
                binding.recipeCardContainer.visibility = View.GONE  // Hide RecyclerView in case of an error
            }
        }

    }

    fun resetSearchState() {
        binding.textDashboard.text = ""  // Clear any previous search results
        binding.searchDashboard.setQuery("", false) // Clear the search view query
        binding.searchDashboard.clearFocus() // Remove focus from the search view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

