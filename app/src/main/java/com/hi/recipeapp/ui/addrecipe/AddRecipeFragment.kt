
package com.hi.recipeapp.ui.addrecipe

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.hi.recipeapp.R
import com.hi.recipeapp.classes.UserFullRecipe
import com.hi.recipeapp.databinding.FragmentAddRecipeBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddRecipeFragment : Fragment() {

    private val viewModel: AddRecipeViewModel by viewModels()
    private lateinit var binding: FragmentAddRecipeBinding
    private var ingredientCount = 1
    private var instructionCount = 1


    private val imageUris = mutableListOf<Uri>() // To store the selected image URIs

    private val selectImagesLauncher =
        registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
            if (uris != null) {
                imageUris.clear()
                imageUris.addAll(uris)
                // Update UI with the number of images selected
                binding.selectedImagesText.text = "${imageUris.size} images selected"
            }
        }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentAddRecipeBinding.inflate(inflater, container, false)


        // Event listeners
        binding.addIngredientButton.setOnClickListener { addIngredientRow() }
        binding.addInstructionButton.setOnClickListener { addInstructionRow() }
        binding.uploadPhotoButton.setOnClickListener { findNavController().navigate(R.id.action_addRecipeFragment_to_uploadPhotoFragment) }
        binding.addRecipeButton.setOnClickListener { submitRecipe() }

        return binding.root
    }
    private fun selectImages() {
        selectImagesLauncher.launch(arrayOf("image/*"))
    }

    // Adds a new ingredient row with checkboxes
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
            layoutParams = TableRow.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
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
    // Validates user input and creates a UserFullRecipe object
    private fun submitRecipe() {
        val title = binding.recipeTitleEditText.text.toString().trim()
        val description = binding.recipeDescriptionEditText.text.toString().trim()


        if (title.isEmpty() || description.isEmpty()) {
            Toast.makeText(requireContext(), "Title og description need to be filled", Toast.LENGTH_SHORT).show()
            return
        }


        // Get UserId

        // Retrieves the ingredient names and quantities from input fields and puts them into Map
        val ingredientsMap = mutableMapOf<String, String>()
        for (i in 0 until binding.ingredientsTableLayout.childCount) {
            val row = binding.ingredientsTableLayout.getChildAt(i) as? TableRow
            val ingredientName = (row?.getChildAt(0) as? EditText)?.text.toString().trim()
            val ingredientQuantity = (row?.getChildAt(1) as? EditText)?.text.toString().trim()

            if (ingredientName.isNotEmpty() && ingredientQuantity.isNotEmpty()) {
                ingredientsMap[ingredientName] = ingredientQuantity
            }
        }

        // Retrieves instructions from the input fields and stores them in a list
        val instructionsList = mutableListOf<String>()
        for (i in 0 until binding.instructionsTableLayout.childCount) {
            val row = binding.instructionsTableLayout.getChildAt(i) as? TableRow
            val instructionText = (row?.getChildAt(1) as? EditText)?.text.toString().trim()
            if (instructionText.isNotEmpty()) {
                instructionsList.add(instructionText)
            }
        }

        // UserFullRecipe object
        val recipe = UserFullRecipe(
            id = 0,
            title = title,
            description = description,
            ingredients = ingredientsMap,
            instructions = instructionsList.joinToString(". "),
            imageUrls = imageUris.map { it.toString() } // Convert URIs to strings
        )

        viewModel.uploadRecipe(recipe)



        // Lets user know that recipe is sent
        Toast.makeText(requireContext(), "Recipe sent!", Toast.LENGTH_SHORT).show()

    }


}
