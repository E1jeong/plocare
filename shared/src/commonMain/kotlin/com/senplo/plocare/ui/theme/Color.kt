package com.senplo.plocare.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.senplo.plocare.domain.filter.FilterType

data class PloCareColors(
    // Figma Main tokens
    val Primary: Color = Color(0xFF1F5F8B),
    val GradientMain: Color = Color(0xFF29B8E9),
    val GradientSecondary: Color = Color(0xFF8FD0E6),
    val PrimaryTint: Color = Color(0xFFD6EEF6),
    val PrimaryContainerLight: Color = Color(0xFFF2FCFF),
    val Background: Color = Color(0xFFF7F7F8),
    val TextMain: Color = Color(0xFF1F2927),

    // Figma Greyscale 12 steps (15:181)
    val Black100: Color = Color(0xFF171B25),
    val Black90: Color = Color(0xFF1F2329),
    val Black80Title: Color = Color(0xFF292D33),
    val Black70: Color = Color(0xFF333941),
    val Black60Body: Color = Color(0xFF505966),
    val Black50Muted: Color = Color(0xFF898F95),
    val Black40Ghost: Color = Color(0xFFADB2B8),
    val Black30: Color = Color(0xFFBEC3C9),
    val Black20: Color = Color(0xFFDCE2E5),
    val Black10: Color = Color(0xFFE5E9F1),
    val Black0Surface: Color = Color(0xFFF6F8FD),
    val White: Color = Color(0xFFFFFFFF),

    // Figma Filter colors (15:320)
    val FilterSediment: Color = Color(0xFF80C15E),
    val FilterPreCarbon: Color = Color(0xFFC9A46E),
    val FilterPostCarbon: Color = Color(0xFF2C5ECE),
    val FilterMembrane: Color = Color(0xFF29A2A0),
    val FilterUnassigned: Color = Color(0xFFADB2B8),

    // Legacy compatibility tokens mapped to Light Theme
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
    fun filterTypeColor(type: FilterType): Color = when (type) {
        FilterType.SEDIMENT -> FilterSediment
        FilterType.PRE_CARBON -> FilterPreCarbon
        FilterType.POST_CARBON -> FilterPostCarbon
        FilterType.MEMBRANE -> FilterMembrane
        FilterType.COMPOSITE, FilterType.UNSPECIFIED -> FilterUnassigned
    }

    fun filterStageColor(stage: Int): Color = when (stage) {
        1 -> AquaTeal
        2 -> VividCyan
        3 -> FilterStage3
        4 -> FilterStage4
        else -> FilterStage5
    }
}

val UserPloCareColors = PloCareColors(
    Primary = Color(0xFF1F5F8B),
    GradientMain = Color(0xFF29B8E9),
    GradientSecondary = Color(0xFF8FD0E6),
    PrimaryTint = Color(0xFFD6EEF6),
    PrimaryContainerLight = Color(0xFFF2FCFF),
    Background = Color(0xFFF7F7F8),
    TextMain = Color(0xFF1F2927),
    BrandNavy = Color(0xFFF7F7F8),
    BgDeep = Color(0xFFFFFFFF),
    BgMid = Color(0xFFF2FCFF),
    SurfaceDark = Color(0xFFFFFFFF),
    SurfaceCard = Color(0xFFF6F8FD),
    SurfaceBorder = Color(0xFFE5E9F1),
    AquaTeal = Color(0xFF1F5F8B),
    VividCyan = Color(0xFF29B8E9),
    LuminousIce = Color(0xFFD6EEF6),
    DeepBlue = Color(0xFF8FD0E6),
    FilterStage3 = Color(0xFF29A2A0),
    FilterStage4 = Color(0xFF2C5ECE),
    FilterStage5 = Color(0xFFADB2B8),
    TextPrimary = Color(0xFF1F2927),
    TextSecondary = Color(0xFF505966),
    TextTertiary = Color(0xFF898F95),
    StatusGood = Color(0xFF10B981),
    StatusWarn = Color(0xFFF59E0B),
    StatusAlert = Color(0xFFFF4D4F),
)

val PartnerPloCareColors = PloCareColors(
    Primary = Color(0xFF174B73),
    GradientMain = Color(0xFF2596C9),
    GradientSecondary = Color(0xFF8FD0E6),
    PrimaryTint = Color(0xFFD6EEF6),
    PrimaryContainerLight = Color(0xFFF2FCFF),
    Background = Color(0xFFF3F6FA),
    TextMain = Color(0xFF1F2927),
    BrandNavy = Color(0xFFF3F6FA),
    BgDeep = Color(0xFFFFFFFF),
    BgMid = Color(0xFFEFF6FF),
    SurfaceDark = Color(0xFFFFFFFF),
    SurfaceCard = Color(0xFFF6F8FD),
    SurfaceBorder = Color(0xFFE5E9F1),
    AquaTeal = Color(0xFF174B73),
    VividCyan = Color(0xFF2596C9),
    LuminousIce = Color(0xFFD6EEF6),
    DeepBlue = Color(0xFF8FD0E6),
    FilterStage3 = Color(0xFF29A2A0),
    FilterStage4 = Color(0xFF2C5ECE),
    FilterStage5 = Color(0xFFADB2B8),
    TextPrimary = Color(0xFF1F2927),
    TextSecondary = Color(0xFF505966),
    TextTertiary = Color(0xFF898F95),
    StatusGood = Color(0xFF10B981),
    StatusWarn = Color(0xFFF59E0B),
    StatusAlert = Color(0xFFFF4D4F),
)

internal val LocalPloCareColors = staticCompositionLocalOf { UserPloCareColors }

val PloCareColor: PloCareColors
    @Composable
    @ReadOnlyComposable
    get() = LocalPloCareColors.current
