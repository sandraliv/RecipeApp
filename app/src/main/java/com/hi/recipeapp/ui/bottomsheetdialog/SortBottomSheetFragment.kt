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
    private var currentSortType: SortType? = null


    fun setOnSortSelectedListener(callback: (SortType) -> Unit) {
        this.onSortSelected = callback
    }


    fun setCurrentSortType(sortType: SortType) {
        currentSortType = sortType
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_sort, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val iconSortByRating = view.findViewById<ImageView>(R.id.iconSortByRating)
        val iconSortByDate = view.findViewById<ImageView>(R.id.iconSortByDate)

        view.findViewById<Button>(R.id.sortByRating).setOnClickListener {
            currentSortType = SortType.RATING
            iconSortByRating.visibility = View.VISIBLE
            iconSortByDate.visibility = View.INVISIBLE
            onSortSelected(SortType.RATING)
            dismiss()
        }

        view.findViewById<Button>(R.id.sortByDate).setOnClickListener {
            currentSortType = SortType.DATE
            iconSortByRating.visibility = View.INVISIBLE
            iconSortByDate.visibility = View.VISIBLE

            onSortSelected(SortType.DATE)
            dismiss()
        }

        // Set initial icon visibility based on currentSortType
        when (currentSortType) {
            SortType.RATING -> {
                iconSortByRating.visibility = View.VISIBLE
                iconSortByDate.visibility = View.INVISIBLE
            }
            SortType.DATE -> {
                iconSortByRating.visibility = View.INVISIBLE
                iconSortByDate.visibility = View.VISIBLE
            }
            else -> {
                iconSortByRating.visibility = View.INVISIBLE
                iconSortByDate.visibility = View.INVISIBLE
            }
        }
    }
}
