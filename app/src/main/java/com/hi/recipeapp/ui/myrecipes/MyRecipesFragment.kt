package com.hi.recipeapp.ui.myrecipes

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.hi.recipeapp.classes.UserRecipeCard
import com.hi.recipeapp.databinding.FragmentMyRecipesBinding
import com.hi.recipeapp.ui.home.HomeViewModel
import com.hi.recipeapp.ui.home.RecipeAdapter
import com.hi.recipeapp.ui.search.SearchFragmentDirections
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MyRecipesFragment : Fragment() {

    private lateinit var binding: FragmentMyRecipesBinding
    private val myRecipesViewModel: MyRecipesViewModel by viewModels()
    private lateinit var recipeAdapter: RecipeAdapter
    private lateinit var userRecipeAdapter: UserRecipeAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMyRecipesBinding.inflate(inflater, container, false)

        // Initialize the adapter for both RecyclerViews
        recipeAdapter = RecipeAdapter(
            onClick = { recipe ->
                val recipeId = recipe.id
                val action = MyRecipesFragmentDirections.actionMyRecipesFragmentToFullRecipeFragment(recipeId)
                findNavController().navigate(action)
            },
            onFavoriteClick = { recipe, isFavorited ->
                myRecipesViewModel.updateFavoriteStatus(recipe, isFavorited)
            }
        )

        userRecipeAdapter = UserRecipeAdapter { userRecipe ->
            val recipeId = userRecipe.id
            val action = MyRecipesFragmentDirections.actionMyRecipesFragmentToUserFullRecipeFragment(recipeId)
            findNavController().navigate(action)
        }

        setupRecyclerView()
        setupButtonListeners()
        observeViewModel()

        // Observe favorite action message
        myRecipesViewModel.favoriteActionMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                // Show the message using Snackbar
                Snackbar.make(binding.root, it, Snackbar.LENGTH_SHORT).show()
            }
        }

        return binding.root
    }

    private fun setupButtonListeners() {
        binding.favoritesButton.setOnClickListener {
            binding.favoriteRecipeRecyclerView.visibility = View.VISIBLE
            binding.userRecipesRecyclerView.visibility = View.GONE
            myRecipesViewModel.fetchFavoriteRecipes()
        }

        binding.myRecipesButton.setOnClickListener {
            binding.favoriteRecipeRecyclerView.visibility = View.GONE
            binding.userRecipesRecyclerView.visibility = View.VISIBLE
            myRecipesViewModel.fetchUserRecipes(0, 10)
        }
    }

    private fun setupRecyclerView() {
        binding.favoriteRecipeRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = recipeAdapter
        }

        binding.userRecipesRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = userRecipeAdapter
        }
    }

    private fun observeViewModel() {
        myRecipesViewModel.favoriteRecipes.observe(viewLifecycleOwner) { recipes ->
            if (recipes != null) {
                recipeAdapter.submitList(recipes)
            } else {
                Toast.makeText(requireContext(), "No favorite recipes found.", Toast.LENGTH_SHORT).show()
            }
        }

        myRecipesViewModel.userRecipes.observe(viewLifecycleOwner) { userrecipes ->
            if (userrecipes != null) {
                userRecipeAdapter.submitList(userrecipes)
            } else {
                Toast.makeText(requireContext(), "No recipes found.", Toast.LENGTH_SHORT).show()
            }
        }

        myRecipesViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        myRecipesViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
