package com.hi.recipeapp.ui.home

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.hi.recipeapp.R
import com.hi.recipeapp.classes.Category
import com.hi.recipeapp.classes.SortType
import com.hi.recipeapp.databinding.FragmentHomeBinding
import com.hi.recipeapp.ui.bottomsheetdialog.CategoryBottomSheetFragment
import com.hi.recipeapp.ui.bottomsheetdialog.SortBottomSheetFragment
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding

    private val homeViewModel: HomeViewModel by viewModels()
    private lateinit var recipeAdapter: RecipeAdapter
    private var isAnimationInProgress = false
    private var currentSortType: SortType = SortType.RATING
    private lateinit var toolbar: Toolbar
    private var isUserDragging = false
    private lateinit var titleTextView: TextView
    private var isLoadingMore = false
    private val starSize = 30
    private val spaceBetweenStars = 3
    private var isScrollingUp = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        // Access the toolbar from the activity
        toolbar = requireActivity().findViewById(R.id.toolbar)

        // Find the custom TextView inside the Toolbar
        titleTextView = toolbar.findViewById(R.id.titleTextView)

        recipeAdapter = homeViewModel.isAdmin.value?.let {
            RecipeAdapter(
                onClick = { recipe ->
                    val recipeId = recipe.id
                    val action = HomeFragmentDirections.actionHomeFragmentToFullRecipeFragment(recipeId)
                    findNavController().navigate(action)
                },
                onFavoriteClick = { recipe, isFavorited ->
                    homeViewModel.updateFavoriteStatus(recipe, isFavorited)
                },
                starSize = starSize,
                spaceBetweenStars = spaceBetweenStars,
                isAdmin = it,
                onDeleteClick = {recipeId -> homeViewModel.deleteRecipe(recipeId)}

            )
        }!!

        homeViewModel.isLoading.observe(viewLifecycleOwner, Observer { isLoading ->
            if (isLoading) {
                binding.progressBar.visibility = View.VISIBLE // Show progress bar
                binding.recipeRecyclerView.visibility = View.GONE
            } else {
                binding.progressBar.visibility = View.GONE // Hide progress bar
                binding.recipeRecyclerView.visibility = View.VISIBLE
               }
        })

        // Set up GridLayoutManager with 2 columns (you can adjust the number of columns)
        val gridLayoutManager = GridLayoutManager(requireContext(), 2)
        binding.recipeRecyclerView.layoutManager = gridLayoutManager
        binding.recipeRecyclerView.adapter = recipeAdapter


        setupCategoryButton()
        setupSortButton()

        // Observe the recipes LiveData
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

        homeViewModel.noMoreRecipes.observe(viewLifecycleOwner) { noMoreRecipes ->
            if (noMoreRecipes) {
                // Prevent further load attempts until the page is refreshed
                binding.textNoRecipeResults.text = getString(R.string.no_more_recipes_available)
                binding.textNoRecipeResults.visibility = View.VISIBLE
            } else {
                // Reset the message and hide the bottom container
                binding.textNoRecipeResults.visibility = View.GONE

            }
        }

        // Observe the error message LiveData
        homeViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }


        // Observe the favorite action message LiveData
        homeViewModel.favoriteActionMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_SHORT).show()
            }
        }


        // Add scroll listener
        addScrollListenerToRecyclerView()
        return binding.root
    }

    private fun addScrollListenerToRecyclerView() {
        binding.recipeRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if (isAnimationInProgress) return // Prevent triggering animations if they're already in progress
                // If already loading or no more recipes, do nothing
                if (isLoadingMore || homeViewModel.noMoreRecipes.value == true) return


                // Handle the header visibility based on scroll direction
                if (dy > 0) {
                    // User is scrolling down (further down)
                    if (!isScrollingUp) {
                        isAnimationInProgress = true

                        // Animate the hiding of the header and buttons
                        binding.homeHeader.animate()
                            .alpha(0f)
                            .translationY(-binding.homeHeader.height.toFloat())
                            .setDuration(300)
                            .withEndAction {
                                binding.homeHeader.visibility = View.GONE
                                binding.buttonContainer.visibility = View.GONE
                                binding.plass.visibility = View.VISIBLE
                                titleTextView.visibility = View.VISIBLE
                                titleTextView.text = "Recipes"
                                binding.plass.visibility = View.VISIBLE
                                isScrollingUp = true
                                isAnimationInProgress = false
                            }
                            .start()

                        // Animate the button container
                        binding.buttonContainer.animate()
                            .alpha(0f)
                            .translationY(-binding.buttonContainer.height.toFloat())
                            .setDuration(300)
                            .withEndAction {
                                binding.buttonContainer.visibility = View.GONE
                            }
                            .start()

                        // Animate the toolbar (mini-header)
                        toolbar.animate()
                            .alpha(1f)
                            .translationY(0f)
                            .setDuration(300)
                            .start()
                    }
                    // Check if we are at the bottom of the list
                    val layoutManager = recyclerView.layoutManager as GridLayoutManager
                    val totalItemCount = layoutManager.itemCount
                    val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()

                    val isAtBottom = totalItemCount <= lastVisibleItemPosition + 4 // Check if we're close to the bottom
                    if (isAtBottom) {
                        loadMoreRecipes()

                    }
                } else if (dy < 0) {
                    // User is scrolling up (viewing further up)
                    if (isScrollingUp) {
                        isAnimationInProgress = true

                        // Animate the showing of the header and buttons
                        binding.homeHeader.animate()
                            .alpha(1f)
                            .translationY(0f)
                            .setDuration(300)
                            .withEndAction {
                                binding.homeHeader.visibility = View.VISIBLE
                                binding.buttonContainer.visibility = View.VISIBLE
                                binding.plass.visibility = View.GONE
                                titleTextView.text = ""
                                titleTextView.visibility = View.GONE
                                binding.plass.visibility = View.GONE
                                isScrollingUp = false
                                isAnimationInProgress = false
                            }
                            .start()

                        // Animate the button container
                        binding.buttonContainer.animate()
                            .alpha(1f)
                            .translationY(0f)
                            .setDuration(300)
                            .withEndAction {
                                binding.buttonContainer.visibility = View.VISIBLE
                            }
                            .start()

                        // Animate the toolbar (mini-header)
                        toolbar.animate()
                            .alpha(0f)
                            .translationY(-toolbar.height.toFloat())
                            .setDuration(300)
                            .start()
                    }
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    isUserDragging = true
                } else if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    isUserDragging = false
                }
            }
        })
    }
    private fun loadMoreRecipes() {
        // Prevent multiple load requests if already loading or no more recipes
        if (isLoadingMore || homeViewModel.noMoreRecipes.value == true) return

        // Mark as loading more recipes
        isLoadingMore = true
        binding.progressBarBottom.visibility = View.VISIBLE

        // Pass the current sort type to the ViewModel so that it loads recipes accordingly
        homeViewModel.loadMoreRecipes(currentSortType)

        // Observe the loading state (whether we are still loading)
        homeViewModel.isLoadingMore.observe(viewLifecycleOwner, Observer { isLoading ->
            if (!isLoading) {
                // Once loading is finished, reset isLoadingMore flag
                isLoadingMore = false

                // Handle the loaded recipes
                homeViewModel.recipes.observe(viewLifecycleOwner) { recipes ->
                    recipeAdapter.submitList(recipes)
                }

                // Hide the progress bar and bottom container after loading
                binding.progressBarBottom.visibility = View.GONE
                if (homeViewModel.noMoreRecipes.value == true) {
                    // If no more recipes are available, show the no more recipes message
                    binding.textNoRecipeResults.text = getString(R.string.no_more_recipes_available)
                    binding.textNoRecipeResults.visibility = View.VISIBLE
                    binding.progressBarBottom.visibility = View.GONE
                } else {
                    // If there are more recipes, hide the "no more" message
                    binding.textNoRecipeResults.visibility = View.GONE
                }
            }
        })
    }


    override fun onResume() {
        super.onResume()
        // Ensure Toolbar and views are correctly displayed when returning to the fragment
        toolbar.animate().alpha(1f).translationY(0f).setDuration(300).start()
        binding.homeHeader.visibility = View.VISIBLE
        binding.buttonContainer.visibility = View.VISIBLE
        binding.plass.visibility = View.GONE
        titleTextView.visibility = View.GONE
    }
    private fun setupSortButton() {
        val sortButton = binding.sortByButton
        sortButton.setOnClickListener {
            val bottomSheetFragment = SortBottomSheetFragment()
            bottomSheetFragment.setCurrentSortType(currentSortType)

            bottomSheetFragment.setOnSortSelectedListener { sortType ->
                // Set the new sort type
                currentSortType = sortType

                // Update the sort type in the ViewModel
                homeViewModel.updateSortType(currentSortType)

                // Show loading UI immediately
                binding.progressBar.visibility = View.VISIBLE
                binding.recipeRecyclerView.visibility = View.GONE
                binding.textHome.visibility = View.GONE // Hide "No Recipes" message while loading

                // Clear the current list of recipes
                recipeAdapter.submitList(emptyList())

                // Observe the recipes LiveData to ensure data is updated
                homeViewModel.recipes.observe(viewLifecycleOwner) { recipes ->
                    if (recipes != null) {
                        // Submit the new list of recipes
                        recipeAdapter.submitList(recipes)

                        // Handle the case where the list is empty
                        if (recipes.isEmpty()) {
                            binding.textHome.text = getString(R.string.no_more_recipes_available)
                            binding.textHome.visibility = View.VISIBLE
                            binding.recipeRecyclerView.visibility = View.GONE
                        } else {
                            binding.progressBar.visibility = View.GONE
                            binding.recipeRecyclerView.visibility = View.VISIBLE
                            binding.textHome.visibility = View.GONE // Hide "No Recipes" message

                            // Scroll to the top after sorting
                            binding.recipeRecyclerView.scrollToPosition(0)
                        }
                    }
                }
            }

            bottomSheetFragment.show(childFragmentManager, bottomSheetFragment.tag)
        }
    }




    // Setup Category Button
    private fun setupCategoryButton() {

        val categoryButton = binding.categoryButton
        categoryButton.setOnClickListener {
            val bottomSheetFragment = CategoryBottomSheetFragment()

            bottomSheetFragment.setOnCategorySelectedListener { category ->
                navigateToCategoryFragment(category)
            }

            bottomSheetFragment.show(childFragmentManager, bottomSheetFragment.tag)
        }
    }



    private fun navigateToCategoryFragment(category: Category) {
        val action = HomeFragmentDirections.actionHomeFragmentToCategoryFragment(category.name)  // Pass category name
        findNavController().navigate(action)
    }

}

