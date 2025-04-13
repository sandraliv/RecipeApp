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

    private val args: CategoryFragmentArgs by navArgs()
     private val categoryName: String get() = args.categoryName
    private var currentSortType: SortType = SortType.RATING

    private val starSize = 30
    private val spaceBetweenStars = 3

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBycategoryBinding.inflate(inflater, container, false)

        val category = Category.valueOf(categoryName)

        binding.textCategoryName.text = category.getDisplayName()
        categoryViewModel.getRecipesByCategory(category)

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

        categoryViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }

        categoryViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                binding.progressBar.visibility = View.VISIBLE
                binding.recipeRecyclerView.visibility = View.GONE
                binding.textNoRecipeResults.visibility = View.GONE // Hide "No recipes available" while loading
            }
        }

        categoryViewModel.favoriteActionMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_SHORT).show()
            }
        }

        binding.recipeRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as GridLayoutManager
                val totalItemCount = layoutManager.itemCount
                val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()

                if (totalItemCount <= lastVisibleItemPosition + 2) {
                    if (!categoryViewModel.isLoading.value!!) {
                        categoryViewModel.loadMoreRecipes(Category.valueOf(categoryName))
                    }
                }
            }
        })

        return binding.root
    }

    /**
     * Sets up the functionality for the Sort button.
     * Opens a BottomSheet for selecting the sort type, and updates the category recipes based on the selected sort type.
     */
    private fun setupSortButton() {
        val sortButton = binding.sortByButton
        sortButton.setOnClickListener {
            val bottomSheetFragment = SortBottomSheetFragment()
            bottomSheetFragment.setCurrentSortType(currentSortType)
            bottomSheetFragment.setOnSortSelectedListener { sortType ->
                currentSortType = sortType
                val category = Category.valueOf(categoryName)
                categoryViewModel.updateSortType(category, currentSortType.name)
            }
            bottomSheetFragment.show(childFragmentManager, bottomSheetFragment.tag)
        }
    }


    /**
     * Sets up the functionality for the Category button.
     * Opens a BottomSheet to allow the user to select a new category.
     */
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
     * Navigates to the CategoryFragment with the selected category.
     *
     * @param category The selected category to navigate to.
     */
    private fun navigateToCategoryFragment(category: Category) {
        val action = CategoryFragmentDirections.actionCategoryFragmentToCategoryFragment(category.name)
        findNavController().navigate(action)
    }
}


