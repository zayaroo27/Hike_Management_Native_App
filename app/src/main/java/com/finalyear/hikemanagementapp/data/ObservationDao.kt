package com.finalyear.hikemanagementapp.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ObservationDao {
    @Query("SELECT * FROM observations WHERE hikeId = :hikeId ORDER BY observedAt DESC")
    fun getObservationsForHike(hikeId: Long): Flow<List<Observation>>
    
    @Query("SELECT * FROM observations WHERE id = :id")
    suspend fun getObservationById(id: Long): Observation?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertObservation(observation: Observation): Long
    
    @Update
    suspend fun updateObservation(observation: Observation)
    
    @Delete
    suspend fun deleteObservation(observation: Observation)
}

