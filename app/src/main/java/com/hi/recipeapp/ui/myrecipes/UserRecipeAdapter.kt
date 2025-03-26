package com.hi.recipeapp.ui.myrecipes

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import com.hi.recipeapp.classes.UserRecipeCard
import android.view.LayoutInflater
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hi.recipeapp.R
import com.hi.recipeapp.databinding.ItemUserRecipeCardBinding

class UserRecipeAdapter(private val onClick: (UserRecipeCard) -> Unit) : ListAdapter<UserRecipeCard, UserRecipeAdapter.UserRecipeViewHolder>(UserRecipeDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserRecipeViewHolder {
        val binding = ItemUserRecipeCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserRecipeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserRecipeViewHolder, position: Int) {
        val userRecipe = getItem(position) // Use getItem() to get the data
        holder.bind(userRecipe)
    }

    inner class UserRecipeViewHolder(private val binding: ItemUserRecipeCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(userRecipe: UserRecipeCard) {
            binding.recipeName.text = userRecipe.title
            binding.recipeDescription.text = userRecipe.description

            // Handle the main image (first image in the list of image URLs)
            val mainImageUrl = userRecipe.imageUrls?.firstOrNull() // First image
            Glide.with(binding.root.context)
                .load(mainImageUrl)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.error_image)
                .into(binding.userRecipeImage)

            // Set the click listener to pass the UserRecipeCard to the next screen
            binding.root.setOnClickListener {
                onClick(userRecipe)  // Passing the full UserRecipeCard object
            }
        }
    }

    // DiffUtil callback to optimize list updates
    class UserRecipeDiffCallback : DiffUtil.ItemCallback<UserRecipeCard>() {
        override fun areItemsTheSame(oldItem: UserRecipeCard, newItem: UserRecipeCard): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: UserRecipeCard, newItem: UserRecipeCard): Boolean {
            return oldItem == newItem
        }
    }
}
