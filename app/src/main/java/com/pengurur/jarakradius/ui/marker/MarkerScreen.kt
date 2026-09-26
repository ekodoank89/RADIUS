package com.pengurur.jarakradius.ui.marker

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.outlined.SquareFoot
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapType
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.pengurur.jarakradius.R
import com.pengurur.jarakradius.ui.util.FormatUtil

@Composable
fun MarkerScreen(
    viewModel: MarkerViewModel,
    cameraPositionState: CameraPositionState,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val primary = MaterialTheme.colorScheme.primary

    // Laporkan pergerakan kamera (koordinat tengah layar) ke ViewModel
    LaunchedEffect(cameraPositionState) {
        snapshotFlow { cameraPositionState.position.target }
            .collect { viewModel.onIntent(MarkerIntent.CameraMoved(it)) }
    }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onIntent(MarkerIntent.ConsumeMessage)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {

        GoogleMap(
            modifier = Modifier.matchParentSize(),
            cameraPositionState = cameraPositionState,
            properties = remember { MapProperties(mapType = MapType.NORMAL) },
            uiSettings = remember {
                MapUiSettings(zoomControlsEnabled = false, tiltGesturesEnabled = false)
            }
        ) {
            // Visualisasi lingkaran radius: PUSAT -> PIN
            Circle(
                center = state.origin,
                radius = state.radiusCm / 100.0,
                strokeWidth = 4f,
                strokeColor = primary,
                fillColor = primary.copy(alpha = 0.12f)
            )
            Polyline(
                points = listOf(state.origin, state.center),
                width = 6f,
                color = primary
            )
            // Marker yang tersimpan
            state.markers.forEach { m ->
                Marker(
                    state = MarkerState(position = LatLng(m.latitude, m.longitude)),
                    title = m.name,
                    snippet = FormatUtil.formatCm(m.radiusCm)
                )
            }
        }

        // Pin tetap di tengah layar (peta yang bergerak)
        CenterPin(modifier = Modifier.align(Alignment.Center))

        RadiusInfoCard(
            radiusCm = state.radiusCm,
            origin = state.origin,
            center = state.center,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(16.dp)
        )

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ExtendedFloatingActionButton(
                onClick = { viewModel.onIntent(MarkerIntent.SetOriginHere) },
                icon = { Icon(Icons.Filled.MyLocation, contentDescription = null) },
                text = { Text("Set Pusat") }
            )
            ExtendedFloatingActionButton(
                onClick = { viewModel.onIntent(MarkerIntent.OpenSaveDialog) },
                icon = { Icon(Icons.Filled.AddLocationAlt, contentDescription = null) },
                text = { Text("Tambah Marker") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 96.dp)
        )
    }

    if (state.showSaveDialog) {
        SaveMarkerDialog(
            name = state.markerName,
            radiusCm = state.radiusCm,
            onNameChange = { viewModel.onIntent(MarkerIntent.MarkerNameChanged(it)) },
            onConfirm = { viewModel.onIntent(MarkerIntent.SaveMarker) },
            onDismiss = { viewModel.onIntent(MarkerIntent.DismissSaveDialog) }
        )
    }
}

@Composable
private fun CenterPin(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        val transition = rememberInfiniteTransition(label = "pulse")
        val scale by transition.animateFloat(
            initialValue = 0.7f,
            targetValue = 1.8f,
            animationSpec = infiniteRepeatable(tween(1400, easing = LinearEasing)),
            label = "scale"
        )
        val alpha by transition.animateFloat(
            initialValue = 0.45f,
            targetValue = 0f,
            animationSpec = infiniteRepeatable(tween(1400, easing = LinearEasing)),
            label = "alpha"
        )

        // Ring berdenyut di titik tengah
        Box(
            modifier = Modifier
                .size(30.dp)
                .scale(scale)
                .alpha(alpha)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
        )

        // Ujung bawah pin tepat di tengah layar
        Icon(
            painter = painterResource(R.drawable.ic_center_pin),
            contentDescription = "Pin pemilih koordinat",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(44.dp)
                .offset(y = (-22).dp)
        )
    }
}

@Composable
private fun RadiusInfoCard(
    radiusCm: Double,
    origin: LatLng,
    center: LatLng,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    Icons.Outlined.SquareFoot,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    "JARAK RADIUS",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                FormatUtil.formatCm(radiusCm),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                "≈ ${FormatUtil.formatReadable(radiusCm)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
            CoordRow("PUSAT", origin)
            CoordRow("PIN", center)
        }
    }
}

@Composable
private fun CoordRow(label: String, latLng: LatLng) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            "${FormatUtil.formatCoordinate(latLng.latitude)}, ${FormatUtil.formatCoordinate(latLng.longitude)}",
            style = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun SaveMarkerDialog(
    name: String,
    radiusCm: Double,
    onNameChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Simpan Marker") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Jarak dari titik pusat:", style = MaterialTheme.typography.bodyMedium)
                Text(
                    FormatUtil.formatCm(radiusCm),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = { Text("Nama marker") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Simpan") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}
