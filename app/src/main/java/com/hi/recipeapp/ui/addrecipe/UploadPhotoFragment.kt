
package com.hi.recipeapp.ui.addrecipe

import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import com.hi.recipeapp.databinding.FragmentUploadPhotoBinding
import java.io.File

class UploadPhotoFragment : Fragment() {

    private var _binding: FragmentUploadPhotoBinding? = null
    private val binding get() = _binding!!

    // The URI where the captured image will be stored
    private lateinit var photoUri: Uri

    // Launcher for taking a full-resolution photo
    private val takePhotoLauncher: ActivityResultLauncher<Uri> =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                // If success == true, the full-res image is in photoUri
                binding.imageView.setImageURI(photoUri)
                returnImageToPreviousFragment(photoUri)
                // TODO: If you need to upload:
                // - Convert photoUri to a File or byte[] and upload to your API
                // - Láttu view model sjá um að gera það og view modelið kallar á user-service
            } else {
                // Handle failure if needed
            }
        }

    // Launcher for picking an image from the gallery (optional)
    private val galleryLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                binding.imageView.setImageURI(it)
                returnImageToPreviousFragment(it)
                // If you need to upload this to an API, same approach: open an input stream, read the bytes, etc.
            }
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentUploadPhotoBinding.inflate(inflater, container, false)


        binding.uploadPhotoButton.setOnClickListener {
            showPhotoDialog()
        }

        return binding.root
    }

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
        // 1) Create a temp file in your app's cache directory (or files directory).
        val photoFile = File.createTempFile(
            "my_cake_image",  // prefix
            ".jpg",           // suffix
            requireContext().cacheDir // directory
        )

        // 2) Get a Uri from the FileProvider (Make sure to define <provider> in AndroidManifest)
        photoUri = FileProvider.getUriForFile(
            requireContext(),
            requireContext().packageName + ".provider",
            photoFile
        )

        // 3) Launch the camera to take a picture and save it to photoUri
        takePhotoLauncher.launch(photoUri)
    }

    private fun returnImageToPreviousFragment(uri: Uri) {
        val bundle = Bundle().apply {
            putParcelable("selectedImageUri", uri)
        }
        setFragmentResult("photoResult", bundle)
        parentFragmentManager.popBackStack() // Fer aftur í AddRecipeFragment
    }




    private fun openGallery() {
        // "image/*" filters only images from the gallery
        galleryLauncher.launch("image/*")
    }
}



