package com.hi.recipeapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.hi.recipeapp.databinding.ActivityWelcomePageBinding
import com.hi.recipeapp.ui.welcomepage.WelcomeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WelcomePageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWelcomePageBinding
    private val welcomeViewModel: WelcomeViewModel by viewModels() // ✅ Inject ViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomePageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupObservers()
        setupListeners()
    }

    private fun setupObservers() {
        welcomeViewModel.loginResult.observe(this) { user ->
            if (user != null) {
                showToast("Login successful! Welcome, ${user.username}")
                Log.d("LoginSuccess", "User: ${user.username}")

                // ✅ Navigate to MainActivity
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }

        welcomeViewModel.errorMessage.observe(this) { error ->
            error?.let {
                showToast(it)
                Log.e("LoginError", it)
            }
        }

        welcomeViewModel.isLoading.observe(this) { isLoading ->
            binding.loginBtn.isEnabled = !isLoading
        }
    }

    private fun setupListeners() {
        binding.loginBtn.setOnClickListener {
            val username = binding.usernameInput.text.toString().trim()
            val password = binding.passwordInput.text.toString().trim()
            welcomeViewModel.login(username, password) // ✅ Call ViewModel
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
