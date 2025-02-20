package com.hi.recipeapp

import android.os.Bundle
import android.util.Log
import android.widget.Button
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.hi.recipeapp.databinding.ActivityMainBinding
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
                    R.id.navigation_dashboard,
                    R.id.navigation_notifications,
                    R.id.navigation_settings
                )
            )
            setupActionBarWithNavController(navController, appBarConfiguration)

            // ✅ Connect BottomNavigationView with NavController
            navView.setupWithNavController(navController)
        } else {
            Log.e("MainActivity", "NavHostFragment not found!")
        }
        val button = findViewById<Button>(R.id.home_button)
    }
}