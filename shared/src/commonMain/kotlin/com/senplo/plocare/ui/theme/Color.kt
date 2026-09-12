package com.senplo.plocare.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class PloCareColors(
    val BrandNavy: Color,
    val BgDeep: Color,
    val BgMid: Color,
    val SurfaceDark: Color,
    val SurfaceCard: Color,
    val SurfaceBorder: Color,
    val AquaTeal: Color,
    val VividCyan: Color,
    val LuminousIce: Color,
    val DeepBlue: Color,
    val FilterStage3: Color,
    val FilterStage4: Color,
    val FilterStage5: Color,
    val TextPrimary: Color,
    val TextSecondary: Color,
    val TextTertiary: Color,
    val StatusGood: Color,
    val StatusWarn: Color,
    val StatusAlert: Color,
) {
    fun filterStageColor(stage: Int): Color = when (stage) {
        1 -> AquaTeal
        2 -> VividCyan
        3 -> FilterStage3
        4 -> FilterStage4
        else -> FilterStage5
    }
}

val UserPloCareColors = PloCareColors(
    BrandNavy = Color(0xFF071A2E),
    BgDeep = Color(0xFF070B14),
    BgMid = Color(0xFF0C162A),
    SurfaceDark = Color(0xFF131F37),
    SurfaceCard = Color(0xFF192644),
    SurfaceBorder = Color(0xFF263A5E),
    AquaTeal = Color(0xFF2DD4BF),
    VividCyan = Color(0xFF38BDF8),
    LuminousIce = Color(0xFFF0FDFF),
    DeepBlue = Color(0xFF0284C7),
    FilterStage3 = Color(0xFF818CF8),
    FilterStage4 = Color(0xFFFBBF24),
    FilterStage5 = Color(0xFFF472B6),
    TextPrimary = Color(0xFFFFFFFF),
    TextSecondary = Color(0xFF94A3B8),
    TextTertiary = Color(0xFF64748B),
    StatusGood = Color(0xFF10B981),
    StatusWarn = Color(0xFFF59E0B),
    StatusAlert = Color(0xFFEF4444),
)

val PartnerPloCareColors = PloCareColors(
    BrandNavy = Color(0xFF0A1628),
    BgDeep = Color(0xFF060A14),
    BgMid = Color(0xFF0C1428),
    SurfaceDark = Color(0xFF12203A),
    SurfaceCard = Color(0xFF182848),
    SurfaceBorder = Color(0xFF2A3F66),
    AquaTeal = Color(0xFF60A5FA),
    VividCyan = Color(0xFF93C5FD),
    LuminousIce = Color(0xFFEFF6FF),
    DeepBlue = Color(0xFF2563EB),
    FilterStage3 = Color(0xFFA78BFA),
    FilterStage4 = Color(0xFFFCD34D),
    FilterStage5 = Color(0xFFF9A8D4),
    TextPrimary = Color(0xFFFFFFFF),
    TextSecondary = Color(0xFF94A3B8),
    TextTertiary = Color(0xFF64748B),
    StatusGood = Color(0xFF10B981),
    StatusWarn = Color(0xFFF59E0B),
    StatusAlert = Color(0xFFEF4444),
)

internal val LocalPloCareColors = staticCompositionLocalOf { UserPloCareColors }

val PloCareColor: PloCareColors
    @Composable
    @ReadOnlyComposable
    get() = LocalPloCareColors.current
