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
    val TextPrimary: Color,
    val TextSecondary: Color,
    val TextTertiary: Color,
    val StatusGood: Color,
    val StatusWarn: Color,
    val StatusAlert: Color,
)

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
