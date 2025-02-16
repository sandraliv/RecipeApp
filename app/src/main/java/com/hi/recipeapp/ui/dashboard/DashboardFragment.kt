package com.hi.recipeapp.ui.dashboard

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsetsAnimation
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.hi.recipeapp.databinding.FragmentDashboardBinding
import androidx.appcompat.widget.SearchView
import com.hi.recipeapp.classes.RecipeCard
import com.hi.recipeapp.ui.Networking.apiClient
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val dashboardViewModel =
            ViewModelProvider(this).get(DashboardViewModel::class.java)

        searchViewModel = ViewModelProvider(this).get(SearchViewModel::class.java)


        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        searchViewModel.searchResults.observe(viewLifecycleOwner, { results ->
            // Update the UI with the search results
            binding.textDashboard.text = results
        })

        val searchView: SearchView = binding.searchDashboard
        searchView.setIconified(false)
        searchView.setSuggestionsAdapter(null)
        searchView.clearFocus()
        searchView.setQueryHint("Search for recipes...")


        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean{
                query?.let {
                    fetchRecipesByQuery(it)
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

    private fun fetchRecipesByQuery(query: String) {
        apiClient.apiService.getRecipesByQuery(query).enqueue(object : Callback<List<RecipeCard>>{
            override fun onResponse(call: Call<List<RecipeCard>>, response: Response<List<RecipeCard>>) {
                if (response.isSuccessful) {
                    val recipes = response.body()
                    if (recipes != null && recipes.isNotEmpty()) {
                        recipes.forEach {
                            Log.d("DashboardFragment", "Recipe found: ${it.title}, Rating: ${it.rating}")
                        }
                        displayRecipes(recipes)
                    } else {
                        displayNoResultsMessage(query)
                    }
                } else {
                    Log.e("DashboardFragment", "API call unsuccessful: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<List<RecipeCard>>, t: Throwable) {
            Log.e("DashboardFragment", "API call failed: ${t.localizedMessage}")
        }
        })
    }
    private fun displayRecipes(recipes: List<RecipeCard>) {
        if (recipes.isEmpty()) {
            Log.d("DashboardFragment", "No recipes to display.")
            return
        }
        recipes.forEach {
            Log.d("DashboardFragment", "Recipe found: ${it.title}, Rating: ${it.rating}")
        }
        binding.textDashboard.text = "Found Recipe: ${recipes.first().title}"
    }

    private fun displayNoResultsMessage(query: String) {
        Log.d("DashboardFragment", "No recipes found for '$query'")
        binding.textDashboard.text = "No recipes found for '$query'"
        binding.textDashboard.visibility = View.VISIBLE
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}