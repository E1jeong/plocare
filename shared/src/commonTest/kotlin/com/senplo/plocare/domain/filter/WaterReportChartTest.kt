package com.senplo.plocare.domain.filter

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
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

    @Test
    fun ecoFollowsSelectedDeviceLifetimeWithoutBackendCarbon() {
        val kitchen = DashboardFixtures.kitchenPurifier(today, now)
        val office = DashboardFixtures.officePurifier(today, now)
        val kitchenEco = buildWaterReportEco(kitchen)
        val officeEco = buildWaterReportEco(office)

        assertEquals(2_410, kitchenEco.bottlesSaved)
        assertEquals(60.25, kitchenEco.plasticKg, 0.0001)
        assertEquals("60.3", formatWaterReportDecimal(kitchenEco.plasticKg))
        assertTrue(kitchenEco.carbonDetail.contains("로컬 추정"))
        assertTrue(kitchenEco.carbonDetail.contains("서버에서 계산하지 않습니다"))
        assertFalse(kitchenEco.carbonDetail.contains("kg CO"))
        assertFalse(kitchenEco.carbonDetail.contains("서버에서 계산했습니다"))

        assertEquals(1_050, officeEco.bottlesSaved)
        assertEquals(26.25, officeEco.plasticKg, 0.0001)
        assertNotEquals(kitchenEco.plasticKg, officeEco.plasticKg)
        assertTrue(officeEco.carbonDetail.contains("26.3 kg"))
    }

    @Test
    fun kitchenHistoryStaysOnFixtureCopy() {
        val kitchen = DashboardFixtures.kitchenPurifier(today, now)
        val history = buildWaterReportHistory(kitchen)
        assertEquals(3, history.size)
        assertEquals("2026.08.12", history[0].dateLabel)
        assertEquals("1단 세디먼트 카본", history[0].title)
        assertEquals("직접 교체 · 누적 4,320 L", history[0].detail)
        assertEquals("2026.03.15", history[1].dateLabel)
        assertEquals("1단 세디먼트 카본 · 2단 프리카본", history[1].title)
        assertEquals("PloCare 파트너 김*수 · 누적 3,120 L", history[1].detail)
        assertEquals("2025.11.02", history[2].dateLabel)
        assertEquals("4단 포스트 실버 항균", history[2].title)
        val boundary = waterReportHistoryBoundary()
        assertTrue(boundary.contains("로컬 이력"))
        assertTrue(boundary.contains("서버에서 불러오지 않았습니다"))
        assertFalse(boundary.contains("서버에서 불러왔습니다"))
    }

    @Test
    fun officeHistoryDoesNotInventBackendRows() {
        val office = DashboardFixtures.officePurifier(today, now)
        assertEquals(emptyList(), office.replacementHistory)
        assertEquals(emptyList(), buildWaterReportHistory(office))
        val empty = waterReportHistoryEmptyCopy()
        assertTrue(empty.contains("로컬 이력이 없습니다"))
        assertTrue(empty.contains("서버에서 불러오지 않았습니다"))
        assertFalse(empty.contains("서버 교체 이력"))
    }
}
