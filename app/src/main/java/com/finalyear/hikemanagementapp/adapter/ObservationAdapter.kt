package com.finalyear.hikemanagementapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.finalyear.hikemanagementapp.R
import com.finalyear.hikemanagementapp.data.Observation
import java.text.SimpleDateFormat
import java.util.*

/**
 * RecyclerView adapter for displaying a list of observations.
 * Uses ListAdapter with DiffUtil for efficient list updates and animations.
 * Provides edit and delete actions for each observation item.
 *
 * @property onEditClick Callback function invoked when the edit button is clicked.
 * @property onDeleteClick Callback function invoked when the delete button is clicked.
 */
class ObservationAdapter(
    private val onEditClick: (Observation) -> Unit,
    private val onDeleteClick: (Observation) -> Unit
) : ListAdapter<Observation, ObservationAdapter.ObservationViewHolder>(ObservationDiffCallback()) {

    /**
     * Creates a new ViewHolder for an observation item.
     *
     * @param parent The parent ViewGroup into which the new View will be added.
     * @param viewType The view type of the new View (not used in this adapter).
     * @return A new ObservationViewHolder that holds the inflated item view.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ObservationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_observation, parent, false)
        return ObservationViewHolder(view)
    }

    /**
     * Binds data to an existing ViewHolder.
     *
     * @param holder The ViewHolder to bind data to.
     * @param position The position of the item in the data set.
     */
    override fun onBindViewHolder(holder: ObservationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    /**
     * ViewHolder class for observation items.
     * Holds references to the views in an observation list item and handles binding data to them.
     *
     * @param itemView The root view of the observation list item layout.
     */
    inner class ObservationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewObservation: TextView = itemView.findViewById(R.id.textViewObservation)
        private val textViewObservedAt: TextView = itemView.findViewById(R.id.textViewObservedAt)
        private val textViewComments: TextView = itemView.findViewById(R.id.textViewComments)
        private val buttonEdit: com.google.android.material.button.MaterialButton = itemView.findViewById(R.id.buttonEditObservation)
        private val buttonDelete: com.google.android.material.button.MaterialButton = itemView.findViewById(R.id.buttonDeleteObservation)

        private val dateTimeFormat = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())

        /**
         * Binds observation data to the views in this ViewHolder.
         * Populates text views with observation information, shows/hides the comments
         * field based on content, and sets up click listeners for edit and delete buttons.
         *
         * @param observation The Observation object containing data to display.
         */
        fun bind(observation: Observation) {
            textViewObservation.text = observation.observation
            textViewObservedAt.text = dateTimeFormat.format(Date(observation.observedAt))
            
            if (!observation.comments.isNullOrBlank()) {
                textViewComments.text = observation.comments
                textViewComments.visibility = View.VISIBLE
            } else {
                textViewComments.visibility = View.GONE
            }

            buttonEdit.setOnClickListener {
                onEditClick(observation)
            }

            buttonDelete.setOnClickListener {
                onDeleteClick(observation)
            }
        }
    }

    /**
     * DiffUtil callback for calculating the difference between two observation lists.
     * Used by ListAdapter to efficiently update the RecyclerView when data changes.
     */
    class ObservationDiffCallback : DiffUtil.ItemCallback<Observation>() {
        /**
         * Checks if two items represent the same observation by comparing their IDs.
         *
         * @param oldItem The observation from the old list.
         * @param newItem The observation from the new list.
         * @return true if the items have the same ID, false otherwise.
         */
        override fun areItemsTheSame(oldItem: Observation, newItem: Observation): Boolean {
            return oldItem.id == newItem.id
        }

        /**
         * Checks if two items have the same content by comparing all their properties.
         *
         * @param oldItem The observation from the old list.
         * @param newItem The observation from the new list.
         * @return true if all properties are equal, false otherwise.
         */
        override fun areContentsTheSame(oldItem: Observation, newItem: Observation): Boolean {
            return oldItem == newItem
        }
    }
}

