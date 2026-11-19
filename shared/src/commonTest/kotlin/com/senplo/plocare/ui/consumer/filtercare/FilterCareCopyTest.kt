package com.senplo.plocare.ui.consumer.filtercare

import com.senplo.plocare.domain.filter.FilterColorLevel
import com.senplo.plocare.domain.filter.FilterSnapshot
import com.senplo.plocare.domain.filter.ReplacementReason
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class FilterCareCopyTest {
    private val today = LocalDate(2026, 11, 19)

    @Test
    fun dDayBadgeCoversFuturePastAndToday() {
        assertEquals("D-12", dDayBadge(12))
        assertEquals("D-day", dDayBadge(0))
        assertEquals("D+3", dDayBadge(-3))
    }

    @Test
    fun statusDateShowsWeekdayThisYearAndYearOtherwise() {
        val capacity = sample(date = LocalDate(2026, 11, 24), reason = ReplacementReason.CAPACITY)
        assertEquals("11월 24일(화) 도달 예상", filterStatusDateLabel(capacity, today))

        val cycle = sample(date = LocalDate(2027, 4, 8), reason = ReplacementReason.CYCLE_EXPIRY)
        assertEquals("2027년 4월 8일 교환주기 만료", filterStatusDateLabel(cycle, today))
    }

    @Test
    fun litersUseSpacedSlashAndHideForCycleOnlyFilters() {
        assertEquals("2,950 / 3,000L", usageOverRatedSpaced(sample(usageL = 2_950.2, rated = 3_000.0)))
        assertNull(usageOverRatedSpaced(sample(rated = 0.0)))
    }

    @Test
    fun ordersByNearestReplacementThenStage() {
        val ordered = sortedByReplacement(
            listOf(
                sample(id = "c", stage = 3, days = 40),
                sample(id = "b", stage = 2, days = 12),
                sample(id = "a", stage = 1, days = 12),
            ),
        )
        assertEquals(listOf("a", "b", "c"), ordered.map { it.id })
    }

    @Test
    fun usageFractionClampsToTrack() {
        assertEquals(0.98f, usageFraction(sample(percent = 98.0)), 0.001f)
        assertEquals(1f, usageFraction(sample(percent = 130.0)))
        assertEquals(0f, usageFraction(sample(percent = -1.0)))
    }

    private fun sample(
        id: String = "a",
        stage: Int = 1,
        usageL: Double = 1_000.0,
        rated: Double = 1_500.0,
        percent: Double = 50.0,
        days: Int = 30,
        date: LocalDate = today,
        reason: ReplacementReason = ReplacementReason.CAPACITY,
    ) = FilterSnapshot(
        id = id,
        stage = stage,
        name = "세디먼트",
        ratedCapacityL = rated,
        baselineL = 0.0,
        usageL = usageL,
        remainingL = rated - usageL,
        exhaustionPercent = percent,
        remainingDays = days,
        estimatedDate = date,
        excessL = 0.0,
        excessDays = 0,
        colorLevel = FilterColorLevel.SAFE,
        showReplacementRequest = false,
        replacementReason = reason,
    )
}
