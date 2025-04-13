package com.hi.recipeapp.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
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

@AndroidEntryPoint
class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

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
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        toolbar = requireActivity().findViewById(R.id.toolbar)

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
                onDeleteClick = {recipeId -> homeViewModel.deleteRecipe(recipeId)},
                onEditClick = { recipeId ->
                    val action = HomeFragmentDirections.homeFragmentToEditRecipe(recipeId)
                    findNavController().navigate(action)
                }


            )
        }!!

        homeViewModel.isLoading.observe(viewLifecycleOwner, Observer { isLoading ->
            if (isLoading) {
                binding.progressBar.visibility = View.VISIBLE
                binding.recipeRecyclerView.visibility = View.GONE
            } else {
                binding.progressBar.visibility = View.GONE
                binding.recipeRecyclerView.visibility = View.VISIBLE
               }
        })


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

                binding.textNoRecipeResults.text = getString(R.string.no_more_recipes_available)
                binding.textNoRecipeResults.visibility = View.VISIBLE
            } else {

                binding.textNoRecipeResults.visibility = View.GONE

            }
        }

        homeViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }

        homeViewModel.favoriteActionMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_SHORT).show()
            }
        }

        addScrollListenerToRecyclerView()
        return binding.root
    }

    /**
     * Adds a scroll listener to the RecyclerView to handle pagination, hiding and showing UI components
     * based on the scroll direction, and animating UI changes.
     */
    private fun addScrollListenerToRecyclerView() {
        binding.recipeRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if (isAnimationInProgress) return
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

    /**
     * Loads more recipes when the user reaches the bottom of the list.
     */
    private fun loadMoreRecipes() {
        // Prevent multiple load requests if already loading or no more recipes
        if (isLoadingMore || homeViewModel.noMoreRecipes.value == true) return

        isLoadingMore = true
        binding.progressBarBottom.visibility = View.VISIBLE
        homeViewModel.loadMoreRecipes(currentSortType)
        homeViewModel.isLoadingMore.observe(viewLifecycleOwner, Observer { isLoading ->
            if (!isLoading) {
                isLoadingMore = false

                homeViewModel.recipes.observe(viewLifecycleOwner) { recipes ->
                    recipeAdapter.submitList(recipes)
                }

                binding.progressBarBottom.visibility = View.GONE
                if (homeViewModel.noMoreRecipes.value == true) {
                    binding.textNoRecipeResults.text = getString(R.string.no_more_recipes_available)
                    binding.textNoRecipeResults.visibility = View.VISIBLE
                    binding.progressBarBottom.visibility = View.GONE
                } else {
                    binding.textNoRecipeResults.visibility = View.GONE
                }
            }
        })
    }

    /**
     * Ensures that the toolbar and views are correctly displayed when returning to the fragment.
     */
    override fun onResume() {
        super.onResume()
        // Ensure Toolbar and views are correctly displayed when returning to the fragment
        toolbar.animate().alpha(1f).translationY(0f).setDuration(300).start()
        binding.homeHeader.visibility = View.VISIBLE
        binding.buttonContainer.visibility = View.VISIBLE
        binding.plass.visibility = View.GONE
        titleTextView.visibility = View.GONE
    }

    /**
     * Sets up the Sort button to allow the user to choose the sorting method.
     */
    private fun setupSortButton() {
        val sortButton = binding.sortByButton
        sortButton.setOnClickListener {
            val bottomSheetFragment = SortBottomSheetFragment()
            bottomSheetFragment.setCurrentSortType(currentSortType)

            bottomSheetFragment.setOnSortSelectedListener { sortType ->

                currentSortType = sortType
                homeViewModel.updateSortType(currentSortType)
                binding.progressBar.visibility = View.VISIBLE
                binding.recipeRecyclerView.visibility = View.GONE
                binding.textHome.visibility = View.GONE

                recipeAdapter.submitList(emptyList())

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

    /**
     * Sets up the Category button to allow the user to filter recipes by category.
     */
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

    /**
     * Navigates to the CategoryFragment when a category is selected.
     */
    private fun navigateToCategoryFragment(category: Category) {
        val action = HomeFragmentDirections.actionHomeFragmentToCategoryFragment(category.name)  // Pass category name
        findNavController().navigate(action)
    }

    /**
     * Cleans up resources when the fragment's view is destroyed.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}

