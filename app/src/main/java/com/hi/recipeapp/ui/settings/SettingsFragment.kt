package com.hi.recipeapp.ui.settings

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
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
import java.io.File

@AndroidEntryPoint
//This class extends Fragment(), meaning it represents a reusable UI component.
class SettingsFragment : Fragment() {

    // _binding holds the view binding reference for the fragment
    private var _binding: FragmentSettingsBinding? = null
    private val themeViewModel: ThemeViewModel by viewModels()

    private lateinit var photoUri: Uri

    private val takePhotoLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            binding.profilePic.setImageURI(photoUri)
            // TODO: If you need to upload:
        }
    }

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            binding.profilePic.setImageURI(it)
            // TODO: If you need to upload:
        }
    }



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

        // Changes theme when you click on dropdown button
        binding.themeDropdownButton.setOnClickListener {
            showThemePopup(it)
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

        // Photo dialog when you click on add icon
        binding.editProfilePicButton.setOnClickListener {
            showPhotoDialog()
        }
        // Photo dialog when you click on profile pic
        binding.profilePic.setOnClickListener {
            showPhotoDialog()
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

    private fun showThemePopup(anchor: View) {
        val popup = android.widget.PopupMenu(requireContext(), anchor)
        val isDarkMode = themeViewModel.isDarkMode.value ?: false

        val themeText = if (isDarkMode) "Change to light mode" else "Change to dark mode"

        popup.menu.add(themeText).setOnMenuItemClickListener {
            themeViewModel.toggleTheme()
            true
        }

        popup.show()
    }


    // Photo dialog with three options
    private fun showPhotoDialog() {
        val options = arrayOf("Take Photo", "Choose from Gallery", "Cancel")
        AlertDialog.Builder(requireContext())
            .setTitle("Select Option")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> openCamera()
                    1 -> openGallery()
                    2 -> dialog.dismiss()
                }
            }
            .show()
    }

    private fun openCamera() {
        val photoFile = File.createTempFile(
            "profile_pic", ".jpg", requireContext().cacheDir
        )
        photoUri = FileProvider.getUriForFile(
            requireContext(),
            requireContext().packageName + ".provider",
            photoFile
        )
        takePhotoLauncher.launch(photoUri)
    }

    private fun openGallery() {
        galleryLauncher.launch("image/*")
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