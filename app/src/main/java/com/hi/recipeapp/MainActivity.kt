package com.hi.recipeapp

import android.os.Bundle
import android.util.Log
import android.widget.Button
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.hi.recipeapp.databinding.ActivityMainBinding
import com.hi.recipeapp.ui.search.SearchFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Force dark mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Hide the ActionBar
        supportActionBar?.hide()

        // Setup Toolbar (if you want to use a Toolbar)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Show the back arrow (up button)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false) // Hide title text

        val navView: BottomNavigationView = binding.navView

        // ✅ Get NavController safely
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main) as? NavHostFragment
        if (navHostFragment != null) {
            navController = navHostFragment.navController

            // ✅ Setup ActionBar with Navigation
            val appBarConfiguration = AppBarConfiguration(
                setOf(
                    R.id.navigation_home,
                    R.id.navigation_search,
                    R.id.navigation_myrecipes,
                    R.id.navigation_settings
                )
            )
            setupActionBarWithNavController(navController, appBarConfiguration)

            // ✅ Connect BottomNavigationView with NavController
            navView.setupWithNavController(navController)

            // ✅ Handle Bottom Navigation Item Selection
            navView.setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.navigation_home -> {
                        // Check if already on HomeFragment to prevent unnecessary action
                        if (navController.currentDestination?.id != R.id.navigation_home) {
                            navController.navigate(R.id.navigation_home)
                        }
                        true
                    }
                    R.id.navigation_search -> {
                        // If we are on a different fragment, navigate to Dashboard directly
                        if (navController.currentDestination?.id != R.id.navigation_search) {
                            navController.navigate(R.id.navigation_search)
                        } else {
                            // If we are already on the Dashboard, reset it manually
                            // Pop all fragments from the back stack, ensuring we're at the initial state
                            navController.popBackStack(R.id.navigation_search, false)
                            // Optionally, reset any other states in the fragment
                            val searchFragment = supportFragmentManager.findFragmentById(R.id.navigation_search)
                            (searchFragment as? SearchFragment)?.resetSearchState()
                        }
                        true
                    }

                    R.id.navigation_add_recipe -> { // ✅ Plús takkinn fer í AddRecipeFragment
                        if (navController.currentDestination?.id != R.id.AddRecipeFragment) {
                            navController.navigate(R.id.AddRecipeFragment)
                        }
                        true
                    }

                    R.id.navigation_myrecipes -> {
                        // Check if already on NotificationsFragment to prevent unnecessary action
                        if (navController.currentDestination?.id != R.id.navigation_myrecipes) {
                            navController.navigate(R.id.navigation_myrecipes)
                        }
                        true
                    }
                    R.id.navigation_settings -> {
                        // Check if already on SettingsFragment to prevent unnecessary action
                        if (navController.currentDestination?.id != R.id.navigation_settings) {
                            navController.navigate(R.id.navigation_settings)
                        }
                        true
                    }
                    else -> false
                }
            }
        } else {
            Log.e("MainActivity", "NavHostFragment not found!")
        }


    }


    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
