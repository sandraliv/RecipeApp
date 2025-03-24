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
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.hi.recipeapp.databinding.FragmentBycategoryBinding
import com.hi.recipeapp.ui.home.RecipeAdapter
import com.hi.recipeapp.classes.Category
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class CategoryFragment : Fragment() {

    private val categoryViewModel: CategoryViewModel by viewModels()
    private lateinit var recipeAdapter: RecipeAdapter
    private lateinit var binding: FragmentBycategoryBinding

    // Safe Args: Retrieve arguments passed to the fragment
    private val args: CategoryFragmentArgs by navArgs()
     private val categoryName: String get() = args.categoryName

    // Define star size and space between stars
    private val starSize = 30
    private val spaceBetweenStars = 3

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBycategoryBinding.inflate(inflater, container, false)

        // Convert the passed category name (string) back to the Category enum
        val category = Category.valueOf(categoryName)

        binding.textCategoryName.text = category.getDisplayName() // Set the category title here

        // Fetch recipes based on category (only once)
        categoryViewModel.getRecipesByCategory(category)

        // Initialize the adapter for displaying recipes
        recipeAdapter = RecipeAdapter(
            onClick = { recipe ->
                val recipeId = recipe.id
                val action = CategoryFragmentDirections.actionCategoryFragmentToFullRecipeFragment(recipeId)
                findNavController().navigate(action)
            },
            onFavoriteClick = { recipe, isFavorited ->
                categoryViewModel.updateFavoriteStatus(recipe, isFavorited)
            },
            starSize = starSize,  // Pass starSize
            spaceBetweenStars = spaceBetweenStars  // Pass spaceBetweenStars
        )

        // Set up GridLayoutManager with 2 columns (you can adjust the number of columns)
        val gridLayoutManager = GridLayoutManager(requireContext(), 2)
        binding.recipeRecyclerView.layoutManager = gridLayoutManager
        binding.recipeRecyclerView.adapter = recipeAdapter

        categoryViewModel.recipesByCategory.observe(viewLifecycleOwner) { recipes ->
            Log.d("CategoryFragment", "Received recipes: $recipes")
            if (recipes.isNotEmpty()) {
                binding.textNoRecipeResults.visibility = View.GONE
                binding.recipeRecyclerView.visibility = View.VISIBLE
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
                // Show the progress bar while loading
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

        return binding.root
    }
}
