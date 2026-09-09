package com.senplo.plocare.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

enum class AppAudience {
    USER,
    PARTNER,
}

fun AppAudience.palette(): PloCareColors = when (this) {
    AppAudience.USER -> UserPloCareColors
    AppAudience.PARTNER -> PartnerPloCareColors
}

internal val LocalAppAudience = staticCompositionLocalOf { AppAudience.USER }

object PloCareTheme {
    val audience: AppAudience
        @Composable
        @ReadOnlyComposable
        get() = LocalAppAudience.current

    val colors: PloCareColors
        @Composable
        @ReadOnlyComposable
        get() = LocalPloCareColors.current
}

@Composable
fun PloCareTheme(
    audience: AppAudience = AppAudience.USER,
    content: @Composable () -> Unit,
) {
    val colors = audience.palette()
    val colorScheme = darkColorScheme(
        primary = colors.AquaTeal,
        secondary = colors.VividCyan,
        background = colors.BgDeep,
        surface = colors.SurfaceDark,
        surfaceVariant = colors.SurfaceCard,
        onPrimary = colors.BgDeep,
        onBackground = colors.TextPrimary,
        onSurface = colors.TextPrimary,
    )
    CompositionLocalProvider(
        LocalPloCareColors provides colors,
        LocalAppAudience provides audience,
    ) {
        MaterialTheme(colorScheme = colorScheme, content = content)
    }
}
