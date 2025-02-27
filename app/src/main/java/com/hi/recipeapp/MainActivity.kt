package com.hi.recipeapp

import android.os.Bundle
import android.util.Log
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
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
                    R.id.navigation_notifications,
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
                            val dashboardFragment = supportFragmentManager.findFragmentById(R.id.navigation_search)
                            (dashboardFragment as? SearchFragment)?.resetSearchState()
                        }
                        true
                    }
                    R.id.navigation_notifications -> {
                        // Check if already on NotificationsFragment to prevent unnecessary action
                        if (navController.currentDestination?.id != R.id.navigation_notifications) {
                            navController.navigate(R.id.navigation_notifications)
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
