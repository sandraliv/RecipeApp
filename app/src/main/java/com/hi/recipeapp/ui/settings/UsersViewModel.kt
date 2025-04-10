package com.hi.recipeapp.ui.settings


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hi.recipeapp.classes.User
import com.hi.recipeapp.services.UserService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UsersViewModel @Inject constructor(
    private val userService: UserService
) : ViewModel() {

    private val _users = MutableLiveData<List<User>>()
    val users: LiveData<List<User>> get() = _users

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    /**
     * All users are fetched
     */
    fun fetchUsers() {
        viewModelScope.launch {
                val result = userService.getAllUsers()
                result
                    .onSuccess { _users.value = it }
                    .onFailure { _error.value = "Failed to load users: ${it.message}" }

        }
    }

    /**
     * A function that calls a function to remove a user with its id.
     * @param userId An int representing a user
     */
    fun deleteUser(userId: Int) {
        viewModelScope.launch {
            val result = userService.deleteUser(userId)
            result
                .onSuccess {
                    // Refresh the user list after successful delete
                    fetchUsers()
                }
                .onFailure {
                    _error.value = "Failed to delete user: ${it.message}"
                }
        }
    }


}
