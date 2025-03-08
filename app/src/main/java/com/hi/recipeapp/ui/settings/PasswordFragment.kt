package com.hi.recipeapp.ui.settings

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.hi.recipeapp.databinding.FragmentPasswordBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PasswordFragment : Fragment() {

    private var _binding: FragmentPasswordBinding? = null
    private val binding get() = _binding!!
    private val passwordViewModel: PasswordViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Disable button initially
        binding.changePwBtn.isEnabled = false
        binding.changePwBtn.alpha = 0.5f // Greyed out effect

        // Add text change listeners
        val textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                checkFieldsAndEnableButton()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        binding.newPw.addTextChangedListener(textWatcher)
        binding.currentPw.addTextChangedListener(textWatcher)
        binding.againNewPw.addTextChangedListener(textWatcher)

        // Handle password change button click
        binding.changePwBtn.setOnClickListener {
            val newPassword = binding.newPw.text.toString()
            val confirmPassword = binding.againNewPw.text.toString()

            if (newPassword == confirmPassword) {
                // TODO: Implement password update logic here
                binding.passwordError.visibility = View.GONE
            } else {
                binding.passwordError.text = "Passwords do not match!"
                binding.passwordError.visibility = View.VISIBLE
            }
        }
    }

    private fun checkFieldsAndEnableButton() {
        val currentPassword = binding.currentPw.text.toString()
        val newPassword = binding.newPw.text.toString()
        val confirmPassword = binding.againNewPw.text.toString()

        // Enable button only if all fields are filled
        val allFieldsFilled = currentPassword.isNotEmpty() && newPassword.isNotEmpty() && confirmPassword.isNotEmpty()

        binding.changePwBtn.isEnabled = allFieldsFilled
        binding.changePwBtn.alpha = if (allFieldsFilled) 1.0f else 0.5f // Change transparency
    }




    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}