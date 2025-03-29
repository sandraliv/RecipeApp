package com.hi.recipeapp.ui.settings

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hi.recipeapp.classes.User
import com.hi.recipeapp.databinding.ItemUserBinding

class UserAdapter(private val users: List<User>, private val onDeleteClick: (Int) -> Unit) :
    RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    inner class UserViewHolder(val binding: ItemUserBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]
        holder.binding.apply {
            userName.text = user.name
            userEmail.text = user.email
            userRole.text = user.role

            deleteUser.setOnClickListener {
                // Show confirmation dialog
                AlertDialog.Builder(holder.itemView.context)
                    .setTitle("Delete User")
                    .setMessage("Are you sure you want to delete ${user.name}?")
                    .setPositiveButton("Yes") { _, _ ->
                        onDeleteClick(user.id)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }

    }

    override fun getItemCount(): Int = users.size
}
