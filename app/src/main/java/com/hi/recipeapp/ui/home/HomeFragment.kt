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
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.snackbar.Snackbar
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

    private var currentSortType: SortType = SortType.RATING // Declare currentSortType

    // Define star size and space between stars
    private val starSize = 30  // Example size for stars
    private val spaceBetweenStars = 3  // Example space between stars

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)


        // Initialize the adapter with the click listener and favorite click handler
        recipeAdapter = RecipeAdapter(
            onClick = { recipe ->
                val recipeId = recipe.id
                val action = HomeFragmentDirections.actionHomeFragmentToFullRecipeFragment(recipeId)
                findNavController().navigate(action)
            },
            onFavoriteClick = { recipe, isFavorited ->
                homeViewModel.updateFavoriteStatus(recipe, isFavorited)
            },
            starSize = starSize,  // Pass starSize
            spaceBetweenStars = spaceBetweenStars  // Pass spaceBetweenStars
        )


        // Set up GridLayoutManager with 2 columns (you can adjust the number of columns)
        val gridLayoutManager = GridLayoutManager(requireContext(), 2)
        binding.recipeRecyclerView.layoutManager = gridLayoutManager
        binding.recipeRecyclerView.adapter = recipeAdapter

        // Set up Category Button to open BottomSheet
        setupCategoryButton()

        setupSortButton()

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

        // Fetch recipes when fragment is created
        homeViewModel.fetchRecipesSortedBy(sortType = currentSortType)


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


        return binding.root
    }

    // Setup Sort Button with vector icon
    private fun setupSortButton() {
        val sortButton = binding.sortByButton
        sortButton.setOnClickListener {
            // Open the BottomSheetDialogFragment when the button is clicked
            val bottomSheetFragment = SortBottomSheetFragment()

            // Pass the callback to handle the sorting selection
            bottomSheetFragment.setOnSortSelectedListener { sortType ->
                currentSortType = sortType
                homeViewModel.fetchRecipesSortedBy(currentSortType)
            }

            // Show the Bottom Sheet
            bottomSheetFragment.show(childFragmentManager, bottomSheetFragment.tag)
        }
    }

    // Setup Category Button
    private fun setupCategoryButton() {

        val categoryButton = binding.categoryButton

        categoryButton.setOnClickListener {
            // Open the Category BottomSheetDialogFragment when the button is clicked
            val bottomSheetFragment = CategoryBottomSheetFragment()

            // Pass the callback to handle the category selection
            bottomSheetFragment.setOnCategorySelectedListener { category ->
                // Navigate to the Category Fragment when a category is selected
                navigateToCategoryFragment(category)
            }

            // Show the Bottom Sheet
            bottomSheetFragment.show(childFragmentManager, bottomSheetFragment.tag)
        }
    }



    private fun navigateToCategoryFragment(category: Category) {
        val action = HomeFragmentDirections.actionHomeFragmentToCategoryFragment(category.name)  // Pass category name
        findNavController().navigate(action)
    }

}

