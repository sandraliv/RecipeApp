package com.hi.recipeapp.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.appcompat.widget.SearchView
import com.hi.recipeapp.databinding.FragmentDashboardBinding
import com.hi.recipeapp.ui.search.SearchViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val searchViewModel: SearchViewModel by viewModels() // ✅ Inject ViewModel via Hilt

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)

        setupSearchView()  // ✅ Encapsulate setup logic for better readability
        //Observe LiveData updates from ViewModel
        observeViewModel()

        return binding.root
    }

    private fun setupSearchView() {
        val searchView: SearchView = binding.searchDashboard
        searchView.isIconified = false
        searchView.setSuggestionsAdapter(null)
        searchView.clearFocus()
        searchView.queryHint = "Search for recipes..."

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    searchViewModel.searchByQuery(it)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })
    }

    private fun observeViewModel() {
        searchViewModel.searchResults.observe(viewLifecycleOwner) { results ->
            binding.textDashboard.text = results
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
