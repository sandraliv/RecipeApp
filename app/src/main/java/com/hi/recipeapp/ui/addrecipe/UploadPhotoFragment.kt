package com.hi.recipeapp.ui.uploadphoto

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.hi.recipeapp.R

class UploadPhotoFragment : Fragment() {

    private lateinit var imageView: ImageView
    private lateinit var uploadPhotoButton: Button
    private var selectedImageUri: Uri? = null  // Store selected image URI

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_upload_photo, container, false)

        imageView = view.findViewById(R.id.imageView)
        uploadPhotoButton = view.findViewById(R.id.uploadPhotoButton)

        uploadPhotoButton.setOnClickListener {
            showPhotoDialog()
        }

        return view
    }
    // Open dialog
    private fun showPhotoDialog() {
        val options = arrayOf("Take Photo", "Choose from Gallery", "Cancel")
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Select Option")

        builder.setItems(options) { dialog, which ->
            when (which) {
                0 -> openCamera()      // Take Photo
                1 -> openGallery()     // Choose from Gallery
                2 -> dialog.dismiss()  // Cancel
            }
        }
        builder.show()
    }
    // Opens camera
    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
    }
    //Opens gallery
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_IMAGE_PICK)
    }
    // Handles photo selection
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_CAPTURE -> {
                    val imageBitmap = data?.extras?.get("data") as Bitmap
                    imageView.setImageBitmap(imageBitmap)
                }
                REQUEST_IMAGE_PICK -> {
                    selectedImageUri = data?.data
                    imageView.setImageURI(selectedImageUri)
                }
            }
        }
    }

    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 1
        private const val REQUEST_IMAGE_PICK = 2
    }
}

