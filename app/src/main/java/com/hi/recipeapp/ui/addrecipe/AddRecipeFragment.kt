package com.hi.recipeapp.ui.addrecipe

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.hi.recipeapp.R
import com.hi.recipeapp.classes.UserFullRecipe
import com.hi.recipeapp.databinding.FragmentAddRecipeBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AddRecipeFragment : Fragment() {

    private val viewModel: AddRecipeViewModel by viewModels()
    private lateinit var binding: FragmentAddRecipeBinding
    private var ingredientCount = 1
    private var instructionCount = 1
    private var userId: Int? = null  // Store userId from ViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentAddRecipeBinding.inflate(inflater, container, false)

        // Observe userId from ViewModel
        viewModel.userId.observe(viewLifecycleOwner) { id ->
            userId = id
        }


        binding.addIngredientButton.setOnClickListener { addIngredientRow() }
        binding.addInstructionButton.setOnClickListener { addInstructionRow() }
        binding.addRecipeButton.setOnClickListener { submitRecipe() }

        return binding.root
    }

    private fun addIngredientRow() {
        val tableRow = TableRow(requireContext())

        val ingredientNameEditText = EditText(requireContext()).apply {
            hint = "Ingredient Name"
            layoutParams = TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }

        val ingredientQuantityEditText = EditText(requireContext()).apply {
            hint = "Quantity"
            layoutParams = TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }

        val ingredientCheckBox = CheckBox(requireContext())

        tableRow.addView(ingredientNameEditText)
        tableRow.addView(ingredientQuantityEditText)
        tableRow.addView(ingredientCheckBox)

        binding.ingredientsTableLayout.addView(tableRow)

        ingredientCount++
    }

    private fun addInstructionRow() {
        val tableRow = TableRow(requireContext())

        val instructionNumberTextView = TextView(requireContext()).apply {
            text = "$instructionCount."
            layoutParams = TableRow.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        val instructionTextEditText = EditText(requireContext()).apply {
            hint = "Instruction"
            layoutParams = TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }

        tableRow.addView(instructionNumberTextView)
        tableRow.addView(instructionTextEditText)

        binding.instructionsTableLayout.addView(tableRow)

        instructionCount++
    }

    private fun submitRecipe() {
        val title = binding.recipeTitleEditText.text.toString().trim()
        val description = binding.recipeDescriptionEditText.text.toString().trim()

        if (title.isEmpty() || description.isEmpty()) {
            Toast.makeText(requireContext(), "Title and description need to be filled", Toast.LENGTH_SHORT).show()
            return
        }

        if (userId == null) {
            Toast.makeText(requireContext(), "User not found", Toast.LENGTH_SHORT).show()
            return
        }

        val ingredientsMap = mutableMapOf<String, String>()
        for (i in 0 until binding.ingredientsTableLayout.childCount) {
            val row = binding.ingredientsTableLayout.getChildAt(i) as? TableRow
            val ingredientName = (row?.getChildAt(0) as? EditText)?.text.toString().trim()
            val ingredientQuantity = (row?.getChildAt(1) as? EditText)?.text.toString().trim()

            if (ingredientName.isNotEmpty() && ingredientQuantity.isNotEmpty()) {
                ingredientsMap[ingredientName] = ingredientQuantity
            }
        }

        val instructionsList = mutableListOf<String>()
        for (i in 0 until binding.instructionsTableLayout.childCount) {
            val row = binding.instructionsTableLayout.getChildAt(i) as? TableRow
            val instructionText = (row?.getChildAt(1) as? EditText)?.text.toString().trim()
            if (instructionText.isNotEmpty()) {
                instructionsList.add(instructionText)
            }
        }

        val recipe = UserFullRecipe(
            id = 0,
            title = title,
            description = description,
            ingredients = ingredientsMap,
            instructions = instructionsList.joinToString(". "),
            imageUrl = "default"
        )

        viewModel.uploadRecipe(recipe)

        Toast.makeText(requireContext(), "Recipe sent!", Toast.LENGTH_SHORT).show()
    }
}
