package com.hi.recipeapp.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SearchViewModel : ViewModel (){

    //Holds search results
    private val _searchResults = MutableLiveData<String>()
    //Allows only reading of the search results to prevent accidental modifications from UI
    val searchResults: LiveData<String> get() = _searchResults

    //A function to update _searchResults, which will notify observers(UI) of the change.
    //The underscore is used to indicate that is should only be modified within the ViewModel,
    fun updateSearchResults(results: String) {
        _searchResults.value = results  // Update the search results with the fetched data
    }
}