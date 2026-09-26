package com.pengurur.jarakradius.domain.usecase

import com.pengurur.jarakradius.domain.model.MarkerPoint
import com.pengurur.jarakradius.domain.repository.MarkerRepository
import com.pengurur.jarakradius.domain.util.DistanceCalculator
import kotlinx.coroutines.flow.Flow

class GetMarkersUseCase(private val repository: MarkerRepository) {
    operator fun invoke(): Flow<List<MarkerPoint>> = repository.getMarkers()
}

class GetFavoritesUseCase(private val repository: MarkerRepository) {
    operator fun invoke(): Flow<List<MarkerPoint>> = repository.getFavorites()
}

class SaveMarkerUseCase(private val repository: MarkerRepository) {
    suspend operator fun invoke(
        name: String,
        originLat: Double,
        originLng: Double,
        targetLat: Double,
        targetLng: Double
    ): MarkerPoint {
        val radiusCm = DistanceCalculator.distanceCm(
            originLat, originLng, targetLat, targetLng
        )
        val marker = MarkerPoint(
            name = name,
            latitude = targetLat,
            longitude = targetLng,
            radiusCm = radiusCm
        )
        val id = repository.addMarker(marker)
        return marker.copy(id = id)
    }
}

class ToggleFavoriteUseCase(private val repository: MarkerRepository) {
    suspend operator fun invoke(marker: MarkerPoint) {
        repository.updateMarker(marker.copy(isFavorite = !marker.isFavorite))
    }
}

class DeleteMarkerUseCase(private val repository: MarkerRepository) {
    suspend operator fun invoke(id: Long) = repository.deleteMarkerById(id)
}
