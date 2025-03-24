package com.hi.recipeapp.ui.bottomsheetdialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hi.recipeapp.R
import com.hi.recipeapp.classes.SortType

class SortBottomSheetFragment : BottomSheetDialogFragment() {

    private lateinit var onSortSelected: (SortType) -> Unit
    private var currentSortType: SortType? = null  // Track the current sort type

    // Set the callback to handle the sort selection
    fun setOnSortSelectedListener(callback: (SortType) -> Unit) {
        this.onSortSelected = callback
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_sort, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Fetch the ImageViews for each sort icon
        val iconSortByRating = view.findViewById<ImageView>(R.id.iconSortByRating)
        val iconSortByDate = view.findViewById<ImageView>(R.id.iconSortByDate)

        // Set up button click listeners
        view.findViewById<Button>(R.id.sortByRating).setOnClickListener {
            // Update the current sort type
            currentSortType = SortType.RATING

            // Show the corresponding icon for the active sort
            iconSortByRating.visibility = View.VISIBLE
            iconSortByDate.visibility = View.GONE  // Hide the other icon

            // Pass the selected sort type back via the listener
            onSortSelected(SortType.RATING)
            dismiss()  // Dismiss the bottom sheet after selection
        }

        view.findViewById<Button>(R.id.sortByDate).setOnClickListener {
            // Update the current sort type
            currentSortType = SortType.DATE

            // Show the corresponding icon for the active sort
            iconSortByRating.visibility = View.GONE  // Hide the other icon
            iconSortByDate.visibility = View.VISIBLE

            // Pass the selected sort type back via the listener
            onSortSelected(SortType.DATE)
            dismiss()  // Dismiss the bottom sheet after selection
        }

        // Initially, show the correct icon based on the current sort type
        when (currentSortType) {
            SortType.RATING -> {
                iconSortByRating.visibility = View.VISIBLE
                iconSortByDate.visibility = View.GONE
            }
            SortType.DATE -> {
                iconSortByRating.visibility = View.GONE
                iconSortByDate.visibility = View.VISIBLE
            }
            else -> {
                iconSortByRating.visibility = View.GONE
                iconSortByDate.visibility = View.GONE
            }
        }
    }
}
