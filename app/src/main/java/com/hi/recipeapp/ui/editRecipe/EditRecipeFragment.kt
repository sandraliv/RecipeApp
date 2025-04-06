package com.hi.recipeapp.ui.editRecipe

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.hi.recipeapp.R
import com.hi.recipeapp.databinding.FragmentAdminEditrecipeBinding
import com.hi.recipeapp.ui.home.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EditRecipeFragment : Fragment() {

    private val homeViewModel: HomeViewModel by viewModels()
    private var _binding: FragmentAdminEditrecipeBinding? = null
    private val binding get() = _binding!!
    private val args: EditRecipeFragmentArgs by navArgs()
    private val recipeId: Int get() = args.recipeId


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminEditrecipeBinding.inflate(inflater, container, false)
        homeViewModel.editRecipe(recipeId)

        // Step 2: Observe the recipe and populate the UI
        homeViewModel.editableRecipe.observe(viewLifecycleOwner) { recipe ->
            binding.editTitle.setText(recipe.title)
            binding.editDescription.setText(recipe.description)
            binding.editInstructions.setText(recipe.instructions)
            Log.d("INGREDIENT", recipe.instructions)
            recipe.ingredients.forEach { (ingredient, quantity) ->
                Log.d("INGREDIENT", ingredient)
                Log.d("INGREDIENT", quantity)
                val editText = EditText(requireContext()).apply {
                    val formattedText = getString(R.string.ingredient_format, ingredient, quantity)
                    setText(formattedText)
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        bottomMargin = (8 * resources.displayMetrics.density).toInt()

                    }
                }
                binding.ingredientsContainer.addView(editText)
            }

            // Add more fields like image, etc., if needed
        }
        return binding.root

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}