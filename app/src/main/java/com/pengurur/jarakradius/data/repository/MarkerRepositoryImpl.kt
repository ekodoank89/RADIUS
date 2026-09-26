package com.pengurur.jarakradius.data.repository

import com.pengurur.jarakradius.data.local.MarkerDao
import com.pengurur.jarakradius.data.local.MarkerEntity
import com.pengurur.jarakradius.domain.model.MarkerPoint
import com.pengurur.jarakradius.domain.repository.MarkerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MarkerRepositoryImpl(private val dao: MarkerDao) : MarkerRepository {

    override fun getMarkers(): Flow<List<MarkerPoint>> =
        dao.getAll().map { list -> list.map(MarkerEntity::toDomain) }

    override fun getFavorites(): Flow<List<MarkerPoint>> =
        dao.getFavorites().map { list -> list.map(MarkerEntity::toDomain) }

    override suspend fun addMarker(marker: MarkerPoint): Long =
        dao.insert(MarkerEntity.fromDomain(marker))

    override suspend fun updateMarker(marker: MarkerPoint) =
        dao.update(MarkerEntity.fromDomain(marker))

    override suspend fun deleteMarkerById(id: Long) = dao.deleteById(id)
}
