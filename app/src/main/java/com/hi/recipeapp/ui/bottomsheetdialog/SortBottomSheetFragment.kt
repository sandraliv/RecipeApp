package com.hi.recipeapp.ui.bottomsheetdialog

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
