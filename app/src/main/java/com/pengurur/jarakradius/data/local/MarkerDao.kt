package com.pengurur.jarakradius.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MarkerDao {

    @Query("SELECT * FROM markers ORDER BY createdAt DESC")
    fun getAll(): Flow<List<MarkerEntity>>

    @Query("SELECT * FROM markers WHERE isFavorite = 1 ORDER BY createdAt DESC")
    fun getFavorites(): Flow<List<MarkerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(marker: MarkerEntity): Long

    @Update
    suspend fun update(marker: MarkerEntity)

    @Query("DELETE FROM markers WHERE id = :id")
    suspend fun deleteById(id: Long)
}
