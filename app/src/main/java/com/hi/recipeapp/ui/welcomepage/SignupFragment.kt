package com.hi.recipeapp.ui.welcomepage

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.hi.recipeapp.R
import com.hi.recipeapp.databinding.FragmentSignupBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignupFragment : Fragment() {

    private var _binding: FragmentSignupBinding? = null
    private val binding get() = _binding!!
    private val signupViewModel: SignupViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()
        setupListeners()
    }

    /**
     * Observes LiveData from the ViewModel:
     * - If sign up is successful, user navigates to the login screen.
     * - If error, displays an error message.
     * - Updates the signup button's enabled state based on loading status.
     */
    private fun setupObservers() {
        signupViewModel.signupResult.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                Toast.makeText(requireContext(), "Signup successful! Welcome, ${user.username}", Toast.LENGTH_SHORT).show()
                Log.d("SignupSuccess", "User: ${user.username}")


                findNavController().navigate(R.id.action_signupFragment_to_loginFragment)
            }
        }

        signupViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                Log.e("SignupError", it)
            }
        }

        signupViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.signupBtn.isEnabled = !isLoading
        }
    }

    /**
     * Sets up listeners for user interactions:
     * - On signup button click, collects input data and calls the ViewModel’s signup method.
     * - If any field is empty, shows a warning message.
     * - Navigates to login screen when the "Log in" text is clicked.
     */
    private fun setupListeners() {
        binding.signupBtn.setOnClickListener {
            val role = "USER"
            val name = binding.signupNameInput.text.toString().trim()
            val username = binding.signupUsernameInput.text.toString().trim()
            val email = binding.signupEmailInput.text.toString().trim()
            val password = binding.signupPasswordInput.text.toString().trim()

            if (name.isNotEmpty() && username.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                signupViewModel.signup(role, name, email, password, username)
            } else {
                Toast.makeText(requireContext(), "Please fill in all fields!", Toast.LENGTH_SHORT).show()
            }
        }

        binding.loginText.setOnClickListener {
            findNavController().navigate(R.id.action_signupFragment_to_loginFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


