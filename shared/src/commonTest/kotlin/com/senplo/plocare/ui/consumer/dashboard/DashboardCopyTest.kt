package com.senplo.plocare.ui.consumer.dashboard

import com.senplo.plocare.domain.filter.DashboardFixtures
import com.senplo.plocare.domain.filter.FilterSnapshot
import com.senplo.plocare.domain.filter.FilterColorLevel
import com.senplo.plocare.domain.filter.buildDashboardSnapshot
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlin.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DashboardCopyTest {
    private val timeZone = TimeZone.of("Asia/Seoul")
    private val today = LocalDate(2026, 9, 10)

    private fun now(): Instant = Instant.parse("2026-09-10T03:00:00Z")

    @Test
    fun formatsLiterGroupsAndDDay() {
        assertEquals("1,500", formatIntWithComma(1_500))
        assertEquals("50 L / 1,500 L", remainingOverRated(sampleFilter(remainingL = 50.4, rated = 1_500.0, days = 12)))
        assertEquals("D-12일", dDayLabel(12))
        assertEquals("수명 초과", dDayLabel(0))
        assertEquals("4.2", formatOneDecimal(4.2))
        assertEquals("10", formatOneDecimal(10.0))
    }

    @Test
    fun briefingFollowsForecastStage() {
        val now = now()
        val kitchen = buildDashboardSnapshot(
            DashboardFixtures.kitchenPurifier(today, now),
            now,
            timeZone,
        )
        assertTrue(briefingTitle(kitchen).contains("AI 정밀 추론 가동 중"))
        assertTrue(briefingTitle(kitchen).contains("4.2 L/일"))
        assertTrue(briefingCaption(kitchen).contains("자정"))

        val office = buildDashboardSnapshot(
            DashboardFixtures.officePurifier(today, now()),
            now(),
            timeZone,
        )
        assertTrue(briefingTitle(office).contains("2인 가구 기준 예측"))
        assertTrue(briefingCaption(office).contains("가구원 수"))
    }

    @Test
    fun chunkFilterRowsMatchesDemoPresets() {
        val two = List(2) { sampleFilter(id = "f$it") }
        val four = List(4) { sampleFilter(id = "f$it") }
        assertEquals(1, chunkFilterRows(two).size)
        assertEquals(2, chunkFilterRows(four).size)
        assertEquals(2, chunkFilterRows(four)[0].size)
        assertEquals(listOf(2, 1), chunkFilterRows(List(3) { sampleFilter(id = "f$it") }).map { it.size })
    }

    private fun sampleFilter(
        id: String = "kitchen-1",
        remainingL: Double = 50.0,
        rated: Double = 1_500.0,
        days: Int = 12,
    ) = FilterSnapshot(
        id = id,
        stage = 1,
        name = "세디먼트 카본",
        ratedCapacityL = rated,
        baselineL = 0.0,
        usageL = rated - remainingL,
        remainingL = remainingL,
        exhaustionPercent = ((rated - remainingL) / rated) * 100.0,
        remainingDays = days,
        estimatedDate = today,
        excessL = 0.0,
        excessDays = 0,
        colorLevel = FilterColorLevel.REPLACE_SOON,
        showReplacementRequest = true,
    )
}
