package com.hi.recipeapp.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SearchViewModel : ViewModel (){

    private val _searchResults = MutableLiveData<String>()
    val searchResults: LiveData<String> get() = _searchResults

    fun updateSearchResults(results: String) {
        _searchResults.value = results  // Update the search results with the fetched data
    }
}