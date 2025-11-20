package com.finalyear.hikemanagementapp.data

import androidx.room.TypeConverter

/**
 * Type converters for Room database to handle List<String> storage
 */
class Converters {
    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        return value?.joinToString(",")
    }

    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        return value?.split(",")?.filter { it.isNotBlank() }?.takeIf { it.isNotEmpty() }
    }
}
