package com.senplo.plocare.ui.consumer.dashboard

import com.senplo.plocare.domain.filter.DashboardFixtures
import com.senplo.plocare.domain.filter.FilterColorLevel
import com.senplo.plocare.domain.filter.FilterSnapshot
import com.senplo.plocare.domain.filter.buildDashboardSnapshot
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.time.Instant

class DashboardCopyTest {
    private val timeZone = TimeZone.of("Asia/Seoul")
    private val today = LocalDate(2026, 9, 10) // Thursday
    private val now = Instant.parse("2026-09-10T03:00:00Z")

    @Test
    fun formatsUsageOverRatedAndPercent() {
        assertEquals("1,500", formatIntWithComma(1_500))
        val filter = sampleFilter(usageL = 2_950.4, rated = 3_000.0, percent = 98.3)
        assertEquals("1단 세디먼트", filterTitle(filter))
        assertEquals("2,950/3,000L", usageOverRated(filter))
        assertEquals("98%", usagePercentLabel(filter))
    }

    @Test
    fun cycleOnlyFilterShowsExpiryInsteadOfLiters() {
        val filter = sampleFilter(usageL = 0.0, rated = 0.0, percent = 40.0, days = 20)
        assertEquals("9월 30일 주기 만료", usageOverRated(filter))
    }

    @Test
    fun gaugeFillsOneCellPerTenPercent() {
        assertEquals(0, gaugeFilledCells(0.0))
        assertEquals(0, gaugeFilledCells(-5.0))
        assertEquals(1, gaugeFilledCells(0.1))
        assertEquals(6, gaugeFilledCells(56.0))
        assertEquals(10, gaugeFilledCells(98.0))
        assertEquals(10, gaugeFilledCells(140.0))
        assertEquals(
            listOf(0.3f, 0.3f, 0.5f, 0.5f, 0.7f, 0.7f, 0.85f, 0.85f, 1f, 1f),
            (0 until GAUGE_CELL_COUNT).map(::gaugeCellAlpha),
        )
    }

    @Test
    fun bannerShowsNearestFilterWithinTwoWeeks() {
        val banner = assertNotNull(
            replacementBanner(snapshotWith(sampleFilter(days = 12, remainingL = 50.0), sampleFilter(id = "b", stage = 2, days = 40))),
        )
        assertEquals("a", banner.filterId)
        assertEquals("1단 세디먼트", banner.filterLabel)
        assertEquals("9월 22일(화) 교체 예상", banner.headline)
        assertEquals("남은 50L", banner.remainingLabel)
        assertEquals("9월 15일까지 구매하면 제때 받아요", banner.purchaseHint)
    }

    @Test
    fun bannerHiddenWhenEveryFilterIsFarOff() {
        assertNull(replacementBanner(snapshotWith(sampleFilter(days = 15), sampleFilter(id = "b", days = 40))))
    }

    @Test
    fun bannerHandlesOverdueAndLatePurchase() {
        val overdue = assertNotNull(replacementBanner(snapshotWith(sampleFilter(usageL = 1_545.0, remainingL = 0.0, days = 0))))
        assertEquals("교체 시기가 지났어요", overdue.headline)
        assertNull(overdue.remainingLabel)
        assertEquals("지금 구매하면 가장 빨리 받아요", overdue.purchaseHint)

        val lastDay = assertNotNull(replacementBanner(snapshotWith(sampleFilter(days = PURCHASE_LEAD_DAYS))))
        assertEquals("지금 구매하면 가장 빨리 받아요", lastDay.purchaseHint)

        val cycleOnly = assertNotNull(replacementBanner(snapshotWith(sampleFilter(rated = 0.0, days = 5))))
        assertNull(cycleOnly.remainingLabel)
    }

    private fun snapshotWith(vararg filters: FilterSnapshot) =
        buildDashboardSnapshot(DashboardFixtures.kitchenPurifier(today, now), now, timeZone)
            .copy(today = today, filters = filters.toList())

    private fun sampleFilter(
        id: String = "a",
        stage: Int = 1,
        usageL: Double = 1_000.0,
        rated: Double = 1_500.0,
        remainingL: Double = rated - usageL,
        percent: Double = 50.0,
        days: Int = 30,
    ): FilterSnapshot {
        val date = today.plus(days, DateTimeUnit.DAY)
        return FilterSnapshot(
            id = id,
            stage = stage,
            name = "세디먼트",
            ratedCapacityL = rated,
            baselineL = 0.0,
            usageL = usageL,
            remainingL = remainingL,
            exhaustionPercent = percent,
            remainingDays = days,
            estimatedDate = date,
            excessL = 0.0,
            excessDays = 0,
            colorLevel = FilterColorLevel.SAFE,
            showReplacementRequest = false,
        )
    }
}
