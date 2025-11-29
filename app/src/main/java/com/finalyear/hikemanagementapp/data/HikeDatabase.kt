package com.finalyear.hikemanagementapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Room database class for the Hike Management application.
 * Defines the database configuration and serves as the main access point
 * for the underlying SQLite database.
 *
 * This database contains two tables:
 * - hikes: Stores Hike entities
 * - observations: Stores Observation entities linked to hikes
 */
@Database(
    entities = [Hike::class, Observation::class],
    version = 1,
    exportSchema = false
)
abstract class HikeDatabase : RoomDatabase() {
    /**
     * Returns the DAO for accessing Hike entities.
     *
     * @return The HikeDao instance for database operations on hikes.
     */
    abstract fun hikeDao(): HikeDao
    
    /**
     * Returns the DAO for accessing Observation entities.
     *
     * @return The ObservationDao instance for database operations on observations.
     */
    abstract fun observationDao(): ObservationDao
    
    companion object {
        @Volatile
        private var INSTANCE: HikeDatabase? = null
        
        /**
         * Gets the singleton database instance, creating it if necessary.
         * Uses double-checked locking to ensure thread safety while minimizing
         * synchronization overhead.
         *
         * @param context The application context used to create the database.
         * @return The singleton HikeDatabase instance.
         */
        fun getDatabase(context: Context): HikeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HikeDatabase::class.java,
                    "hike_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

