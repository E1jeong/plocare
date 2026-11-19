package com.senplo.plocare.ui.consumer.filtercare

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class VisitRequestFormTest {
    private val thursday = LocalDate(2026, 9, 10)
    private val friday = LocalDate(2026, 9, 11)

    @Test
    fun earliestVisitSkipsWeekends() {
        assertEquals(LocalDate(2026, 9, 14), addBusinessDays(thursday, 2))
        assertEquals(LocalDate(2026, 9, 15), addBusinessDays(friday, 2))
    }

    @Test
    fun calendarStartsOnMondayOfThisWeekForThreeWeeks() {
        val days = visitCalendarDays(thursday)
        assertEquals(21, days.size)
        assertEquals(LocalDate(2026, 9, 7), days.first())
        assertEquals(DayOfWeek.MONDAY, days.first().dayOfWeek)
        assertEquals(LocalDate(2026, 9, 27), days.last())
    }

    @Test
    fun onlyBusinessDaysFromTheEarliestVisitAreSelectable() {
        assertFalse(isVisitDateSelectable(LocalDate(2026, 9, 11), thursday))
        assertTrue(isVisitDateSelectable(LocalDate(2026, 9, 14), thursday))
        assertFalse(isVisitDateSelectable(LocalDate(2026, 9, 19), thursday))
        assertFalse(isVisitDateSelectable(LocalDate(2026, 9, 20), thursday))
    }

    @Test
    fun preferencesKeepPriorityOrderAndCapAtThree() {
        val a = LocalDate(2026, 9, 14)
        val b = LocalDate(2026, 9, 16)
        val c = LocalDate(2026, 9, 18)
        val d = LocalDate(2026, 9, 21)
        var dates = listOf<LocalDate>()
        dates = toggleVisitDate(dates, b)
        dates = toggleVisitDate(dates, a)
        dates = toggleVisitDate(dates, c)
        assertEquals(listOf(b, a, c), dates)
        assertEquals(listOf(b, a, c), toggleVisitDate(dates, d))
        assertEquals(listOf(b, c), toggleVisitDate(dates, a))
    }

    @Test
    fun preferenceLabelShowsMonthDayAndWeekday() {
        assertEquals("9월 14일(월)", visitPreferenceLabel(LocalDate(2026, 9, 14)))
    }

    @Test
    fun filterIdCodecRoundTrips() {
        assertEquals("kitchen-1,kitchen-2", encodeVisitFilterIds(listOf("kitchen-1", "kitchen-2")))
        assertEquals(listOf("kitchen-1", "kitchen-2"), decodeVisitFilterIds("kitchen-1,kitchen-2"))
        assertEquals(emptyList(), decodeVisitFilterIds(""))
    }
}
