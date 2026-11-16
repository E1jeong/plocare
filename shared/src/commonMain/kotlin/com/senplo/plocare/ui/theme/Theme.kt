package com.senplo.plocare.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
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

    val typography: PloCareTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalPloCareTypography.current
}

@Composable
fun PloCareTheme(
    audience: AppAudience = AppAudience.USER,
    content: @Composable () -> Unit,
) {
    val colors = audience.palette()
    val fontFamily = rememberPretendardFontFamily()
    val ploCareTypography = buildPloCareTypography(fontFamily)
    val colorScheme = lightColorScheme(
        primary = colors.Primary,
        onPrimary = colors.White,
        primaryContainer = colors.PrimaryTint,
        onPrimaryContainer = colors.Primary,
        secondary = colors.GradientMain,
        onSecondary = colors.White,
        secondaryContainer = colors.PrimaryContainerLight,
        onSecondaryContainer = colors.Primary,
        background = colors.Background,
        onBackground = colors.TextMain,
        surface = colors.White,
        onSurface = colors.TextMain,
        surfaceVariant = colors.Black0Surface,
        onSurfaceVariant = colors.Black60Body,
        outline = colors.Black10,
        outlineVariant = colors.Black20,
        error = colors.StatusAlert,
        onError = colors.White,
    )
    CompositionLocalProvider(
        LocalPloCareColors provides colors,
        LocalAppAudience provides audience,
        LocalPloCareTypography provides ploCareTypography,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = ploCareTypography.toMaterialTypography(),
            content = content,
        )
    }
}
