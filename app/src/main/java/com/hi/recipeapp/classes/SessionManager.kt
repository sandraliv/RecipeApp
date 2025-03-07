package com.hi.recipeapp.classes

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class SessionManager @Inject constructor(@ApplicationContext context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("user_session", Context.MODE_PRIVATE)

    companion object {
        const val KEY_USER_ID = "user_id"
        const val KEY_USER_NAME = "user_name"
        private const val TAG = "SessionManager"
    }

    // Save user ID (if needed)
    fun saveUserId(userId: Int) {
        Log.d(TAG, "Saving User ID: $userId")
        val editor = sharedPreferences.edit()
        editor.putInt(KEY_USER_ID, userId)
        editor.apply()
        Log.d(TAG, "User ID saved successfully.")
    }

    // Retrieve user ID (if needed)
    fun getUserId(): Int {
        val userId = sharedPreferences.getInt(KEY_USER_ID, -1)
        Log.d(TAG, "Retrieved User ID: $userId")
        if (userId == -1) {
            Log.e(TAG, "User ID not found in session")
            throw IllegalStateException("User ID not found in session")
        }
        return userId
    }

    // Save user name (if needed)
    fun saveUserName(userName: String) {
        Log.d(TAG, "Saving User Name: $userName")
        val editor = sharedPreferences.edit()
        editor.putString(KEY_USER_NAME, userName)
        editor.apply()
        Log.d(TAG, "User Name saved successfully.")
    }

    // Retrieve user name (if needed)
    fun getUserName(): String? {
        val userName = sharedPreferences.getString(KEY_USER_NAME, null)
        Log.d(TAG, "Retrieved User Name: $userName")
        return userName
    }

    // Clear session (logout)
    fun clearSession() {
        Log.d(TAG, "Clearing session")
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
        Log.d(TAG, "Session cleared successfully.")
    }
}
