package com.pengurur.jarakradius

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.compose.rememberCameraPositionState
import com.pengurur.jarakradius.ui.favorite.FavoriteViewModel
import com.pengurur.jarakradius.ui.marker.DEFAULT_MAP_CENTER
import com.pengurur.jarakradius.ui.marker.MarkerViewModel
import com.pengurur.jarakradius.ui.navigation.MainScreen
import com.pengurur.jarakradius.ui.simpan.SimpanViewModel
import com.pengurur.jarakradius.ui.theme.RadiusTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RadiusTheme {
                val app = application as RadiusApp

                // ViewModel di-scope ke Activity agar state tidak hilang saat pindah tab
                val markerViewModel: MarkerViewModel = viewModel { MarkerViewModel(app.markerRepository) }
                val simpanViewModel: SimpanViewModel = viewModel { SimpanViewModel(app.markerRepository) }
                val favoriteViewModel: FavoriteViewModel = viewModel { FavoriteViewModel(app.markerRepository) }

                // Camera state di-hoist agar posisi peta tetap saat berpindah tab
                val cameraPositionState = rememberCameraPositionState {
                    position = CameraPosition.fromLatLngZoom(DEFAULT_MAP_CENTER, 18f)
                }

                MainScreen(
                    markerViewModel = markerViewModel,
                    simpanViewModel = simpanViewModel,
                    favoriteViewModel = favoriteViewModel,
                    cameraPositionState = cameraPositionState
                )
            }
        }
    }
}
