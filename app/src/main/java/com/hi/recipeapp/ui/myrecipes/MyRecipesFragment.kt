package com.hi.recipeapp.ui.myrecipes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.hi.recipeapp.databinding.FragmentMyRecipesBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
//This class extends Fragment(), meaning it represents a reusable UI component.
class MyRecipesFragment : Fragment() {

    // _binding holds the view binding reference for the fragment
    private var _binding: FragmentMyRecipesBinding? = null

    // binding is a non-nullable property, ensuring safe access to UI elements within the fragment's lifecycle
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val myRecipesViewModel =
            ViewModelProvider(this)[MyRecipesViewModel::class.java]

        _binding = FragmentMyRecipesBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textMyRecipes
        myRecipesViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root
    }

    // _binding is set to null in onDestroyView() to prevent memory leak
    //If an Android Fragment, memory leaks can occur if the fragments holds references to UI elemnts (like TextView, Buttons, etc) AFTER the view is destroyed.
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}