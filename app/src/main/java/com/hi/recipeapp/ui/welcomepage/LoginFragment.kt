package com.hi.recipeapp.ui.welcomepage

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.hi.recipeapp.MainActivity
import com.hi.recipeapp.R
import com.hi.recipeapp.databinding.FragmentLoginBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val LoginViewModel: LoginViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()
        setupListeners()
    }

    /**
     * Observes changes from the ViewModel:
     * - If login is successful, shows welcome message and navigates to MainActivity.
     * - If login fails, shows error message.
     * - Disables login button while loading.
     */
    private fun setupObservers() {
        LoginViewModel.loginResult.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                Toast.makeText(
                    requireContext(),
                    "Login successful! Welcome, ${user.username}",
                    Toast.LENGTH_SHORT
                ).show()
                Log.d("LoginSuccess", "User: ${user.username}")


                startActivity(Intent(requireContext(), MainActivity::class.java))
                requireActivity().finish()
            }
        }

        LoginViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                Log.e("LoginError", it)
            }
        }

        LoginViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.loginBtn.isEnabled = !isLoading
        }
    }

    /**
     * Sets up listeners for user interactions:
     * - When the login button is pressed, the app reads the username and password input fields,
     *   and calls the ViewModel’s login function to attempt logging in.
     * - When you click on "Sign Up", the user is goes to SignUpFragment.
     */
    private fun setupListeners() {
        binding.loginBtn.setOnClickListener {
            val username = binding.usernameInput.text.toString().trim()
            val password = binding.passwordInput.text.toString().trim()

            if (username.isNotEmpty() && password.isNotEmpty()) {
                LoginViewModel.login(username, password)
            } else {
                Toast.makeText(
                    requireContext(),
                    "Please enter both username and password",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        binding.signupText.setOnClickListener {
            Log.d("Navigation", "Navigating to SignupFragment")
            findNavController().navigate(R.id.action_loginFragment_to_signupFragment)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}