package com.senplo.plocare.map

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.KakaoMapSdk
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapView
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.label.LabelOptions
import com.kakao.vectormap.label.LabelStyle
import com.kakao.vectormap.label.LabelStyles

@Composable
actual fun KakaoMapView(
    modifier: Modifier,
    initialLocation: MapLocation,
    markers: List<MapLocation>,
    onMarkerClick: (MapLocation) -> Unit,
    fallback: @Composable () -> Unit,
) {
    var mapError by remember { mutableStateOf(false) }
    if (!KakaoMapSdk.isInitialized() || mapError) {
        Box(modifier) { fallback() }
        return
    }

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val mapView = remember { MapView(context) }
    var kakaoMap by remember { mutableStateOf<KakaoMap?>(null) }
    val currentOnMarkerClick by rememberUpdatedState(onMarkerClick)

    DisposableEffect(mapView) {
        mapView.start(
            object : MapLifeCycleCallback() {
                override fun onMapDestroy() {
                    kakaoMap = null
                }

                override fun onMapError(error: Exception) {
                    // Auth failures (wrong key or unregistered key hash) land here; keep the cause visible in logcat.
                    Log.w("KakaoMapView", "Kakao map failed; showing fallback", error)
                    mapError = true
                }
            },
            object : KakaoMapReadyCallback() {
                override fun getPosition(): LatLng = initialLocation.toLatLng()

                override fun onMapReady(map: KakaoMap) {
                    map.setOnLabelClickListener { _, _, label ->
                        (label.tag as? MapLocation)?.let(currentOnMarkerClick)
                        true
                    }
                    kakaoMap = map
                }
            },
        )
        onDispose { mapView.finish() }
    }

    DisposableEffect(lifecycleOwner, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.resume()
                Lifecycle.Event.ON_PAUSE -> mapView.pause()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(kakaoMap, markers) {
        val map = kakaoMap ?: return@LaunchedEffect
        val layer = map.labelManager?.layer ?: return@LaunchedEffect
        val density = context.resources.displayMetrics.density
        layer.removeAll()
        markers.forEach { marker ->
            val color = marker.markerColor?.toArgb() ?: android.graphics.Color.BLUE
            val style = LabelStyle.from(markerBitmap(color, density))
                .setAnchorPoint(0.5f, 0.5f)
                .setApplyDpScale(false)
            layer.addLabel(
                LabelOptions.from(marker.toLatLng())
                    .setStyles(LabelStyles.from(style))
                    .setClickable(true)
                    .setTag(marker),
            )
        }
        when (markers.size) {
            0 -> Unit
            1 -> map.moveCamera(CameraUpdateFactory.newCenterPosition(markers.single().toLatLng()))
            else -> map.moveCamera(
                CameraUpdateFactory.fitMapPoints(markers.map { it.toLatLng() }.toTypedArray(), (48 * density).toInt()),
            )
        }
    }

    AndroidView(modifier = modifier, factory = { mapView })
}

private fun MapLocation.toLatLng(): LatLng = LatLng.from(latitude, longitude)

private fun markerBitmap(color: Int, density: Float): Bitmap {
    val size = (28 * density).toInt()
    val radius = size / 2f
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    paint.color = android.graphics.Color.WHITE
    canvas.drawCircle(radius, radius, radius, paint)
    paint.color = color
    canvas.drawCircle(radius, radius, radius - 2 * density, paint)
    return bitmap
}
