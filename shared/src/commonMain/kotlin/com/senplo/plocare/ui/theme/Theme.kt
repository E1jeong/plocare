package com.senplo.plocare.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.font.FontFamily
import org.jetbrains.compose.resources.Font
import plocare.shared.generated.resources.Res
import plocare.shared.generated.resources.pretendard_regular

private fun Typography.withFontFamily(fontFamily: FontFamily): Typography = copy(
    displayLarge = displayLarge.copy(fontFamily = fontFamily),
    displayMedium = displayMedium.copy(fontFamily = fontFamily),
    displaySmall = displaySmall.copy(fontFamily = fontFamily),
    headlineLarge = headlineLarge.copy(fontFamily = fontFamily),
    headlineMedium = headlineMedium.copy(fontFamily = fontFamily),
    headlineSmall = headlineSmall.copy(fontFamily = fontFamily),
    titleLarge = titleLarge.copy(fontFamily = fontFamily),
    titleMedium = titleMedium.copy(fontFamily = fontFamily),
    titleSmall = titleSmall.copy(fontFamily = fontFamily),
    bodyLarge = bodyLarge.copy(fontFamily = fontFamily),
    bodyMedium = bodyMedium.copy(fontFamily = fontFamily),
    bodySmall = bodySmall.copy(fontFamily = fontFamily),
    labelLarge = labelLarge.copy(fontFamily = fontFamily),
    labelMedium = labelMedium.copy(fontFamily = fontFamily),
    labelSmall = labelSmall.copy(fontFamily = fontFamily),
)

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
    val fontFamily = FontFamily(Font(Res.font.pretendard_regular))
    val colorScheme = darkColorScheme(
        primary = colors.AquaTeal,
        secondary = colors.VividCyan,
        background = colors.BrandNavy,
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
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography().withFontFamily(fontFamily),
            content = content,
        )
    }
}
