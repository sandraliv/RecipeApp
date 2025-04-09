package com.hi.recipeapp.ui.settings

import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hi.recipeapp.classes.SessionManager
import com.hi.recipeapp.services.UserService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import javax.inject.Inject
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.URI

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val userService: UserService
) : ViewModel() {
    private val _profilePic = MutableLiveData<String?>()
    val profilePic: LiveData<String?> get() = _profilePic
    private val _isAdmin = MutableLiveData<Boolean>()
    val isAdmin: LiveData<Boolean> = _isAdmin
    private val _uploadPPErrorMessage = MutableLiveData<String>()
    val uploadPPErrorMessage: LiveData<String> get() = _uploadPPErrorMessage

    init {
        loadProfilePic()
        checkIfUserIsAdmin()
    }

    private fun loadProfilePic() {
        val pic = sessionManager.getProfilePic()
        _profilePic.value = if (!pic.isNullOrEmpty()) pic else null
    }

    fun logout() {
        viewModelScope.launch {
            sessionManager.clearSession()
        }
    }

    private fun checkIfUserIsAdmin() {
        // Assume you have a session manager or user info
        val user = sessionManager.isAdmin()
        _isAdmin.value = user == true
    }

    fun addPicToSessionManager(uri: Uri) {
        sessionManager.saveProfilePic(uri.toString())
    }

    fun uploadPhotoBytes(photoBytes: ByteArray) {
        viewModelScope.launch {
            try {
                val requestBody = photoBytes.toRequestBody("image/*".toMediaTypeOrNull())
                val photoPart = MultipartBody.Part.createFormData(
                    "file",
                    "profile_picture.jpg",
                    requestBody
                )
                val response = userService.uploadProfilePic(sessionManager.getUserId(), photoPart)
                if(response.isSuccessful) {
                    val body = response.body()
                    val newPPUrl = body?.get("profilePictureUrl")
                    if(newPPUrl != null) {
                        _uploadPPErrorMessage.value = "Profile picture has been changed!"
                        sessionManager.saveProfilePic(newPPUrl)
                        loadProfilePic()
                    } else {
                        _uploadPPErrorMessage.value = "Missing 'profilePictureUrl' in response"
                    }
                } else {
                    _uploadPPErrorMessage.value = "Upload failed: ${response.code()} ${response.message()}"
                }
            } catch (e: Exception) {
                _uploadPPErrorMessage.value = e.message ?: "Error uploading photo, try again"
            }
        }
    }

}