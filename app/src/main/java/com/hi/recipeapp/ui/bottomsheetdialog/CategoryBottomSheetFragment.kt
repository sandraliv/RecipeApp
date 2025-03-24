package com.hi.recipeapp.ui.bottomsheetdialog


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hi.recipeapp.R
import com.hi.recipeapp.classes.Category

class CategoryBottomSheetFragment : BottomSheetDialogFragment() {

    private lateinit var onCategorySelected: (Category) -> Unit

    // Set the callback to handle the category selection
    fun setOnCategorySelectedListener(callback: (Category) -> Unit) {
        this.onCategorySelected = callback
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_category, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.categoryBreakfast).setOnClickListener {
            onCategorySelected(Category.BREAKFAST)
            dismiss()  // Dismiss the bottom sheet after selection
        }

        view.findViewById<Button>(R.id.categoryAppetizer).setOnClickListener {
            onCategorySelected(Category.APPETIZER)
            dismiss()
        }

        view.findViewById<Button>(R.id.categoryMainCourse).setOnClickListener {
            onCategorySelected(Category.MAIN_COURSE)
            dismiss()
        }

        view.findViewById<Button>(R.id.categorySnack).setOnClickListener {
            onCategorySelected(Category.SNACK)
            dismiss()
        }

        view.findViewById<Button>(R.id.categoryDessert).setOnClickListener {
            onCategorySelected(Category.DESSERT)
            dismiss()
        }

        view.findViewById<Button>(R.id.categoryBaking).setOnClickListener {
            onCategorySelected(Category.BAKING)
            dismiss()
        }
    }
}
