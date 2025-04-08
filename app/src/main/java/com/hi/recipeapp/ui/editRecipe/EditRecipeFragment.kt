package com.hi.recipeapp.ui.editRecipe

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.hi.recipeapp.R
import com.hi.recipeapp.classes.UserFullRecipe
import com.hi.recipeapp.databinding.FragmentAdminEditrecipeBinding
import com.hi.recipeapp.ui.home.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

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
        val toolbarTitle = requireActivity().findViewById<TextView>(R.id.titleTextView)
        toolbarTitle.text = "Edit Recipe"
        toolbarTitle.visibility = View.VISIBLE

        _binding = FragmentAdminEditrecipeBinding.inflate(inflater, container, false)
        homeViewModel.editRecipe(recipeId)

        binding.addIngredientButton.setOnClickListener {
            val somth = binding.ingredientsContainer

            val rowLayout = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = (8 * resources.displayMetrics.density).toInt()
                }
            }

            val ingredientInput = EditText(requireContext()).apply {
                hint = "Ingredient"
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }

            val quantityInput = EditText(requireContext()).apply {
                hint = "Quantity"
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }

            val deleteButton = ImageButton(requireContext()).apply {
                setImageResource(R.drawable.delete)
                layoutParams = LinearLayout.LayoutParams(
                    (48 * resources.displayMetrics.density).toInt(),  // width in px
                    (48 * resources.displayMetrics.density).toInt()   // height in px
                ).apply {
                    marginStart = (8 * resources.displayMetrics.density).toInt()
                }
                scaleType = ImageView.ScaleType.FIT_CENTER
                background = null
                contentDescription = "Delete ingredient"
                setOnClickListener {
                    somth.removeView(rowLayout)
                }
            }

            rowLayout.addView(ingredientInput)
            rowLayout.addView(quantityInput)
            rowLayout.addView(deleteButton)
            somth.addView(rowLayout)
        }



        homeViewModel.editableRecipe.observe(viewLifecycleOwner) { recipe ->
            binding.editTitle.setText(recipe.title)
            binding.editDescription.setText(recipe.description)
            binding.editInstructions.setText(recipe.instructions)

            val somth = binding.ingredientsContainer
            somth.removeAllViews() // Clear any previous dynamic views

            recipe.ingredients.forEach { (ingredient, quantity) ->
                val rowLayout = LinearLayout(requireContext()).apply {
                    orientation = LinearLayout.HORIZONTAL
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        bottomMargin = (8 * resources.displayMetrics.density).toInt()
                    }
                }

                val ingredientInput = EditText(requireContext()).apply {
                    hint = "Ingredient"
                    setText(ingredient)
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                }

                val quantityInput = EditText(requireContext()).apply {
                    hint = "Quantity"
                    setText(quantity)
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                }

                val deleteButton = ImageButton(requireContext()).apply {
                    setImageResource(R.drawable.delete)
                    layoutParams = LinearLayout.LayoutParams(
                        (48 * resources.displayMetrics.density).toInt(),  // width in dp
                        (48 * resources.displayMetrics.density).toInt()   // height in dp
                    ).apply {
                        marginStart = (8 * resources.displayMetrics.density).toInt()
                    }
                    scaleType = ImageView.ScaleType.FIT_CENTER
                    background = null
                    contentDescription = "Delete ingredient"
                    setOnClickListener {
                        somth.removeView(rowLayout)
                    }
                }

                // Now add views to the row (order matters)
                rowLayout.addView(ingredientInput)
                rowLayout.addView(quantityInput)
                rowLayout.addView(deleteButton)

                // Finally, add the row to the container
                somth.addView(rowLayout)
            }

            binding.saveRecipe.setOnClickListener {
                val current = homeViewModel.editableRecipe.value ?: return@setOnClickListener

                //Listinn af url-um getur verið tómur
                val safeImageUrls = current.imageUrls?.filter { it.isNotBlank() } ?: emptyList()

                    val updatedRecipe = UserFullRecipe(
                        id = current.id,
                        title = binding.editTitle.text.toString(),
                        description = binding.editDescription.text.toString(),
                        instructions = binding.editInstructions.text.toString(),
                        imageUrls =  safeImageUrls,
                        ingredients = collectIngredientMap()
                    )
                viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        val response = homeViewModel.patchRecipe(updatedRecipe)
                        if (response.isSuccessful) {
                            Toast.makeText(requireContext(), "Recipe updated!", Toast.LENGTH_SHORT).show()
                        } else {
                            Log.e("Update", "Error: ${response.code()} - ${response.message()}")
                            Toast.makeText(requireContext(), "Update failed", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Log.e("Update", "Exception: ${e.message}", e)
                        Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }

            }



        }
        return binding.root

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun collectIngredientMap(): Map<String, String> {
        val ingredientMap = mutableMapOf<String, String>()
        for (i in 0 until binding.ingredientsContainer.childCount) {
            val row = binding.ingredientsContainer.getChildAt(i) as LinearLayout

            val ingredientEdit = row.getChildAt(0) as EditText
            val quantityEdit = row.getChildAt(1) as EditText

            val ingredient = ingredientEdit.text.toString()
            val quantity = quantityEdit.text.toString()

            if (ingredient.isNotBlank() && quantity.isNotBlank()) {
                ingredientMap[ingredient] = quantity
            }
        }
        return ingredientMap
    }

}