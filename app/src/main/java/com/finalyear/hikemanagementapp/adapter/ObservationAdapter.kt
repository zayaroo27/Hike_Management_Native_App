package com.finalyear.hikemanagementapp.adapter

import android.content.Intent
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.finalyear.hikemanagementapp.PhotoViewerActivity
import com.finalyear.hikemanagementapp.R
import com.finalyear.hikemanagementapp.data.Observation
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class ObservationAdapter(
    private val onEditClick: (Observation) -> Unit,
    private val onDeleteClick: (Observation) -> Unit
) : ListAdapter<Observation, ObservationAdapter.ObservationViewHolder>(ObservationDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ObservationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_observation, parent, false)
        return ObservationViewHolder(view)
    }

    override fun onBindViewHolder(holder: ObservationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ObservationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewObservation: TextView = itemView.findViewById(R.id.textViewObservation)
        private val textViewObservedAt: TextView = itemView.findViewById(R.id.textViewObservedAt)
        private val textViewComments: TextView = itemView.findViewById(R.id.textViewComments)
        private val imageViewPhoto: ImageView = itemView.findViewById(R.id.imageViewObservationPhoto)
        private val buttonEdit: com.google.android.material.button.MaterialButton = itemView.findViewById(R.id.buttonEditObservation)
        private val buttonDelete: com.google.android.material.button.MaterialButton = itemView.findViewById(R.id.buttonDeleteObservation)

        private val dateTimeFormat = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())

        fun bind(observation: Observation) {
            textViewObservation.text = observation.observation
            textViewObservedAt.text = dateTimeFormat.format(Date(observation.observedAt))
            
            if (!observation.comments.isNullOrBlank()) {
                textViewComments.text = observation.comments
                textViewComments.visibility = View.VISIBLE
            } else {
                textViewComments.visibility = View.GONE
            }
            
            // Display first photo thumbnail if available
            if (!observation.photoUris.isNullOrEmpty()) {
                val firstPhotoPath = observation.photoUris.first()
                val file = File(firstPhotoPath)
                if (file.exists()) {
                    val bitmap = BitmapFactory.decodeFile(firstPhotoPath)
                    imageViewPhoto.setImageBitmap(bitmap)
                    imageViewPhoto.visibility = View.VISIBLE
                    
                    // Open photo viewer when clicked
                    imageViewPhoto.setOnClickListener {
                        val intent = Intent(itemView.context, PhotoViewerActivity::class.java)
                        intent.putExtra("photo_uris", observation.photoUris.toTypedArray())
                        intent.putExtra("photo_index", 0)
                        itemView.context.startActivity(intent)
                    }
                } else {
                    imageViewPhoto.visibility = View.GONE
                }
            } else {
                imageViewPhoto.visibility = View.GONE
            }

            buttonEdit.setOnClickListener {
                onEditClick(observation)
            }

            buttonDelete.setOnClickListener {
                onDeleteClick(observation)
            }
        }
    }

    class ObservationDiffCallback : DiffUtil.ItemCallback<Observation>() {
        override fun areItemsTheSame(oldItem: Observation, newItem: Observation): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Observation, newItem: Observation): Boolean {
            return oldItem == newItem
        }
    }
}

