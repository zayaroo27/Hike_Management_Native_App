package com.finalyear.hikemanagementapp.adapter

import android.content.Intent
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.finalyear.hikemanagementapp.PhotoViewerActivity
import com.finalyear.hikemanagementapp.R
import java.io.File

/**
 * Adapter for displaying photo thumbnails with remove functionality
 */
class PhotoThumbnailAdapter(
    private val onRemoveClick: (String) -> Unit,
    private val editable: Boolean = true
) : ListAdapter<String, PhotoThumbnailAdapter.PhotoViewHolder>(PhotoDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_photo_thumbnail, parent, false)
        return PhotoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    inner class PhotoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.imageViewThumbnail)
        private val buttonRemove: ImageButton = itemView.findViewById(R.id.buttonRemovePhoto)

        fun bind(photoPath: String, position: Int) {
            // Load and display thumbnail
            val file = File(photoPath)
            if (file.exists()) {
                val bitmap = BitmapFactory.decodeFile(photoPath)
                imageView.setImageBitmap(bitmap)
            }

            imageView.setOnClickListener {
                // Open full-screen viewer
                val intent = Intent(itemView.context, PhotoViewerActivity::class.java)
                intent.putExtra("photo_uris", currentList.toTypedArray())
                intent.putExtra("photo_index", position)
                itemView.context.startActivity(intent)
            }

            if (editable) {
                buttonRemove.visibility = View.VISIBLE
                buttonRemove.setOnClickListener {
                    onRemoveClick(photoPath)
                }
            } else {
                buttonRemove.visibility = View.GONE
            }
        }
    }

    class PhotoDiffCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }
    }
}
