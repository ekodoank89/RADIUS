package com.pengurur.jarakradius.ui.simpan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pengurur.jarakradius.domain.model.MarkerPoint
import com.pengurur.jarakradius.domain.repository.MarkerRepository
import com.pengurur.jarakradius.domain.usecase.DeleteMarkerUseCase
import com.pengurur.jarakradius.domain.usecase.GetMarkersUseCase
import com.pengurur.jarakradius.domain.usecase.ToggleFavoriteUseCase
import com.pengurur.jarakradius.ui.components.EmptyState
import com.pengurur.jarakradius.ui.components.MarkerCard
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SimpanUiState(val markers: List<MarkerPoint> = emptyList())

sealed interface SimpanIntent {
    data class ToggleFavorite(val id: Long) : SimpanIntent
    data class Delete(val id: Long) : SimpanIntent
}

class SimpanViewModel(repository: MarkerRepository) : ViewModel() {

    private val getMarkers = GetMarkersUseCase(repository)
    private val toggleFavorite = ToggleFavoriteUseCase(repository)
    private val deleteMarker = DeleteMarkerUseCase(repository)

    private val _state = MutableStateFlow(SimpanUiState())
    val state: StateFlow<SimpanUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            getMarkers().collect { markers ->
                _state.update { it.copy(markers = markers) }
            }
        }
    }

    fun onIntent(intent: SimpanIntent) {
        when (intent) {
            is SimpanIntent.ToggleFavorite -> viewModelScope.launch {
                _state.value.markers.find { it.id == intent.id }?.let { toggleFavorite(it) }
            }
            is SimpanIntent.Delete -> viewModelScope.launch {
                deleteMarker(intent.id)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpanScreen(viewModel: SimpanViewModel) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Marker Tersimpan", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        if (state.markers.isEmpty()) {
            EmptyState(
                icon = Icons.Outlined.Bookmark,
                title = "Belum Ada Marker",
                subtitle = "Buka tab MARKER, geser peta, lalu tap Tambah Marker untuk menyimpan titik.",
                modifier = Modifier.padding(padding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.markers, key = { it.id }) { marker ->
                    MarkerCard(
                        marker = marker,
                        onToggleFavorite = {
                            viewModel.onIntent(SimpanIntent.ToggleFavorite(marker.id))
                        },
                        onDelete = {
                            viewModel.onIntent(SimpanIntent.Delete(marker.id))
                        }
                    )
                }
            }
        }
    }
}
