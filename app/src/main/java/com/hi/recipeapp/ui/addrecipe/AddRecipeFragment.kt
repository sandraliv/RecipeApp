package com.hi.recipeapp.ui.addrecipe

import android.app.AlertDialog
import android.content.ContentValues
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.hi.recipeapp.R
import com.hi.recipeapp.classes.UserFullRecipe
import com.hi.recipeapp.databinding.FragmentAddRecipeBinding
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

@AndroidEntryPoint
class AddRecipeFragment : Fragment() {

    private val viewModel: AddRecipeViewModel by viewModels()
    private lateinit var binding: FragmentAddRecipeBinding
    private var ingredientCount = 1
    private var instructionCount = 1
    private val imageUris = mutableListOf<Uri>()
    private lateinit var photoUri: Uri

    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                imageUris.clear()
                imageUris.add(it)
                binding.selectedImagesText.text = "1 image selected"
            }
        }

    private val takePhotoLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                imageUris.clear()
                imageUris.add(photoUri)
                binding.selectedImagesText.text = "1 image selected"
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddRecipeBinding.inflate(inflater, container, false)

        val toolbarTitle = requireActivity().findViewById<TextView>(R.id.titleTextView)
        toolbarTitle.text = "Add Your Own Recipe"
        toolbarTitle.visibility = View.VISIBLE

        binding.addIngredientButton.setOnClickListener { addIngredientRow() }
        binding.addInstructionButton.setOnClickListener { addInstructionRow() }
        binding.uploadPhotoContainer.setOnClickListener { showPhotoDialog() }
        binding.addRecipeButton.setOnClickListener { submitRecipe() }

        return binding.root
    }

    /**
     * A method to show the photo dialog where user can choose to take a photo,
     * choose a photo from library og to cancel
     */
    private fun showPhotoDialog() {
        val options = arrayOf("Take Photo", "Choose from Gallery", "Cancel")
        AlertDialog.Builder(requireContext())
            .setTitle("Select Option")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> openCamera()
                    1 -> openGallery()
                    2 -> dialog.dismiss()
                }
            }
            .show()
    }

    /**
     * A method for opening the camera. The camera is given a photoUri to add the photo.
     * The function creates a content URI (photoUri) for where the image will be saved.
     *
     * That URI is passed into the takePhotoLauncher.launch(photoUri) to tell the camera
     * where to save the photo after it’s taken.
     */
    private fun openCamera() {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "recipe_${System.currentTimeMillis()}.jpg")
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
        }
        photoUri = requireContext().contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)!!
        takePhotoLauncher.launch(photoUri)
    }

    private fun openGallery() {
        galleryLauncher.launch("image/*")
    }

    /**
     * When user pressed "add ingredient" button in the view, this method runs and dynamically adds a row
     *
     */
    private fun addIngredientRow() {
        val tableRow = TableRow(requireContext())
        val ingredientName = EditText(requireContext()).apply {
            hint = "Ingredient Name"
            layoutParams = TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }
        val ingredientQty = EditText(requireContext()).apply {
            hint = "Quantity"
            layoutParams = TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }
        val checkBox = CheckBox(requireContext())
        tableRow.addView(ingredientName)
        tableRow.addView(ingredientQty)
        tableRow.addView(checkBox)
        binding.ingredientsTableLayout.addView(tableRow)
        ingredientCount++
    }

    /**
     * When user pressed "add instruction" button in the view, this method runs and dynamically adds a row
     */
    private fun addInstructionRow() {
        val tableRow = TableRow(requireContext())
        val numberView = TextView(requireContext()).apply {
            text = "$instructionCount."
            layoutParams = TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
        val instruction = EditText(requireContext()).apply {
            hint = "Instruction"
            layoutParams = TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }
        tableRow.addView(numberView)
        tableRow.addView(instruction)
        binding.instructionsTableLayout.addView(tableRow)
        instructionCount++
    }

    /**
     * This method runs when an admin presses a button to add his recipe to the database.
     */
    private fun submitRecipe() {
        val title = binding.recipeTitleEditText.text.toString().trim()
        val description = binding.recipeDescriptionEditText.text.toString().trim()
        if (title.isEmpty() || description.isEmpty()) {
            Toast.makeText(requireContext(), "Title og description þarf að fylla út", Toast.LENGTH_SHORT).show()
            return
        }
        val ingredients = mutableMapOf<String, String>()
        for (i in 0 until binding.ingredientsTableLayout.childCount) {
            val row = binding.ingredientsTableLayout.getChildAt(i) as TableRow
            val name = (row.getChildAt(0) as? EditText)?.text.toString().trim()
            val qty = (row.getChildAt(1) as? EditText)?.text.toString().trim()
            if (name.isNotEmpty() && qty.isNotEmpty()) {
                ingredients[name] = qty
            }
        }
        val instructions = mutableListOf<String>()
        for (i in 0 until binding.instructionsTableLayout.childCount) {
            val row = binding.instructionsTableLayout.getChildAt(i) as TableRow
            val step = (row.getChildAt(1) as? EditText)?.text.toString().trim()
            if (step.isNotEmpty()) instructions.add(step)
        }
        val recipe = UserFullRecipe(
            id = 0,
            title = title,
            description = description,
            ingredients = ingredients,
            instructions = instructions.joinToString(". "),
            imageUrls = imageUris.map { it.toString() }
        )
        viewModel.uploadRecipe(recipe)
        Toast.makeText(requireContext(), "Recipe sent", Toast.LENGTH_SHORT).show()

        findNavController().navigate(R.id.action_addRecipeFragment_to_navigation_myrecipes)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        val toolbarTitle = requireActivity().findViewById<TextView>(R.id.titleTextView)
        toolbarTitle.visibility = View.GONE
    }
}


