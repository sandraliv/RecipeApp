package com.hi.recipeapp.ui.search

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
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
    private lateinit var toolbar: Toolbar
    private lateinit var titleTextView: TextView
    private val starSize = 30
    private val spaceBetweenStars = 3
    private val gridColumnCount = 2
    private var currentSortType: SortType = SortType.RATING
    private var isScrollingUp = false
    private var isAnimationInProgress = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)

        // Access the toolbar from the activity
        toolbar = requireActivity().findViewById(R.id.toolbar)

        // Find the custom TextView inside the Toolbar
        titleTextView = toolbar.findViewById(R.id.titleTextView)

        titleTextView.text = ""

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


            } else {
                Log.d("SearchFragment", "Loading complete.")
                binding.progressBar.visibility = View.GONE // Hide progress bar
                binding.recipeCardContainer.visibility = View.VISIBLE // Show recipe list

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

        // Observe the loading state for "Load More"
        searchViewModel.isLoadingMore.observe(viewLifecycleOwner) { isLoadingMore ->
            if (isLoadingMore) {
                // Show a loading more indicator (You can use a different loading indicator or text)
                binding.progressBarBottom.visibility = View.VISIBLE
            } else {
                // Hide the loading more indicator
                binding.progressBarBottom.visibility = View.GONE
            }
        }


        // Observe "No More Recipes Available"
        searchViewModel.noMoreRecipes.observe(viewLifecycleOwner) { noMoreRecipes ->
            if (noMoreRecipes) {
                binding.textLoadmore.text = getString(R.string.no_more_recipes_available)
                binding.textLoadmore.visibility = View.VISIBLE

            } else {
                binding.textLoadmore.visibility = View.GONE

            }
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
                // Show loading UI immediately
                binding.progressBar.visibility = View.VISIBLE
                binding.recipeCardContainer.visibility = View.GONE
                binding.textDashboard.visibility = View.GONE // Hide "No Recipes" message while loading

                // Clear the current list of recipes
                recipeAdapter.submitList(emptyList())
                // Manually scroll to the top of the RecyclerView

                binding.recipeCardContainer.scrollToPosition(0)
            }

            bottomSheetFragment.show(childFragmentManager, bottomSheetFragment.tag)
        }
    }


    private fun setupSearchView() {
        val searchView: SearchView = binding.searchDashboard
        searchView.isIconified = false
        searchView.queryHint = "Search for recipes..."

        // Detect when the user clicks on the search bar
        searchView.setOnClickListener {
            Log.d("SearchView", "Search bar clicked!")

            // Ensure tags and buttons are shown, even if they're hidden due to scrolling
            showSortAndTags()
        }


        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // Convert selectedTags (Set<RecipeTag>) to Set<String> before passing it
                val tagNames = selectedTags.map { it.name }.toSet()
                binding.recipeCardContainer.visibility = View.GONE
                binding.progressBar.visibility = View.VISIBLE
                searchViewModel.searchByQuery(query ?: "", tagNames, currentSortType)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val tagNames = selectedTags.map { it.name }.toSet()
                binding.recipeCardContainer.visibility = View.GONE
                binding.progressBar.visibility = View.VISIBLE
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
            spaceBetweenStars = spaceBetweenStars
        )
        // Use GridLayoutManager with the defined number of columns
        val gridLayoutManager = GridLayoutManager(context, gridColumnCount)
        binding.recipeCardContainer.apply {
            layoutManager = gridLayoutManager
            adapter = recipeAdapter
        }

        binding.recipeCardContainer.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if (isAnimationInProgress) return // Prevent triggering animations if they're already in progress

                if (dy > 0) {
                    // Scrolling down
                    if (!isScrollingUp) {
                        isAnimationInProgress = true



                        // Animate hiding the ChipGroup and Sort button
                        binding.chipGroupTags.animate()
                            .alpha(0f)
                            .translationY(-binding.chipGroupTags.height.toFloat())
                            .setDuration(300)
                            .start()

                        binding.buttonContainer.animate()
                            .alpha(0f)
                            .translationY(-binding.buttonContainer.height.toFloat())
                            .setDuration(300)
                            .withEndAction {
                                binding.chipGroupTags.visibility = View.GONE
                                binding.buttonContainer.visibility = View.GONE
                                isScrollingUp = true
                                isAnimationInProgress = false
                            }
                            .start()
                    }
                } else if (dy < 0) {
                    // Scrolling up
                    if (isScrollingUp) {
                        isAnimationInProgress = true

                        // Animate showing the ChipGroup and Sort button
                        binding.chipGroupTags.animate()
                            .alpha(1f)
                            .translationY(0f)
                            .setDuration(300)
                            .start()

                        binding.buttonContainer.animate()
                            .alpha(1f)
                            .translationY(0f)
                            .setDuration(300)
                            .withEndAction {
                                binding.chipGroupTags.visibility = View.VISIBLE
                                binding.buttonContainer.visibility = View.VISIBLE
                                isScrollingUp = false
                                isAnimationInProgress = false
                            }
                            .start()
                    }
                }

                // Handle infinite scrolling (load more recipes)
                val layoutManager = recyclerView.layoutManager as GridLayoutManager
                val totalItemCount = layoutManager.itemCount
                val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
                if (totalItemCount <= lastVisibleItemPosition + 2) {
                    if (!searchViewModel.isLoading.value!! && !searchViewModel.noMoreRecipes.value!!) {
                        searchViewModel.loadMoreRecipes(
                            binding.searchDashboard.query.toString(),
                            selectedTags.map { it.name }.toSet()
                        )
                    }
                }
            }
        })

    }


    private fun observeViewModel() {
        searchViewModel.searchResults.observe(viewLifecycleOwner) { results ->
            if (results.isNullOrEmpty()) {
                binding.textDashboard.text = "No recipes found."
                binding.progressBar.visibility = View.GONE
                binding.recipeCardContainer.visibility = View.GONE
                binding.textDashboard.visibility = View.VISIBLE
            } else {
                binding.textDashboard.visibility = View.GONE
                binding.recipeCardContainer.visibility = View.VISIBLE
                binding.progressBar.visibility = View.GONE
                recipeAdapter.submitList(results)
                Log.d("SEARCH_RESULTS", "Submitted list to adapter: $results")
            }
        }

        searchViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                binding.textDashboard.text = error
                binding.recipeCardContainer.visibility = View.GONE
            }
        }
    }

    private fun showSortAndTags() {
        // Ensure tags and sort buttons are shown, even when they are hidden due to scrolling
        if (binding.chipGroupTags.visibility != View.VISIBLE) {
            binding.chipGroupTags.visibility = View.VISIBLE
            binding.buttonContainer.visibility = View.VISIBLE

            // Animate them back into place if necessary
            binding.chipGroupTags.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(300)
                .start()

            binding.buttonContainer.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(300)
                .start()
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

