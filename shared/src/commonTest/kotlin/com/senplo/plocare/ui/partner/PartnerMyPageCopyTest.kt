package com.senplo.plocare.ui.partner

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class PartnerMyPageCopyTest {
    @Test
    fun monthRangeEndsOnTheMonthsLastDay() {
        assertEquals("10월 1일 ~ 31일", monthRangeLabel(LocalDate(2026, 10, 15)))
        assertEquals("11월 1일 ~ 30일", monthRangeLabel(LocalDate(2026, 11, 20)))
        assertEquals("12월 1일 ~ 31일", monthRangeLabel(LocalDate(2026, 12, 31)))
        assertEquals("2월 1일 ~ 29일", monthRangeLabel(LocalDate(2028, 2, 1)))
    }

    @Test
    fun sampleDaysOffAreTheNextTwoSundays() {
        // Friday
        assertEquals(
            listOf(LocalDate(2026, 11, 22), LocalDate(2026, 11, 29)),
            sampleDaysOff(LocalDate(2026, 11, 20)),
        )
        // A Sunday today moves to the following Sunday.
        assertEquals(
            listOf(LocalDate(2026, 11, 29), LocalDate(2026, 12, 6)),
            sampleDaysOff(LocalDate(2026, 11, 22)),
        )
    }

    @Test
    fun dayOffLabelUsesFigmaFormat() {
        assertEquals("11월 8일(일)", dayOffLabel(LocalDate(2026, 11, 8)))
    }
}
