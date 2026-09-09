package com.senplo.plocare.ui.theme

import androidx.compose.ui.graphics.Color
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class PloCareThemeTest {

    @Test
    fun userPaletteKeepsCurrentBrandTokens() {
        assertEquals(Color(0xFF071A2E), UserPloCareColors.BrandNavy)
        assertEquals(Color(0xFF2DD4BF), UserPloCareColors.AquaTeal)
        assertEquals(Color(0xFF38BDF8), UserPloCareColors.VividCyan)
        assertEquals(Color(0xFF070B14), UserPloCareColors.BgDeep)
    }

    @Test
    fun partnerPaletteUsesDistinctAccents() {
        assertEquals(Color(0xFF60A5FA), PartnerPloCareColors.AquaTeal)
        assertEquals(Color(0xFF93C5FD), PartnerPloCareColors.VividCyan)
        assertEquals(Color(0xFF0A1628), PartnerPloCareColors.BrandNavy)
        assertNotEquals(UserPloCareColors.AquaTeal, PartnerPloCareColors.AquaTeal)
        assertNotEquals(UserPloCareColors.VividCyan, PartnerPloCareColors.VividCyan)
        assertNotEquals(UserPloCareColors.BrandNavy, PartnerPloCareColors.BrandNavy)
        assertEquals(UserPloCareColors.StatusAlert, PartnerPloCareColors.StatusAlert)
        assertEquals(UserPloCareColors.StatusWarn, PartnerPloCareColors.StatusWarn)
        assertEquals(UserPloCareColors.StatusGood, PartnerPloCareColors.StatusGood)
    }

    @Test
    fun audienceMapsToExpectedPalette() {
        assertEquals(UserPloCareColors, AppAudience.USER.palette())
        assertEquals(PartnerPloCareColors, AppAudience.PARTNER.palette())
    }
}
