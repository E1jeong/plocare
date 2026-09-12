package com.senplo.plocare.map

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
actual fun KakaoMapView(
    modifier: Modifier,
    initialLocation: MapLocation,
    markers: List<MapLocation>,
    onMarkerClick: (MapLocation) -> Unit,
) {
    Box(modifier)
}
