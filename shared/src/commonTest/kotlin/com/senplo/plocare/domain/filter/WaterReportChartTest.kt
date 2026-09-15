package com.senplo.plocare.domain.filter

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.time.Instant

class WaterReportChartTest {
    private val today = LocalDate(2026, 9, 10)
    private val now = Instant.parse("2026-09-10T03:00:00Z")

    @Test
    fun chartAverageMatchesCalculatorForSelectedDevice() {
        val kitchen = DashboardFixtures.kitchenPurifier(today, now)
        val office = DashboardFixtures.officePurifier(today, now)

        val kitchenChart = buildWaterReportChart(kitchen)
        assertEquals(kitchen.recentDailyUsageL.takeLast(14), kitchenChart.dailyUsageL)
        assertEquals(FilterLifeCalculator.dailyAverage(kitchen.recentDailyUsageL), kitchenChart.averageL)
        assertEquals(kitchen.dailyAvgL, kitchenChart.averageL, 0.01)
        assertEquals(kitchen.recentDailyUsageL.last(), kitchenChart.todayL)
        assertEquals(kitchen.recentDailyUsageL.takeLast(14).sum(), kitchenChart.periodSumL, 0.01)
        assertEquals(14, kitchenChart.periodDays)

        val officeChart = buildWaterReportChart(office)
        assertEquals(office.recentDailyUsageL.takeLast(14), officeChart.dailyUsageL)
        assertEquals(FilterLifeCalculator.dailyAverage(office.recentDailyUsageL), officeChart.averageL)
        assertNotEquals(kitchenChart.dailyUsageL, officeChart.dailyUsageL)
        assertEquals(office.recentDailyUsageL.last(), officeChart.todayL)
        assertNotEquals(kitchenChart.todayL, officeChart.todayL)
    }

    @Test
    fun chartUsesLastFourteenDaysOnly() {
        val extra = List(5) { 9.0 } + List(14) { 4.2 }
        val device = DashboardFixtures.kitchenPurifier(today, now).copy(recentDailyUsageL = extra)
        val chart = buildWaterReportChart(device)

        assertEquals(List(14) { 4.2 }, chart.dailyUsageL)
        assertEquals(FilterLifeCalculator.dailyAverage(extra), chart.averageL)
        assertEquals(4.2, chart.averageL, 0.01)
    }

    @Test
    fun emptyUsageFloorsAverageToCalculator() {
        val device = DashboardFixtures.kitchenPurifier(today, now).copy(recentDailyUsageL = emptyList())
        val chart = buildWaterReportChart(device)

        assertEquals(emptyList(), chart.dailyUsageL)
        assertEquals(FilterLifeCalculator.dailyAverage(emptyList()), chart.averageL)
        assertEquals(FilterLifeCalculator.MIN_DAILY_AVG_L, chart.averageL)
        assertEquals(0.0, chart.todayL)
        assertEquals(0, chart.periodDays)
    }
}
