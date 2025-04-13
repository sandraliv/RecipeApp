package com.hi.recipeapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.hi.recipeapp.classes.SessionManager
import com.hi.recipeapp.databinding.ActivityWelcomePageBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class WelcomePageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWelcomePageBinding
    private lateinit var navController: NavController
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
        navController = navHostFragment.navController

        // ✅ Define top-level destination(s)
        val appBarConfiguration = AppBarConfiguration(setOf(R.id.loginFragment))

        // ✅ Link toolbar with navigation
        setupActionBarWithNavController(navController, appBarConfiguration)

        // ✅ Optional: Hide toolbar on login screen
        navController.addOnDestinationChangedListener { _, destination, _ ->
            toolbar.visibility =
                if (destination.id == R.id.loginFragment) View.GONE else View.VISIBLE
        }

    }
    // ✅ Handle back arrow click
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}