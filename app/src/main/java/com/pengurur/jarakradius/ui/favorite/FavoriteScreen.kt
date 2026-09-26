package com.pengurur.jarakradius.ui.favorite

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Star
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
import com.pengurur.jarakradius.domain.usecase.GetFavoritesUseCase
import com.pengurur.jarakradius.domain.usecase.ToggleFavoriteUseCase
import com.pengurur.jarakradius.ui.components.EmptyState
import com.pengurur.jarakradius.ui.components.MarkerCard
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FavoriteUiState(val favorites: List<MarkerPoint> = emptyList())

sealed interface FavoriteIntent {
    data class ToggleFavorite(val id: Long) : FavoriteIntent
    data class Delete(val id: Long) : FavoriteIntent
}

class FavoriteViewModel(repository: MarkerRepository) : ViewModel() {

    private val getFavorites = GetFavoritesUseCase(repository)
    private val toggleFavorite = ToggleFavoriteUseCase(repository)
    private val deleteMarker = DeleteMarkerUseCase(repository)

    private val _state = MutableStateFlow(FavoriteUiState())
    val state: StateFlow<FavoriteUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            getFavorites().collect { favorites ->
                _state.update { it.copy(favorites = favorites) }
            }
        }
    }

    fun onIntent(intent: FavoriteIntent) {
        when (intent) {
            is FavoriteIntent.ToggleFavorite -> viewModelScope.launch {
                _state.value.favorites.find { it.id == intent.id }?.let { toggleFavorite(it) }
            }
            is FavoriteIntent.Delete -> viewModelScope.launch {
                deleteMarker(intent.id)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteScreen(viewModel: FavoriteViewModel) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Favorit Saya", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        if (state.favorites.isEmpty()) {
            EmptyState(
                icon = Icons.Outlined.Star,
                title = "Belum Ada Favorit",
                subtitle = "Tandai bintang ⭐ pada marker di tab SIMPAN untuk menambahkannya ke sini.",
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
                items(state.favorites, key = { it.id }) { marker ->
                    MarkerCard(
                        marker = marker,
                        onToggleFavorite = {
                            viewModel.onIntent(FavoriteIntent.ToggleFavorite(marker.id))
                        },
                        onDelete = {
                            viewModel.onIntent(FavoriteIntent.Delete(marker.id))
                        }
                    )
                }
            }
        }
    }
}
