package com.senplo.plocare.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

data class MapLocation(
    val latitude: Double,
    val longitude: Double,
    val title: String? = null,
)

@Composable
expect fun KakaoMapView(
    modifier: Modifier = Modifier,
    initialLocation: MapLocation = MapLocation(37.5665, 126.9780),
    markers: List<MapLocation> = emptyList(),
    onMarkerClick: (MapLocation) -> Unit = {},
)
