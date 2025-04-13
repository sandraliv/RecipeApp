package com.hi.recipeapp.ui.settings

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.hi.recipeapp.R
import com.hi.recipeapp.WelcomePageActivity
import com.hi.recipeapp.databinding.FragmentSettingsBinding
import dagger.hilt.android.AndroidEntryPoint

import java.io.File

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private val binding get() = _binding!!
    private val settingsViewModel: SettingsViewModel by viewModels() // Get ViewModel instance
    private var _binding: FragmentSettingsBinding? = null

    private lateinit var photoUri: Uri

    private val takePhotoLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            binding.profilePic.setImageURI(photoUri)
            uploadPhoto(photoUri)
        }
    }

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            binding.profilePic.setImageURI(it)
            settingsViewModel.addPicToSessionManager(it)
            uploadPhoto(it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)

        val toolbarTitle = requireActivity().findViewById<TextView>(R.id.titleTextView)
        toolbarTitle.text = "Account"
        toolbarTitle.visibility = View.VISIBLE

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

        /**
         * Observer for errors while uploading a new profile pic
         * @returns message and makes a toast if there was a message
         */
        settingsViewModel.uploadPPErrorMessage.observe(viewLifecycleOwner) { message ->
            if (!message.isNullOrEmpty()) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        }

        /**
         * Photo dialog when you click on profile pic
         */
        binding.profilePic.setOnClickListener {
            showPhotoDialog()
        }


        /**
         * Observe and update profile picture (fetch url)
         */
        settingsViewModel.profilePic.observe(viewLifecycleOwner) { profilePicUrl ->
            if (!profilePicUrl.isNullOrEmpty()) {
                binding.profilePicHint.visibility = View.GONE
                Glide.with(this)
                    .load(profilePicUrl)
                    .into(binding.profilePic)
            } else {
                binding.profilePicHint.visibility = View.VISIBLE
            }
        }

        /**
         * If admin is present, he will see a "User Management" button on his profile where he
         * can delete users.
         */
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

    /**
     * Gets an InputStrem from the Uri, reads it into a ByteArray and sends it along.
     * @param photoUri Uri for where a photo is placed
     */
    private fun uploadPhoto(photoUri: Uri) {

        val contentResolver = requireContext().contentResolver
        val inputStream = contentResolver.openInputStream(photoUri) ?: return

        // Read the entire stream into a ByteArray
        val photoBytes = inputStream.readBytes()
        inputStream.close()

        settingsViewModel.uploadPhotoBytes(photoBytes)

    }

    /**
     * A photo dialog with three options.
     */
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
        val action = SettingsFragmentDirections.settingsFragmentToPasswordFragment()
        findNavController().navigate(action)
    }

    private fun navigateToLogin() {
        val intent = Intent(requireContext(), WelcomePageActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

     override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}