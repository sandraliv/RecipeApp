package com.hi.recipeapp.ui.addrecipe

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.hi.recipeapp.R
import com.hi.recipeapp.classes.FullRecipe
import com.hi.recipeapp.databinding.FragmentAddRecipeBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddRecipeFragment : Fragment() {

    private lateinit var binding: FragmentAddRecipeBinding
    private var ingredientCount = 1
    private var instructionCount = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentAddRecipeBinding.inflate(inflater, container, false)

        // Add Ingredient Row
        binding.addIngredientButton.setOnClickListener {
            addIngredientRow()
        }

        // Add Instruction Row
        binding.addInstructionButton.setOnClickListener {
            addInstructionRow()
        }

        return binding.root
    }

    // Function to add a new ingredient row with checkboxes
    private fun addIngredientRow() {
        val tableRow = TableRow(requireContext())

        // Ingredient Name EditText
        val ingredientNameEditText = EditText(requireContext()).apply {
            hint = "Ingredient Name"
            layoutParams = TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }

        // Ingredient Quantity EditText
        val ingredientQuantityEditText = EditText(requireContext()).apply {
            hint = "Quantity"
            layoutParams = TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }

        // Ingredient CheckBox for marking
        val ingredientCheckBox = CheckBox(requireContext())

        // Add views to the TableRow
        tableRow.addView(ingredientNameEditText)
        tableRow.addView(ingredientQuantityEditText)
        tableRow.addView(ingredientCheckBox)

        // Add the TableRow to the Ingredients Table
        binding.ingredientsTableLayout.addView(tableRow)

        ingredientCount++
    }

    // Function to add a new instruction row with numbered steps
    private fun addInstructionRow() {
        val tableRow = TableRow(requireContext())

        // Instruction Number TextView
        val instructionNumberTextView = TextView(requireContext()).apply {
            text = "$instructionCount."
            layoutParams = TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }

        // Instruction Text EditText
        val instructionTextEditText = EditText(requireContext()).apply {
            hint = "Instruction"
            layoutParams = TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }

        // Add views to the TableRow
        tableRow.addView(instructionNumberTextView)
        tableRow.addView(instructionTextEditText)

        // Add the TableRow to the Instructions Table
        binding.instructionsTableLayout.addView(tableRow)

        instructionCount++
    }
}