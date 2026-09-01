package com.senplo.plocare.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
actual fun KakaoMapView(
    modifier: Modifier,
    initialLocation: MapLocation,
    markers: List<MapLocation>,
    onMarkerClick: (MapLocation) -> Unit,
) {
    Box(modifier = modifier.background(Color.LightGray), contentAlignment = Alignment.Center) {
        Text("Kakao Map (iOS Native Bridge)")
    }
}
