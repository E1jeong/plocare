package com.senplo.plocare.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapView

@Composable
actual fun KakaoMapView(
    modifier: Modifier,
    initialLocation: MapLocation,
    markers: List<MapLocation>,
    onMarkerClick: (MapLocation) -> Unit,
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            MapView(context).apply {
                start(
                    object : MapLifeCycleCallback() {
                        override fun onMapDestroy() {}
                        override fun onMapError(error: Exception) {}
                    },
                    object : KakaoMapReadyCallback() {
                        override fun onMapReady(kakaoMap: KakaoMap) {
                            // Kakao Map ready callback
                        }
                    }
                )
            }
        }
    )
}
