package com.hi.recipeapp.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SettingsViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is a Settings Fragment"
    }
    val text: LiveData<String> = _text

    // Add this method to allow updating the text from the fragment
    fun updateText(newText: String) {
        _text.value = newText
    }
}