package com.hi.recipeapp.ui.dashboard

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.hi.recipeapp.databinding.FragmentDashboardBinding
import androidx.appcompat.widget.SearchView
import com.hi.recipeapp.classes.RecipeCard
import com.hi.recipeapp.services.RecipeService
import com.hi.recipeapp.ui.search.SearchViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var searchViewModel: SearchViewModel
    private lateinit var recipeService: RecipeService

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val dashboardViewModel =
            ViewModelProvider(this)[DashboardViewModel::class.java]

        searchViewModel = ViewModelProvider(this)[SearchViewModel::class.java]
        recipeService = RecipeService()

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        //This subscribes the UI (Fragment) to the LiveData (searchResults).
        //Whenever searchResults changes, the observer will be notified automatically
        //The viewLifecyclyOwner ensures that the observer is only active while the fragments lifecycle is alive.
        //and also prevents memory leaks by automatically removing the observer when the fragment is destroyed.
        searchViewModel.searchResults.observe(viewLifecycleOwner, { results ->
            binding.textDashboard.text = results
        })

        val searchView: SearchView = binding.searchDashboard
        searchView.isIconified = false
        searchView.setSuggestionsAdapter(null)
        searchView.clearFocus()
        searchView.setQueryHint("Search for recipes...")


        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean{
                query?.let {
                    searchByQuery(it)
                }
                return true
            }
            override fun onQueryTextChange(newText: String?) : Boolean{

                return true
            }
        })

        val textView: TextView = binding.textDashboard
        dashboardViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root
    }

    private fun searchByQuery(query: String) {
        recipeService.searchRecipes(query) { recipes, error ->
            if (!recipes.isNullOrEmpty()) {
                val firstRecipe = recipes.first()
                val resultText = "Found Recipe: ${firstRecipe.title}\nRating: ${firstRecipe.averageRating}\nDescription: ${firstRecipe.description}"
                searchViewModel.updateSearchResults(resultText) // ✅ Use ViewModel to update UI
            } else {
                searchViewModel.updateSearchResults(error ?: "No recipes found for '$query'")
            }
        }
    }

}