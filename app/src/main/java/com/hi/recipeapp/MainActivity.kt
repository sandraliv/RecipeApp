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

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Hide the ActionBar
        supportActionBar?.hide()

        // Setup Toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val navView: BottomNavigationView = binding.navView

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main) as? NavHostFragment
        if (navHostFragment != null) {
            navController = navHostFragment.navController

            val appBarConfiguration = AppBarConfiguration(
                setOf(
                    R.id.navigation_home,
                    R.id.navigation_search,
                    R.id.navigation_myrecipes,
                    R.id.navigation_settings
                )
            )
            setupActionBarWithNavController(navController, appBarConfiguration)

            navView.setupWithNavController(navController)

            navView.setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.navigation_home -> {
                        if (navController.currentDestination?.id != R.id.navigation_home) {
                            navController.navigate(R.id.navigation_home)
                        }
                        true
                    }
                    R.id.navigation_search -> {
                        if (navController.currentDestination?.id != R.id.navigation_search) {
                            navController.navigate(R.id.navigation_search)
                        } else {
                            navController.popBackStack(R.id.navigation_search, false)
                            val searchFragment = supportFragmentManager.findFragmentById(R.id.navigation_search)
                            (searchFragment as? SearchFragment)?.resetSearchState()
                        }
                        true
                    }

                    R.id.navigation_add_recipe -> {
                        if (navController.currentDestination?.id != R.id.AddRecipeFragment) {
                            navController.navigate(R.id.AddRecipeFragment)
                        }
                        true
                    }

                    R.id.navigation_myrecipes -> {
                        if (navController.currentDestination?.id != R.id.navigation_myrecipes) {
                            navController.navigate(R.id.navigation_myrecipes)
                        }
                        true
                    }
                    R.id.navigation_settings -> {
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
