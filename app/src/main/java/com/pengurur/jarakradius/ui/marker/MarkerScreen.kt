package com.pengurur.jarakradius.ui.marker

import android.location.Location
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
// ⚠️ SESUAIKAN 3 baris di bawah ini jika ViewModel Anda ada di package lain.
// Cara cek: buka file MarkerViewModel.kt / SimpanViewModel.kt / FavoriteViewModel.kt,
// lihat baris "package ..." paling atas, lalu samakan di sini.
import com.pengurur.jarakradius.viewmodel.FavoriteViewModel
import com.pengurur.jarakradius.viewmodel.MarkerViewModel
import com.pengurur.jarakradius.viewmodel.SimpanViewModel

private val DEFAULT_POSITION = LatLng(-6.175392, 106.827153)
private const val DEFAULT_ZOOM = 14f

@Composable
fun MarkerScreen(
    markerViewModel: MarkerViewModel = viewModel(),
    simpanViewModel: SimpanViewModel = viewModel(),
    favoriteViewModel: FavoriteViewModel = viewModel(),
    cameraPositionState: CameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(DEFAULT_POSITION, DEFAULT_ZOOM)
    },
    modifier: Modifier = Modifier
) {
    val markerPoints = remember { mutableStateListOf<LatLng>() }
    var isSatellite by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {

        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                // mapType di versi maps-compose Anda bertipe enum MapType,
                // bukan Int — maka dipakai MapType.HYBRID / MapType.NORMAL
                mapType = if (isSatellite) MapType.HYBRID else MapType.NORMAL
            ),
            uiSettings = MapUiSettings(zoomControlsEnabled = false),
            onMapClick = { latLng -> markerPoints.add(latLng) }
        ) {
            if (markerPoints.size > 1) {
                Polyline(
                    points = markerPoints.toList(),
                    color = Color(0xFF1565C0),
                    width = 8f
                )
            }

            if (markerPoints.size >= 2) {
                val radiusMeters = distanceBetween(markerPoints.first(), markerPoints.last())
                Circle(
                    center = markerPoints.first(),
                    radius = radiusMeters.toDouble(),
                    strokeColor = Color(0xFF1565C0),
                    strokeWidth = 4f,
                    fillColor = Color(0x331565C0)
                )
            }

            markerPoints.forEachIndexed { index, latLng ->
                Marker(
                    state = rememberMarkerState(position = latLng),
                    title = if (index == 0) "Pusat" else "Titik $index",
                    icon = BitmapDescriptorFactory.defaultMarker(
                        if (index == 0) BitmapDescriptorFactory.HUE_AZURE
                        else BitmapDescriptorFactory.HUE_RED
                    )
                )
            }
        }

        Card(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(16.dp)
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.92f)
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "Pengukur Jarak & Radius",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Jumlah titik: ${markerPoints.size}",
                    style = MaterialTheme.typography.bodySmall
                )
                if (markerPoints.size >= 2) {
                    val radiusMeters =
                        distanceBetween(markerPoints.first(), markerPoints.last())
                    Text(
                        text = "Radius (pusat → titik akhir): ${formatDistance(radiusMeters)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "Total jarak: ${formatDistance(totalDistance(markerPoints))}",
                        style = MaterialTheme.typography.bodySmall
                    )
                } else {
                    Text(
                        text = "Ketuk peta: titik pertama = pusat, titik berikutnya = titik ukur.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.92f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Satelit", style = MaterialTheme.typography.bodySmall)
                    Switch(checked = isSatellite, onCheckedChange = { isSatellite = it })
                }
            }

            OutlinedButton(
                onClick = { markerPoints.removeAt(markerPoints.lastIndex) },
                enabled = markerPoints.isNotEmpty()
            ) { Text("Undo") }

            OutlinedButton(
                onClick = { markerPoints.clear() },
                enabled = markerPoints.isNotEmpty()
            ) { Text("Hapus") }
        }
    }
}

private fun distanceBetween(a: LatLng, b: LatLng): Float {
    val results = FloatArray(1)
    Location.distanceBetween(a.latitude, a.longitude, b.latitude, b.longitude, results)
    return results[0]
}

private fun totalDistance(points: List<LatLng>): Float =
    points.zipWithNext { a, b -> distanceBetween(a, b) }.sum()

private fun formatDistance(meters: Float): String =
    if (meters >= 1000f) "%.2f km".format(meters / 1000f)
    else "%.1f m".format(meters)
