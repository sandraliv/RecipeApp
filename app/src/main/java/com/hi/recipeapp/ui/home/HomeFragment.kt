package com.hi.recipeapp.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
    private lateinit var binding: FragmentHomeBinding

    private val homeViewModel: HomeViewModel by viewModels()
    private lateinit var recipeAdapter: RecipeAdapter
    private var currentSortType: SortType = SortType.RATING
    private val starSize = 30
    private val spaceBetweenStars = 3

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        binding.loadMoreButton.visibility = View.GONE
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

        // Observe the loading state
        homeViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                binding.progressBar.visibility = View.VISIBLE // Show progress bar
                binding.recipeRecyclerView.visibility = View.GONE
                binding.loadMoreButton.isEnabled = false   // Disable the Load More button while loading
                binding.loadMoreButton.visibility = View.GONE
            } else {
                binding.progressBar.visibility = View.GONE // Hide progress bar
                binding.recipeRecyclerView.visibility = View.VISIBLE
                binding.loadMoreButton.isEnabled = true    // Enable the Load More button once loading is complete
            }
        }

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


        // Observe the "No More Recipes Available" state
        homeViewModel.noMoreRecipes.observe(viewLifecycleOwner, Observer { noMoreRecipes ->
            if (noMoreRecipes) {
                // Show the "No More Recipes Available" message
                binding.textHome.text = getString(R.string.no_more_recipes_available)
                binding.textHome.visibility = View.VISIBLE
                binding.loadMoreButton.visibility = View.GONE
            } else {
                // Hide the "No Recipes Available" message
                binding.textHome.visibility = View.GONE
                binding.loadMoreButton.visibility = View.VISIBLE
            }
        })


        // Handle Load More button click
        binding.loadMoreButton.setOnClickListener {
            homeViewModel.loadMoreRecipes()
            binding.loadMoreButton.visibility = View.GONE
        }

        // Detect if the user has scrolled to the bottom of the RecyclerView
        binding.recipeRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as GridLayoutManager
                val totalItemCount = layoutManager.itemCount
                val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()

                // If the user has scrolled to the bottom, show the "Load More" button
                if (totalItemCount <= lastVisibleItemPosition + 2) {  // 2 is just an offset to trigger early
                    if (!homeViewModel.isLoading.value!! && !homeViewModel.noMoreRecipes.value!!) {
                        binding.loadMoreButton.visibility = View.VISIBLE
                    }
                } else {
                    binding.loadMoreButton.visibility = View.GONE
                }
            }
        })


        return binding.root
    }

    private fun setupSortButton() {
        val sortButton = binding.sortByButton
        sortButton.setOnClickListener {
            val bottomSheetFragment = SortBottomSheetFragment()
            bottomSheetFragment.setCurrentSortType(currentSortType)

            bottomSheetFragment.setOnSortSelectedListener { sortType ->
                // Change the current sort type
                currentSortType = sortType
                homeViewModel.updateSortType(currentSortType)

                // Manually scroll to the top of the RecyclerView
                binding.recipeRecyclerView.scrollToPosition(0)
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

