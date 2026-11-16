package com.senplo.plocare.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import com.senplo.plocare.domain.filter.FilterType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class PloCareThemeTest {

    @Test
    fun userPaletteReflectsFigmaLightTokens() {
        assertEquals(Color(0xFF1F5F8B), UserPloCareColors.Primary)
        assertEquals(Color(0xFF29B8E9), UserPloCareColors.GradientMain)
        assertEquals(Color(0xFF8FD0E6), UserPloCareColors.GradientSecondary)
        assertEquals(Color(0xFFD6EEF6), UserPloCareColors.PrimaryTint)
        assertEquals(Color(0xFFF7F7F8), UserPloCareColors.Background)
        assertEquals(Color(0xFF1F2927), UserPloCareColors.TextMain)
        assertEquals(Color(0xFFFF4D4F), UserPloCareColors.StatusAlert)
        // Legacy compatibility aliases
        assertEquals(UserPloCareColors.Background, UserPloCareColors.BrandNavy)
        assertEquals(UserPloCareColors.Primary, UserPloCareColors.AquaTeal)
        assertEquals(UserPloCareColors.White, UserPloCareColors.BgDeep)
        assertEquals(UserPloCareColors.White, UserPloCareColors.SurfaceDark)
    }

    @Test
    fun partnerPaletteUsesDistinctAccents() {
        assertEquals(Color(0xFF174B73), PartnerPloCareColors.Primary)
        assertEquals(Color(0xFF2596C9), PartnerPloCareColors.GradientMain)
        assertEquals(Color(0xFFF3F6FA), PartnerPloCareColors.Background)
        assertNotEquals(UserPloCareColors.AquaTeal, PartnerPloCareColors.AquaTeal)
        assertNotEquals(UserPloCareColors.VividCyan, PartnerPloCareColors.VividCyan)
        assertNotEquals(UserPloCareColors.BrandNavy, PartnerPloCareColors.BrandNavy)
        assertEquals(UserPloCareColors.StatusAlert, PartnerPloCareColors.StatusAlert)
        assertEquals(UserPloCareColors.StatusWarn, PartnerPloCareColors.StatusWarn)
        assertEquals(UserPloCareColors.StatusGood, PartnerPloCareColors.StatusGood)
    }

    @Test
    fun filterColorsMapByFilterTypeWithGrayFallback() {
        assertEquals(Color(0xFF80C15E), UserPloCareColors.filterTypeColor(FilterType.SEDIMENT))
        assertEquals(Color(0xFFC9A46E), UserPloCareColors.filterTypeColor(FilterType.PRE_CARBON))
        assertEquals(Color(0xFF2C5ECE), UserPloCareColors.filterTypeColor(FilterType.POST_CARBON))
        assertEquals(Color(0xFF29A2A0), UserPloCareColors.filterTypeColor(FilterType.MEMBRANE))
        assertEquals(Color(0xFFADB2B8), UserPloCareColors.filterTypeColor(FilterType.COMPOSITE))
        assertEquals(Color(0xFFADB2B8), UserPloCareColors.filterTypeColor(FilterType.UNSPECIFIED))

        assertEquals(FilterType.SEDIMENT, FilterType.fromName("세디먼트 카본"))
        assertEquals(FilterType.PRE_CARBON, FilterType.fromName("프리카본"))
        assertEquals(FilterType.MEMBRANE, FilterType.fromName("RO 멤브레인"))
        assertEquals(FilterType.POST_CARBON, FilterType.fromName("포스트 실버 항균"))
        assertEquals(FilterType.UNSPECIFIED, FilterType.fromName("특수 미네랄"))
    }

    @Test
    fun typographyCorrectsSmallAndTinyLineHeightAnomalies() {
        val typography = buildPloCareTypography()
        assertEquals(32.sp, typography.headLarge.fontSize)
        assertEquals(38.sp, typography.headLarge.lineHeight)
        assertEquals(18.sp, typography.titleMid.fontSize)
        assertEquals(23.sp, typography.titleMid.lineHeight)
        assertTrue(typography.labelSmall.lineHeight.value > typography.labelSmall.fontSize.value)
        assertTrue(typography.labelTiny.lineHeight.value > typography.labelTiny.fontSize.value)
        assertEquals(16.sp, typography.labelSmall.lineHeight)
        assertEquals(14.sp, typography.labelTiny.lineHeight)
    }

    @Test
    fun audienceMapsToExpectedPalette() {
        assertEquals(UserPloCareColors, AppAudience.USER.palette())
        assertEquals(PartnerPloCareColors, AppAudience.PARTNER.palette())
    }
}
