package com.finalyear.hikemanagementapp.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for Observation entities.
 * Provides methods for querying, inserting, updating, and deleting observations in the database.
 */
@Dao
interface ObservationDao {
    /**
     * Retrieves all observations for a specific hike, ordered by observation time descending.
     *
     * @param hikeId The ID of the hike to get observations for.
     * @return A Flow emitting a list of observations, automatically updated when data changes.
     */
    @Query("SELECT * FROM observations WHERE hikeId = :hikeId ORDER BY observedAt DESC")
    fun getObservationsForHike(hikeId: Long): Flow<List<Observation>>
    
    /**
     * Retrieves a single observation by its ID.
     *
     * @param id The unique identifier of the observation to retrieve.
     * @return The Observation with the specified ID, or null if not found.
     */
    @Query("SELECT * FROM observations WHERE id = :id")
    suspend fun getObservationById(id: Long): Observation?
    
    /**
     * Inserts a new observation into the database or replaces an existing one with the same ID.
     *
     * @param observation The Observation object to insert.
     * @return The row ID of the inserted observation.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertObservation(observation: Observation): Long
    
    /**
     * Updates an existing observation in the database.
     *
     * @param observation The Observation object with updated values.
     */
    @Update
    suspend fun updateObservation(observation: Observation)
    
    /**
     * Deletes an observation from the database.
     *
     * @param observation The Observation object to delete.
     */
    @Delete
    suspend fun deleteObservation(observation: Observation)
}

