package com.finalyear.hikemanagementapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Hike::class, Observation::class],
    version = 1,
    exportSchema = false
)
abstract class HikeDatabase : RoomDatabase() {
    abstract fun hikeDao(): HikeDao
    abstract fun observationDao(): ObservationDao
    
    companion object {
        @Volatile
        private var INSTANCE: HikeDatabase? = null
        
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

