package com.hi.recipeapp.ui.settings

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.hi.recipeapp.databinding.FragmentSettingsBinding
import com.hi.recipeapp.ui.home.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
//This class extends Fragment(), meaning it represents a reusable UI component.
class SettingsFragment : Fragment() {

    // _binding holds the view binding reference for the fragment
    private var _binding: FragmentSettingsBinding? = null

    // binding is a non-nullable property, ensuring safe access to UI elements within the fragment's lifecycle
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val settingsViewModel: SettingsViewModel by viewModels() // Get ViewModel instance
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)

        return binding.root
    }

    // _binding is set to null in onDestroyView() to prevent memory leak
    //If an Android Fragment, memory leaks can occur if the fragments holds references to UI elemnts (like TextView, Buttons, etc) AFTER the view is destroyed.
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}