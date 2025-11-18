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

class HikeAdapter(
    private val onItemClick: (Hike) -> Unit
) : ListAdapter<Hike, HikeAdapter.HikeViewHolder>(HikeDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HikeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_hike, parent, false)
        return HikeViewHolder(view)
    }

    override fun onBindViewHolder(holder: HikeViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class HikeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewHikeName: TextView = itemView.findViewById(R.id.textViewHikeName)
        private val textViewLocation: TextView = itemView.findViewById(R.id.textViewLocation)
        private val textViewDate: TextView = itemView.findViewById(R.id.textViewDate)
        private val textViewLength: TextView = itemView.findViewById(R.id.textViewLength)
        private val textViewDifficulty: TextView = itemView.findViewById(R.id.textViewDifficulty)

        private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

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

    class HikeDiffCallback : DiffUtil.ItemCallback<Hike>() {
        override fun areItemsTheSame(oldItem: Hike, newItem: Hike): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Hike, newItem: Hike): Boolean {
            return oldItem == newItem
        }
    }
}

