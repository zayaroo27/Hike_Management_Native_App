package com.finalyear.hikemanagementapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "hikes")
data class Hike(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,                    // Required
    val location: String,                // Required
    val date: Long,                      // Required (stored as timestamp)
    val parkingAvailable: Boolean,       // Required (Yes/No)
    val length: Double,                  // Required (in km)
    val difficulty: String,              // Required
    val description: String? = null,     // Optional
    val weather: String? = null,         // Optional - custom field 1
    val groupSize: Int? = null,          // Optional - custom field 2
    val photoPath: String? = null,       // For storing photo path
    val latitude: Double? = null,        // For location coordinates
    val longitude: Double? = null        // For location coordinates
)

