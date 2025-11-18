package com.finalyear.hikemanagementapp.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "observations",
    foreignKeys = [
        ForeignKey(
            entity = Hike::class,
            parentColumns = ["id"],
            childColumns = ["hikeId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Observation(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val hikeId: Long,                    // Required - reference to hike
    val observation: String,             // Required
    val observedAt: Long,                // Required (stored as timestamp)
    val comments: String? = null         // Optional
)

