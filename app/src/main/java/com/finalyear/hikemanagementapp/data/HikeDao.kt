package com.finalyear.hikemanagementapp.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface HikeDao {
    @Query("SELECT * FROM hikes ORDER BY date DESC")
    fun getAllHikes(): Flow<List<Hike>>
    
    @Query("SELECT * FROM hikes WHERE id = :id")
    suspend fun getHikeById(id: Long): Hike?
    
    @Query("SELECT * FROM hikes WHERE name LIKE :searchQuery OR location LIKE :searchQuery")
    suspend fun searchHikesByNameOrLocation(searchQuery: String): List<Hike>
    
    @Query("SELECT * FROM hikes WHERE name LIKE :nameQuery AND location LIKE :locationQuery AND length >= :minLength AND length <= :maxLength AND date >= :minDate AND date <= :maxDate ORDER BY date DESC")
    suspend fun advancedSearch(
        nameQuery: String,
        locationQuery: String,
        minLength: Double,
        maxLength: Double,
        minDate: Long,
        maxDate: Long
    ): List<Hike>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHike(hike: Hike): Long
    
    @Update
    suspend fun updateHike(hike: Hike)
    
    @Delete
    suspend fun deleteHike(hike: Hike)
    
    @Query("DELETE FROM hikes")
    suspend fun deleteAllHikes()
}

