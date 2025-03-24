package com.hi.recipeapp.ui.bottomsheetdialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hi.recipeapp.R
import com.hi.recipeapp.classes.SortType

class SortBottomSheetFragment : BottomSheetDialogFragment() {

    private lateinit var onSortSelected: (SortType) -> Unit

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

        view.findViewById<Button>(R.id.sortByRating).setOnClickListener {
            onSortSelected(SortType.RATING)
            dismiss()  // Dismiss the bottom sheet after selection
        }

        view.findViewById<Button>(R.id.sortByDate).setOnClickListener {
            onSortSelected(SortType.DATE)
            dismiss()  // Dismiss the bottom sheet after selection
        }
    }
}
