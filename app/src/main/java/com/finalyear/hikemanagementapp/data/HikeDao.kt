package com.finalyear.hikemanagementapp.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for Hike entities.
 * Provides methods for querying, inserting, updating, and deleting hikes in the database.
 */
@Dao
interface HikeDao {
    /**
     * Retrieves all hikes from the database ordered by date in descending order.
     *
     * @return A Flow emitting a list of all hikes, automatically updated when data changes.
     */
    @Query("SELECT * FROM hikes ORDER BY date DESC")
    fun getAllHikes(): Flow<List<Hike>>
    
    /**
     * Retrieves a single hike by its ID.
     *
     * @param id The unique identifier of the hike to retrieve.
     * @return The Hike with the specified ID, or null if not found.
     */
    @Query("SELECT * FROM hikes WHERE id = :id")
    suspend fun getHikeById(id: Long): Hike?
    
    /**
     * Searches for hikes where the name or location contains the search query.
     *
     * @param searchQuery The search pattern to match (should include % wildcards).
     * @return A list of hikes matching the search criteria.
     */
    @Query("SELECT * FROM hikes WHERE name LIKE :searchQuery OR location LIKE :searchQuery")
    suspend fun searchHikesByNameOrLocation(searchQuery: String): List<Hike>
    
    /**
     * Performs an advanced search with multiple filter criteria.
     *
     * @param nameQuery Pattern to match against hike name (should include % wildcards).
     * @param locationQuery Pattern to match against hike location (should include % wildcards).
     * @param minLength Minimum hike length in kilometers.
     * @param maxLength Maximum hike length in kilometers.
     * @param minDate Minimum date as timestamp in milliseconds.
     * @param maxDate Maximum date as timestamp in milliseconds.
     * @return A list of hikes matching all the specified criteria, ordered by date descending.
     */
    @Query("SELECT * FROM hikes WHERE name LIKE :nameQuery AND location LIKE :locationQuery AND length >= :minLength AND length <= :maxLength AND date >= :minDate AND date <= :maxDate ORDER BY date DESC")
    suspend fun advancedSearch(
        nameQuery: String,
        locationQuery: String,
        minLength: Double,
        maxLength: Double,
        minDate: Long,
        maxDate: Long
    ): List<Hike>
    
    /**
     * Inserts a new hike into the database or replaces an existing one with the same ID.
     *
     * @param hike The Hike object to insert.
     * @return The row ID of the inserted hike.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHike(hike: Hike): Long
    
    /**
     * Updates an existing hike in the database.
     *
     * @param hike The Hike object with updated values.
     */
    @Update
    suspend fun updateHike(hike: Hike)
    
    /**
     * Deletes a hike from the database.
     * Also cascades to delete all observations associated with this hike.
     *
     * @param hike The Hike object to delete.
     */
    @Delete
    suspend fun deleteHike(hike: Hike)
    
    /**
     * Deletes all hikes from the database.
     * Also cascades to delete all observations.
     */
    @Query("DELETE FROM hikes")
    suspend fun deleteAllHikes()
}

