package com.hi.recipeapp.ui.settings

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.hi.recipeapp.MainActivity
import com.hi.recipeapp.R
import com.hi.recipeapp.WelcomePageActivity
import com.hi.recipeapp.databinding.FragmentSettingsBinding
import com.hi.recipeapp.ui.home.HomeViewModel
import com.hi.recipeapp.ui.theme.ThemeViewModel
import com.hi.recipeapp.ui.welcomepage.LoginFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
//This class extends Fragment(), meaning it represents a reusable UI component.
class SettingsFragment : Fragment() {

    // _binding holds the view binding reference for the fragment
    private var _binding: FragmentSettingsBinding? = null
    private val themeViewModel: ThemeViewModel by viewModels()


    // binding is a non-nullable property, ensuring safe access to UI elements within the fragment's lifecycle
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val settingsViewModel: SettingsViewModel by viewModels() // Get ViewModel instance
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)

        // Skiptir um þema
        binding.themeToggleButton.setOnClickListener {
            themeViewModel.toggleTheme()
        }


        themeViewModel.isDarkMode.observe(viewLifecycleOwner) { isDarkMode ->
            binding.themeToggleButton.setImageResource(R.drawable.dark_mode)
        }




        return binding.root
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Logout button click listener
        binding.logOut.setOnClickListener {
            settingsViewModel.logout()
            navigateToLogin()
        }

        binding.changePw.setOnClickListener {
            navigateToPasswordChange()
        }

        // Observe and update profile picture
        settingsViewModel.profilePic.observe(viewLifecycleOwner) { profilePicUrl ->
            if (!profilePicUrl.isNullOrEmpty()) {
                Glide.with(this)
                    .load(profilePicUrl)
                    .into(binding.profilePic) // Load into ImageView
            }
        }

        settingsViewModel.isAdmin.observe(viewLifecycleOwner) { isAdmin ->
            if (isAdmin) {
                binding.deleteUsers.visibility = View.VISIBLE
                binding.deleteUsers.setOnClickListener {
                    // Navigate to your admin screen or show a toast
                    findNavController().navigate(R.id.settingsFragment_to_deleteUsersFragment)
                }
            }
        }


    }

    private fun navigateToPasswordChange() {
        findNavController().navigate(R.id.settingsFragment_to_passwordFragment)
    }


    private fun navigateToLogin() {
        val intent = Intent(requireContext(), WelcomePageActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
    // _binding is set to null in onDestroyView() to prevent memory leak
    //If an Android Fragment, memory leaks can occur if the fragments holds references to UI elemnts (like TextView, Buttons, etc) AFTER the view is destroyed.
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}