package com.hi.recipeapp.ui.home

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hi.recipeapp.R

class RecipeImageAdapter(
    private val context: Context,
    private var imageUrls: List<String>,  // Allow imageUrls to be mutable
    private val isFullRecipeView: Boolean // This flag determines if it's the full recipe view or recipe card view
) : RecyclerView.Adapter<RecipeImageAdapter.ImageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val layoutInflater = LayoutInflater.from(context)

        // Check if it's full recipe view or recipe card view and inflate the appropriate layout
        val layoutResId = if (isFullRecipeView) {
            R.layout.item_full_recipe_image  // Use full recipe image layout
        } else {
            R.layout.item_recipe_card_image  // Use recipe card image layout
        }

        val view = layoutInflater.inflate(layoutResId, parent, false)
        val imageView = view.findViewById<ImageView>(R.id.recipe_image)  // Ensure `recipe_image` is the correct ID in both layouts

        return ImageViewHolder(imageView)
    }

    // Bind images to the ImageView
    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        Glide.with(context)
            .load(imageUrls[position])
            .placeholder(R.drawable.placeholder) // Optional: Set a placeholder
            .error(R.drawable.error_image) // Optional: Set an error image
            .into(holder.imageView)
    }

    // Return the total number of images
    override fun getItemCount(): Int = imageUrls.size

    // Method to update the image list dynamically
    fun updateImages(newImageUrls: List<String>) {
        imageUrls = newImageUrls
        notifyDataSetChanged()  // Notify the adapter that the data has changed
    }

    // ViewHolder class for ImageView
    class ImageViewHolder(val imageView: ImageView) : RecyclerView.ViewHolder(imageView)
}
