package com.pengurur.jarakradius.ui.navigation

import androidx.compose.foundation.layout.calculateBottomPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.maps.android.compose.CameraPositionState
import com.pengurur.jarakradius.ui.favorite.FavoriteScreen
import com.pengurur.jarakradius.ui.favorite.FavoriteViewModel
import com.pengurur.jarakradius.ui.marker.MarkerScreen
import com.pengurur.jarakradius.ui.marker.MarkerViewModel
import com.pengurur.jarakradius.ui.simpan.SimpanScreen
import com.pengurur.jarakradius.ui.simpan.SimpanViewModel

private data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val iconActive: ImageVector
)

private val bottomNavItems = listOf(
    BottomNavItem("marker", "MARKER", Icons.Outlined.LocationOn, Icons.Filled.LocationOn),
    BottomNavItem("simpan", "SIMPAN", Icons.Outlined.Bookmark, Icons.Filled.Bookmark),
    BottomNavItem("favorite", "FAVORITE", Icons.Outlined.Star, Icons.Filled.Star)
)

@Composable
fun MainScreen(
    markerViewModel: MarkerViewModel,
    simpanViewModel: SimpanViewModel,
    favoriteViewModel: FavoriteViewModel,
    cameraPositionState: CameraPositionState
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { item ->
                    val selected = currentRoute == item.route
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            if (!selected) {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = if (selected) item.iconActive else item.icon,
                                contentDescription = item.label
                            )
                        },
                        label = { Text(item.label, fontWeight = FontWeight.SemiBold) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "marker",
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = padding.calculateBottomPadding())
        ) {
            composable("marker") {
                MarkerScreen(
                    viewModel = markerViewModel,
                    cameraPositionState = cameraPositionState
                )
            }
            composable("simpan") { SimpanScreen(viewModel = simpanViewModel) }
            composable("favorite") { FavoriteScreen(viewModel = favoriteViewModel) }
        }
    }
}
