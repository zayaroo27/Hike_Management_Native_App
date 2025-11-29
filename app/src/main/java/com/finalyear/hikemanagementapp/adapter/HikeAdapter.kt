package com.finalyear.hikemanagementapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.finalyear.hikemanagementapp.R
import com.finalyear.hikemanagementapp.data.Hike
import java.text.SimpleDateFormat
import java.util.*

/**
 * RecyclerView adapter for displaying a list of hikes.
 * Uses ListAdapter with DiffUtil for efficient list updates and animations.
 *
 * @property onItemClick Callback function invoked when a hike item is clicked.
 */
class HikeAdapter(
    private val onItemClick: (Hike) -> Unit
) : ListAdapter<Hike, HikeAdapter.HikeViewHolder>(HikeDiffCallback()) {

    /**
     * Creates a new ViewHolder for a hike item.
     *
     * @param parent The parent ViewGroup into which the new View will be added.
     * @param viewType The view type of the new View (not used in this adapter).
     * @return A new HikeViewHolder that holds the inflated item view.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HikeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_hike, parent, false)
        return HikeViewHolder(view)
    }

    /**
     * Binds data to an existing ViewHolder.
     *
     * @param holder The ViewHolder to bind data to.
     * @param position The position of the item in the data set.
     */
    override fun onBindViewHolder(holder: HikeViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    /**
     * ViewHolder class for hike items.
     * Holds references to the views in a hike list item and handles binding data to them.
     *
     * @param itemView The root view of the hike list item layout.
     */
    inner class HikeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewHikeName: TextView = itemView.findViewById(R.id.textViewHikeName)
        private val textViewLocation: TextView = itemView.findViewById(R.id.textViewLocation)
        private val textViewDate: TextView = itemView.findViewById(R.id.textViewDate)
        private val textViewLength: TextView = itemView.findViewById(R.id.textViewLength)
        private val textViewDifficulty: TextView = itemView.findViewById(R.id.textViewDifficulty)

        private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

        /**
         * Binds hike data to the views in this ViewHolder.
         * Populates text views with hike information and sets up the click listener.
         *
         * @param hike The Hike object containing data to display.
         */
        fun bind(hike: Hike) {
            textViewHikeName.text = hike.name
            textViewLocation.text = "Location: ${hike.location}"
            textViewDate.text = "Date: ${dateFormat.format(Date(hike.date))}"
            textViewLength.text = "Length: ${hike.length} km"
            textViewDifficulty.text = hike.difficulty

            itemView.setOnClickListener {
                onItemClick(hike)
            }
        }
    }

    /**
     * DiffUtil callback for calculating the difference between two hike lists.
     * Used by ListAdapter to efficiently update the RecyclerView when data changes.
     */
    class HikeDiffCallback : DiffUtil.ItemCallback<Hike>() {
        /**
         * Checks if two items represent the same hike by comparing their IDs.
         *
         * @param oldItem The hike from the old list.
         * @param newItem The hike from the new list.
         * @return true if the items have the same ID, false otherwise.
         */
        override fun areItemsTheSame(oldItem: Hike, newItem: Hike): Boolean {
            return oldItem.id == newItem.id
        }

        /**
         * Checks if two items have the same content by comparing all their properties.
         *
         * @param oldItem The hike from the old list.
         * @param newItem The hike from the new list.
         * @return true if all properties are equal, false otherwise.
         */
        override fun areContentsTheSame(oldItem: Hike, newItem: Hike): Boolean {
            return oldItem == newItem
        }
    }
}

