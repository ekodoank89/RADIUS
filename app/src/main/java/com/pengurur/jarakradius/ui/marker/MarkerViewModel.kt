package com.pengurur.jarakradius.ui.marker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.pengurur.jarakradius.domain.model.MarkerPoint
import com.pengurur.jarakradius.domain.repository.MarkerRepository
import com.pengurur.jarakradius.domain.usecase.GetMarkersUseCase
import com.pengurur.jarakradius.domain.usecase.SaveMarkerUseCase
import com.pengurur.jarakradius.domain.util.DistanceCalculator
import com.pengurur.jarakradius.ui.util.FormatUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Titik awal peta (Monas, Jakarta) */
val DEFAULT_MAP_CENTER = LatLng(-6.175392, 106.827153)

data class MarkerUiState(
    val center: LatLng = DEFAULT_MAP_CENTER,
    val origin: LatLng = DEFAULT_MAP_CENTER,
    val radiusCm: Double = 0.0,
    val markers: List<MarkerPoint> = emptyList(),
    val showSaveDialog: Boolean = false,
    val markerName: String = "",
    val message: String? = null
)

sealed interface MarkerIntent {
    data class CameraMoved(val center: LatLng) : MarkerIntent
    data object SetOriginHere : MarkerIntent
    data object OpenSaveDialog : MarkerIntent
    data class MarkerNameChanged(val name: String) : MarkerIntent
    data object DismissSaveDialog : MarkerIntent
    data object SaveMarker : MarkerIntent
    data object ConsumeMessage : MarkerIntent
}

class MarkerViewModel(repository: MarkerRepository) : ViewModel() {

    private val getMarkers = GetMarkersUseCase(repository)
    private val saveMarker = SaveMarkerUseCase(repository)

    private val _state = MutableStateFlow(MarkerUiState())
    val state: StateFlow<MarkerUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            getMarkers().collect { markers ->
                _state.update { it.copy(markers = markers) }
            }
        }
    }

    fun onIntent(intent: MarkerIntent) {
        when (intent) {
            is MarkerIntent.CameraMoved -> _state.update { s ->
                val cm = DistanceCalculator.distanceCm(
                    s.origin.latitude, s.origin.longitude,
                    intent.center.latitude, intent.center.longitude
                )
                s.copy(center = intent.center, radiusCm = cm)
            }

            MarkerIntent.SetOriginHere -> _state.update { s ->
                s.copy(origin = s.center, radiusCm = 0.0, message = "Titik pusat dipindah ke pin")
            }

            MarkerIntent.OpenSaveDialog -> _state.update { s ->
                s.copy(showSaveDialog = true, markerName = "Marker ${s.markers.size + 1}")
            }

            is MarkerIntent.MarkerNameChanged -> _state.update { it.copy(markerName = intent.name) }

            MarkerIntent.DismissSaveDialog -> _state.update { it.copy(showSaveDialog = false) }

            MarkerIntent.SaveMarker -> viewModelScope.launch {
                val s = _state.value
                saveMarker(
                    name = s.markerName.ifBlank { "Marker ${s.markers.size + 1}" },
                    originLat = s.origin.latitude,
                    originLng = s.origin.longitude,
                    targetLat = s.center.latitude,
                    targetLng = s.center.longitude
                )
                _state.update {
                    it.copy(
                        showSaveDialog = false,
                        message = "Marker tersimpan • ${FormatUtil.formatCm(s.radiusCm)}"
                    )
                }
            }

            MarkerIntent.ConsumeMessage -> _state.update { it.copy(message = null) }
        }
    }
}
