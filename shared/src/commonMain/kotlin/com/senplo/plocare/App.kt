package com.senplo.plocare

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.senplo.plocare.navigation.PloCareNavHost
import com.senplo.plocare.ui.theme.PloCareColor

@Composable
@Preview
fun App() {
    val darkColors = darkColorScheme(
        primary = PloCareColor.AquaTeal,
        secondary = PloCareColor.VividCyan,
        background = PloCareColor.BgDeep,
        surface = PloCareColor.SurfaceDark,
        surfaceVariant = PloCareColor.SurfaceCard,
        onPrimary = PloCareColor.BgDeep,
        onBackground = PloCareColor.TextPrimary,
        onSurface = PloCareColor.TextPrimary
    )

    MaterialTheme(colorScheme = darkColors) {
        PloCareNavHost()
    }
}