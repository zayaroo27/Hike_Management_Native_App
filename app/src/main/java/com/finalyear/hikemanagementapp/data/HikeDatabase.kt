package com.finalyear.hikemanagementapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Hike::class, Observation::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class HikeDatabase : RoomDatabase() {
    abstract fun hikeDao(): HikeDao
    abstract fun observationDao(): ObservationDao
    
    companion object {
        @Volatile
        private var INSTANCE: HikeDatabase? = null
        
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add photoUris column to observations table
                database.execSQL("ALTER TABLE observations ADD COLUMN photoUris TEXT")
            }
        }
        
        fun getDatabase(context: Context): HikeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HikeDatabase::class.java,
                    "hike_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

