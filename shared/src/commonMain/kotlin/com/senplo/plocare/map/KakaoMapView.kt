package com.senplo.plocare.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

data class MapLocation(
    val latitude: Double,
    val longitude: Double,
    val title: String? = null,
    val id: String? = null,
    val markerColor: Color? = null,
)

/** Shows [fallback] wherever the native Kakao map is unavailable (unsupported platform, missing app key, or map error). */
@Composable
expect fun KakaoMapView(
    modifier: Modifier = Modifier,
    initialLocation: MapLocation = MapLocation(37.5665, 126.9780),
    markers: List<MapLocation> = emptyList(),
    onMarkerClick: (MapLocation) -> Unit = {},
    fallback: @Composable () -> Unit,
)
