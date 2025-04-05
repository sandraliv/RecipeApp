package com.hi.recipeapp.ui.bycategory

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.hi.recipeapp.databinding.FragmentBycategoryBinding
import com.hi.recipeapp.ui.home.RecipeAdapter
import com.hi.recipeapp.classes.Category
import com.hi.recipeapp.classes.SortType
import com.hi.recipeapp.ui.bottomsheetdialog.CategoryBottomSheetFragment
import com.hi.recipeapp.ui.bottomsheetdialog.SortBottomSheetFragment
import com.hi.recipeapp.ui.home.HomeViewModel
import com.hi.recipeapp.ui.search.SearchFragmentDirections
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class CategoryFragment : Fragment() {

    private val categoryViewModel: CategoryViewModel by viewModels()
    private lateinit var recipeAdapter: RecipeAdapter
    private lateinit var binding: FragmentBycategoryBinding

    // Safe Args: Retrieve arguments passed to the fragment
    private val args: CategoryFragmentArgs by navArgs()
     private val categoryName: String get() = args.categoryName
    private var currentSortType: SortType = SortType.RATING

    // Define star size and space between stars
    private val starSize = 30
    private val spaceBetweenStars = 3

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBycategoryBinding.inflate(inflater, container, false)

        val category = Category.valueOf(categoryName)

        binding.textCategoryName.text = category.getDisplayName()

        // Fetch recipes based on category (only once)
        categoryViewModel.getRecipesByCategory(category)

        // Initialize the adapter for displaying recipes
        recipeAdapter =
            RecipeAdapter(
                onClick = { recipe ->
                    val recipeId = recipe.id
                    val action = CategoryFragmentDirections.actionCategoryFragmentToFullRecipeFragment(recipeId)
                    findNavController().navigate(action)
                },
                onFavoriteClick = { recipe, isFavorited ->
                    categoryViewModel.updateFavoriteStatus(recipe, isFavorited)
                },
                starSize = starSize,  // Pass starSize
                spaceBetweenStars = spaceBetweenStars,  // Pass spaceBetweenStars
                isAdmin = false,
                onDeleteClick = {},
                onEditClick = {}
            )


        // Set up GridLayoutManager with 2 columns (you can adjust the number of columns)
        val gridLayoutManager = GridLayoutManager(requireContext(), 2)
        binding.recipeRecyclerView.layoutManager = gridLayoutManager
        binding.recipeRecyclerView.adapter = recipeAdapter

        setupSortButton()
        setupCategoryButton()

        categoryViewModel.recipesByCategory.observe(viewLifecycleOwner) { recipes ->
            Log.d("CategoryFragment", "Received recipes: $recipes")
            if (recipes.isNotEmpty()) {
                binding.textNoRecipeResults.visibility = View.GONE
                binding.recipeRecyclerView.visibility = View.VISIBLE
                binding.progressBar.visibility = View.GONE
                recipeAdapter.submitList(recipes)
            } else {
                binding.textCategoryName.visibility = View.VISIBLE
                binding.recipeRecyclerView.visibility = View.GONE
            }
        }

        // Observe error messages
        categoryViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }

        // Observe loading state and show progress bar while loading
        categoryViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                binding.progressBar.visibility = View.VISIBLE
                binding.recipeRecyclerView.visibility = View.GONE
                binding.textNoRecipeResults.visibility = View.GONE // Hide "No recipes available" while loading
            }
        }

        // Observe the favorite action message LiveData
        categoryViewModel.favoriteActionMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_SHORT).show()
            }
        }


        // Detect if the user has scrolled to the bottom of the RecyclerView
        binding.recipeRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as GridLayoutManager
                val totalItemCount = layoutManager.itemCount
                val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()

                // If the user has scrolled to the bottom, load more recipes
                if (totalItemCount <= lastVisibleItemPosition + 2) {  // 2 is just an offset to trigger early
                    if (!categoryViewModel.isLoading.value!!) {
                        categoryViewModel.loadMoreRecipes(Category.valueOf(categoryName))
                    }
                }
            }
        })

        return binding.root
    }

    // Setup Sort Button functionality
    private fun setupSortButton() {
        val sortButton = binding.sortByButton
        sortButton.setOnClickListener {
            val bottomSheetFragment = SortBottomSheetFragment()

            // Pass the current sort type to the bottom sheet
            bottomSheetFragment.setCurrentSortType(currentSortType)

            // Pass the callback to handle the sorting selection
            bottomSheetFragment.setOnSortSelectedListener { sortType ->
                currentSortType = sortType
                val category = Category.valueOf(categoryName)

                // Update the sort type in the view model along with the current category
                categoryViewModel.updateSortType(category, currentSortType.name)
            }

            // Show the Bottom Sheet
            bottomSheetFragment.show(childFragmentManager, bottomSheetFragment.tag)
        }
    }


    // Setup Category Button functionality
    private fun setupCategoryButton() {
        val categoryButton = binding.categoryButton
        categoryButton.setOnClickListener {
            val bottomSheetFragment = CategoryBottomSheetFragment()

            // Pass the callback to handle the category selection
            bottomSheetFragment.setOnCategorySelectedListener { category ->
                navigateToCategoryFragment(category)
            }

            bottomSheetFragment.show(childFragmentManager, bottomSheetFragment.tag)
        }
    }

    // Navigate to CategoryFragment with the selected category
    private fun navigateToCategoryFragment(category: Category) {
        val action = CategoryFragmentDirections.actionCategoryFragmentToCategoryFragment(category.name)
        findNavController().navigate(action)
    }
}


