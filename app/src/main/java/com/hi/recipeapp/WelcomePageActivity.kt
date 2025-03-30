package com.hi.recipeapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import com.hi.recipeapp.classes.SessionManager
import com.hi.recipeapp.databinding.ActivityWelcomePageBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class WelcomePageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWelcomePageBinding
    //I need to have the session manager here for checking if a user is logged in, so that I can redirect him from the login page.
    @Inject
    lateinit var sessionManager: SessionManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (sessionManager.isUserLoggedIn()) {
            Log.d("TEST", "I AM LOGGED INN")
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        binding = ActivityWelcomePageBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // Initialize Toolbar
        val toolbar = binding.toolbar
        setSupportActionBar(toolbar)

        // Set up navigation
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // Set up ActionBar with Navigation Controller
        setupActionBarWithNavController(navController)
    }
}