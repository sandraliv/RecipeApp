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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.hi.recipeapp.R
import com.hi.recipeapp.databinding.FragmentSearchBinding
import com.hi.recipeapp.ui.home.RecipeAdapter
import dagger.hilt.android.AndroidEntryPoint
import com.hi.recipeapp.classes.RecipeTag
import com.hi.recipeapp.classes.SortType
import com.hi.recipeapp.ui.bottomsheetdialog.SortBottomSheetFragment

@AndroidEntryPoint
class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private val searchViewModel: SearchViewModel by viewModels()
    private lateinit var recipeAdapter: RecipeAdapter
    private val selectedTags = mutableSetOf<RecipeTag>() // Only tags now

    private val starSize = 30
    private val spaceBetweenStars = 3
    private val gridColumnCount = 2
    private var currentSortType: SortType = SortType.RATING

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        setupRecyclerView()
        setupSearchView()
        setupTagSelection()
        setupSortButton()
        observeViewModel()

        // Observe the loading state
        searchViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                Log.d("SearchFragment", "Loading data...")
                binding.progressBar.visibility = View.VISIBLE // Show progress bar
                binding.recipeCardContainer.visibility = View.GONE // Hide the recipe list
                binding.loadMoreButton.isEnabled = false // Disable Load More button while loading
                binding.loadMoreButton.visibility = View.GONE
            } else {
                Log.d("SearchFragment", "Loading complete.")
                binding.progressBar.visibility = View.GONE // Hide progress bar
                binding.recipeCardContainer.visibility = View.VISIBLE // Show recipe list
                binding.loadMoreButton.isEnabled = true // Enable Load More button once loading is complete
                binding.loadMoreButton.visibility = View.GONE
            }
        }


        // Observe the result of adding recipe to favorites
        searchViewModel.favoriteResult.observe(viewLifecycleOwner) { result ->
            result.onSuccess { successMessage ->
                Toast.makeText(requireContext(), successMessage, Toast.LENGTH_SHORT).show()
            }.onFailure { exception ->
                Toast.makeText(requireContext(), exception.message ?: "An error occurred.", Toast.LENGTH_SHORT).show()
            }
        }

        // Observe the result of adding recipe to favorites
        searchViewModel.favoriteActionMessage.observe(viewLifecycleOwner) { message ->
            if (message != null) {
                Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
            }
        }

        // Observe "No More Recipes Available"
        searchViewModel.noMoreRecipes.observe(viewLifecycleOwner) { noMoreRecipes ->
            if (noMoreRecipes) {
                binding.textDashboard.text = getString(R.string.no_more_recipes_available)
                binding.textDashboard.visibility = View.VISIBLE
                binding.loadMoreButton.visibility = View.GONE
            } else {
                binding.textDashboard.visibility = View.GONE
                binding.loadMoreButton.visibility = View.VISIBLE
            }
        }

        // Handle Load More button click
        binding.loadMoreButton.setOnClickListener {
            // Pass the current query and selected tags to load more recipes
            val query = binding.searchDashboard.query.toString()
            val tagNames = selectedTags.map { it.name }.toSet()

            // Pass the parameters to load more recipes
            searchViewModel.loadMoreRecipes(query, tagNames)
            binding.loadMoreButton.visibility = View.GONE
        }

        return binding.root
    }

    private fun setupSortButton() {
        binding.sortByButton.setOnClickListener {
            // Open the Sort BottomSheetDialogFragment
            val bottomSheetFragment = SortBottomSheetFragment()
            bottomSheetFragment.setCurrentSortType(currentSortType)

            bottomSheetFragment.setOnSortSelectedListener { sortType ->
                currentSortType = sortType
                searchViewModel.updateSortType(currentSortType)
                // Manually scroll to the top of the RecyclerView
                binding.recipeCardContainer.scrollToPosition(0)
            }


            bottomSheetFragment.show(childFragmentManager, bottomSheetFragment.tag)
        }
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
                val tagNames = selectedTags.map { it.name }.toSet()
                binding.recipeCardContainer.visibility = View.GONE
                binding.progressBar.visibility = View.VISIBLE
                binding.loadMoreButton.visibility = View.GONE
                searchViewModel.searchByQuery(query ?: "", tagNames, currentSortType)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val tagNames = selectedTags.map { it.name }.toSet()
                binding.recipeCardContainer.visibility = View.GONE
                binding.progressBar.visibility = View.VISIBLE
                binding.loadMoreButton.visibility = View.GONE
                searchViewModel.searchByQuery(newText ?: "", tagNames, currentSortType)
                return true
            }
        })
    }

    private fun setupTagSelection() {
        val chipGroup = binding.chipGroupTags
        chipGroup.removeAllViews()

        RecipeTag.values().forEach { tag ->
            val chip = Chip(context)
            chip.text = tag.getDisplayName()
            chip.isCheckable = true
            chip.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedTags.add(tag)
                } else {
                    selectedTags.remove(tag)
                }
                // Convert selectedTags (Set<RecipeTag>) to Set<String> before passing it
                val tagNames = selectedTags.map { it.name }.toSet()
                searchViewModel.searchByQuery(binding.searchDashboard.query.toString(), tagNames, currentSortType)
            }
            chipGroup.addView(chip)
        }
    }


    private fun setupRecyclerView() {
        recipeAdapter = RecipeAdapter(
            onClick = { recipe ->
                val recipeId = recipe.id
                val action = SearchFragmentDirections.actionSearchFragmentToFullRecipeFragment(recipeId)
                findNavController().navigate(action)
            },
            onFavoriteClick = { recipe, isFavorited ->
                searchViewModel.updateFavoriteStatus(recipe, isFavorited)
            },
            starSize = starSize,
            spaceBetweenStars = spaceBetweenStars,
            isAdmin = false,
            onDeleteClick = { }

        )
        // Use GridLayoutManager with the defined number of columns
        val gridLayoutManager = GridLayoutManager(context, gridColumnCount)
        binding.recipeCardContainer.apply {
            layoutManager = gridLayoutManager
            adapter = recipeAdapter
        }

        // Detect if the user has scrolled to the bottom of the RecyclerView
        binding.recipeCardContainer.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as GridLayoutManager
                val totalItemCount = layoutManager.itemCount
                val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()

                // If the user has scrolled to the bottom, show the "Load More" button
                if (totalItemCount <= lastVisibleItemPosition + 2) {  // 2 is just an offset to trigger early
                    if (!searchViewModel.isLoading.value!! && !searchViewModel.noMoreRecipes.value!!) {
                        binding.loadMoreButton.visibility = View.VISIBLE
                    }
                } else {
                    binding.loadMoreButton.visibility = View.GONE
                }
            }
        })
    }


    private fun observeViewModel() {
        // Observe search results from the ViewModel and update RecyclerView
        searchViewModel.searchResults.observe(viewLifecycleOwner) { results ->
            if (results.isNullOrEmpty()) {
                binding.textDashboard.text = "No recipes found."
                binding.progressBar.visibility = View.GONE
                binding.recipeCardContainer.visibility = View.GONE
                binding.loadMoreButton.visibility = View.GONE
                binding.textDashboard.visibility = View.VISIBLE

            } else {
                binding.textDashboard.visibility = View.GONE
                binding.recipeCardContainer.visibility = View.VISIBLE
                binding.progressBar.visibility = View.GONE
                binding.loadMoreButton.visibility = View.GONE
                recipeAdapter.submitList(results)
                Log.d("SEARCH_RESULTS", "Submitted list to adapter: $results")
            }
        }

        // Observe any error messages
        searchViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                binding.textDashboard.text = error
                binding.recipeCardContainer.visibility = View.GONE
            }
        }

    }

    fun resetSearchState() {
        binding.textDashboard.text = ""
        binding.searchDashboard.setQuery("", false)
        binding.searchDashboard.clearFocus()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

