package com.hi.recipeapp.ui.bottomsheetdialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
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

        // Fetch the ImageViews for each category checkmark
        val checkmarkBreakfast = view.findViewById<ImageView>(R.id.iconBreakfast)
        val checkmarkAppetizer = view.findViewById<ImageView>(R.id.iconAppetizer)
        val checkmarkMainCourse = view.findViewById<ImageView>(R.id.iconMainCourse)
        val checkmarkSnack = view.findViewById<ImageView>(R.id.iconSnack)
        val checkmarkDessert = view.findViewById<ImageView>(R.id.iconDessert)
        val checkmarkBaking = view.findViewById<ImageView>(R.id.iconBaking)

        // Set up button click listeners and control checkmark visibility
        view.findViewById<Button>(R.id.categoryBreakfast).setOnClickListener {
            onCategorySelected(Category.BREAKFAST)
            // Show the checkmark for Breakfast and hide others
            checkmarkBreakfast.visibility = View.VISIBLE
            checkmarkAppetizer.visibility = View.INVISIBLE
            checkmarkMainCourse.visibility = View.INVISIBLE
            checkmarkSnack.visibility = View.INVISIBLE
            checkmarkDessert.visibility = View.INVISIBLE
            checkmarkBaking.visibility = View.INVISIBLE
            dismiss()
        }

        view.findViewById<Button>(R.id.categoryAppetizer).setOnClickListener {
            onCategorySelected(Category.APPETIZER)
            // Show the checkmark for Appetizer and hide others
            checkmarkAppetizer.visibility = View.VISIBLE
            checkmarkBreakfast.visibility = View.INVISIBLE
            checkmarkMainCourse.visibility = View.INVISIBLE
            checkmarkSnack.visibility = View.INVISIBLE
            checkmarkDessert.visibility = View.INVISIBLE
            checkmarkBaking.visibility = View.INVISIBLE
            dismiss()
        }

        view.findViewById<Button>(R.id.categoryMainCourse).setOnClickListener {
            onCategorySelected(Category.MAIN_COURSE)
            // Show the checkmark for MainCourse and hide others
            checkmarkMainCourse.visibility = View.VISIBLE
            checkmarkBreakfast.visibility = View.INVISIBLE
            checkmarkAppetizer.visibility = View.INVISIBLE
            checkmarkSnack.visibility = View.INVISIBLE
            checkmarkDessert.visibility = View.INVISIBLE
            checkmarkBaking.visibility = View.INVISIBLE
            dismiss()
        }

        view.findViewById<Button>(R.id.categorySnack).setOnClickListener {
            onCategorySelected(Category.SNACK)
            // Show the checkmark for Snack and hide others
            checkmarkSnack.visibility = View.VISIBLE
            checkmarkBreakfast.visibility = View.INVISIBLE
            checkmarkAppetizer.visibility = View.INVISIBLE
            checkmarkMainCourse.visibility = View.INVISIBLE
            checkmarkDessert.visibility = View.INVISIBLE
            checkmarkBaking.visibility = View.INVISIBLE
            dismiss()
        }

        view.findViewById<Button>(R.id.categoryDessert).setOnClickListener {
            onCategorySelected(Category.DESSERT)
            // Show the checkmark for Dessert and hide others
            checkmarkDessert.visibility = View.VISIBLE
            checkmarkBreakfast.visibility = View.INVISIBLE
            checkmarkAppetizer.visibility = View.INVISIBLE
            checkmarkMainCourse.visibility = View.INVISIBLE
            checkmarkSnack.visibility = View.INVISIBLE
            checkmarkBaking.visibility = View.INVISIBLE
            dismiss()
        }

        view.findViewById<Button>(R.id.categoryBaking).setOnClickListener {
            onCategorySelected(Category.BAKING)
            // Show the checkmark for Baking and hide others
            checkmarkBaking.visibility = View.VISIBLE
            checkmarkBreakfast.visibility = View.INVISIBLE
            checkmarkAppetizer.visibility = View.INVISIBLE
            checkmarkMainCourse.visibility = View.INVISIBLE
            checkmarkSnack.visibility = View.INVISIBLE
            checkmarkDessert.visibility = View.INVISIBLE
            dismiss()
        }
    }
}
