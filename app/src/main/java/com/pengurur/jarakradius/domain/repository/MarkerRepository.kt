package com.pengurur.jarakradius.domain.repository

import com.pengurur.jarakradius.domain.model.MarkerPoint
import kotlinx.coroutines.flow.Flow

interface MarkerRepository {
    fun getMarkers(): Flow<List<MarkerPoint>>
    fun getFavorites(): Flow<List<MarkerPoint>>
    suspend fun addMarker(marker: MarkerPoint): Long
    suspend fun updateMarker(marker: MarkerPoint)
    suspend fun deleteMarkerById(id: Long)
}
