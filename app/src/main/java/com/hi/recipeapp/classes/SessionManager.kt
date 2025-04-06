package com.hi.recipeapp.classes

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.hi.recipeapp.data.local.RecipeDao
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class SessionManager @Inject constructor(@ApplicationContext context: Context, private val recipeDao: RecipeDao) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("user_session", Context.MODE_PRIVATE)


    companion object {
        const val KEY_USER_ID = "user_id"
        const val KEY_USER_NAME = "user_name"
        private const val TAG = "SessionManager"
        const val KEY_USER_PROFILE_PIC = "user_pic"
        const val KEY_USER_PASSWORD = "user_pw"
        const val KEY_USER_ROLE = "user_role"
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
            // Consider handling the case where the user is not logged in, instead of throwing an exception
            return -1 // Return -1 or handle this case gracefully in your activity or fragment
        }
        return userId
    }

    fun getProfilePic(): String? {
        val profilePic = sharedPreferences.getString(KEY_USER_PROFILE_PIC, "")
        Log.d(TAG,"Retreived profilepic url: $profilePic")
        if (profilePic == ""){
            Log.e(TAG, "No profile pic for user")
            return ""
        }
        return profilePic
    }

    // Save user name (if needed)
    fun saveUserNameAndRole(userName: String, role: String) {
        Log.d(TAG, "Saving User Name: $userName")
        val editor = sharedPreferences.edit()
        editor.putString(KEY_USER_NAME, userName)
        editor.putString(KEY_USER_ROLE, role)
        editor.apply()
        Log.d(TAG, "User Name saved successfully.")
    }

    fun savePassword(password: String) {
        val editor = sharedPreferences.edit()
        editor.putString(KEY_USER_PASSWORD, password)
        editor.apply()
    }

    fun getPassword(): String? {
        val password = sharedPreferences.getString(KEY_USER_PASSWORD, null)
        return password
    }

    // Retrieve user name (if needed)
    fun getUserName(): String? {
        val userName = sharedPreferences.getString(KEY_USER_NAME, null)
        Log.d(TAG, "Retrieved User Name: $userName")
        return userName
    }

    fun isAdmin(): Boolean {
        val userRole = sharedPreferences.getString(KEY_USER_ROLE, null)
        if (userRole == "admin") {
            return true
        }
        return false
    }

    // Clear session (logout)
    suspend fun clearSession() {
        Log.d(TAG, "Clearing session")
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
        recipeDao.removeAll()
        Log.d(TAG, "Session cleared successfully.")
    }

    // Check if the user is logged in by verifying if userId is not -1
    fun isUserLoggedIn(): Boolean {
        return getUserId() != -1
    }

    fun saveProfilePic(profilePictureUrl: String?) {
        Log.d(TAG, "Saving ProfilePIC URL: $profilePictureUrl")
        val editor = sharedPreferences.edit()
        editor.putString(KEY_USER_PROFILE_PIC, profilePictureUrl)
        editor.apply()
        Log.d(TAG, "User Profile Pic saved successfully.")
    }


    fun setFavoritedStatus(userId: Int, recipeId: Int, isFavorited: Boolean) {
        val editor = sharedPreferences.edit()
        val key = "user_${userId}_recipe_$recipeId"
        editor.putBoolean(key, isFavorited)
        editor.apply()
    }

    fun getFavoritedStatus(userId: Int, recipeId: Int): Boolean {
        val key = "user_${userId}_recipe_${recipeId}"
        return sharedPreferences.getBoolean(key, false)
    }

    // Save the favorite recipe IDs in SharedPreferences
    fun saveFavoriteRecipeIds(favoriteRecipeIds: Set<Int>) {
        val editor = sharedPreferences.edit()
        editor.putStringSet("FAVORITE_RECIPES", favoriteRecipeIds.map { it.toString() }.toSet()) // Save as Set of Strings
        editor.apply()
    }

    // Get the favorite recipe IDs from SharedPreferences
    fun getFavoriteRecipeIds(): Set<Int> {
        val favorites = sharedPreferences.getStringSet("FAVORITE_RECIPES", emptySet())
        return favorites?.map { it.toInt() }?.toSet() ?: emptySet()
    }

    fun logout() {
        clearSession()
        // Perform other logout-related actions if needed
    }

    fun isDarkModeEnabled(): Boolean {
        return sharedPreferences.getBoolean("dark_mode", false)
    }

    fun setDarkMode(enabled: Boolean) {
        sharedPreferences.edit().putBoolean("dark_mode", enabled).apply()
    }

}

