package com.senplo.plocare.domain.filter

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlin.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FilterLifeCalculatorTest {

    @Test
    fun dailyAverageUsesFourteenDayWindowAndFloor() {
        assertEquals(1.0, FilterLifeCalculator.dailyAverage(emptyList()))
        assertEquals(1.0, FilterLifeCalculator.dailyAverage(listOf(0.0, 0.2)))
        assertEquals(4.0, FilterLifeCalculator.dailyAverage(listOf(99.0) + List(14) { 4.0 }))
    }

    @Test
    fun filterUsageAndExhaustionFollowSpec() {
        assertEquals(220.0, FilterLifeCalculator.filterUsageL(1_000.0, 780.0))
        assertEquals(0.0, FilterLifeCalculator.filterUsageL(100.0, 180.0))
        val over = FilterLifeCalculator.exhaustionRatePercent(1_545.0, 1_500.0)
        assertTrue(over > 100.0)
        assertEquals(103.0, over, 0.01)
    }

    @Test
    fun remainingDaysAreZeroWhenCapacityIsExceeded() {
        assertEquals(0, FilterLifeCalculator.remainingDays(1_545.0, 1_500.0, 4.2))
        assertEquals(12, FilterLifeCalculator.remainingDays(1_449.6, 1_500.0, 4.2))
        assertEquals(3, FilterLifeCalculator.excessDays(12.6, 4.2))
    }

    @Test
    fun bundleMergesFiltersWithinFourteenDays() {
        val clusters = FilterLifeCalculator.bundleClusters(
            listOf("a" to 12, "b" to 20, "c" to 180, "d" to 320),
        )
        assertEquals(1, clusters.size)
        assertEquals(setOf("a", "b"), clusters.single().toSet())
    }

    @Test
    fun bundleDoesNotFormWhenDeltaExceedsFourteenDays() {
        val clusters = FilterLifeCalculator.bundleClusters(
            listOf("a" to 0, "b" to 90),
        )
        assertTrue(clusters.isEmpty())
    }
}

class DashboardSnapshotTest {
    private val timeZone = TimeZone.of("Asia/Seoul")
    private val today = LocalDate(2026, 9, 10)

    private fun now(): Instant = Instant.parse("2026-09-10T03:00:00Z")

    @Test
    fun kitchenFixtureIsMatureWithBundleAndVisitTargets() {
        val now = now()
        val device = DashboardFixtures.kitchenPurifier(today, now)
        val snapshot = buildDashboardSnapshot(device, now, timeZone)

        assertEquals(ForecastStage.MATURE, snapshot.forecastStage)
        assertEquals(4, snapshot.filters.size)
        assertEquals(12, snapshot.representativeRemainingDays)
        assertEquals(LocalDate(2026, 9, 22), snapshot.representativeDate)
        assertFalse(snapshot.sensorStale)
        assertTrue(snapshot.overCapacityFilters.isEmpty())
        val bundle = assertNotNull(snapshot.bundle)
        assertEquals(setOf("kitchen-1", "kitchen-2"), bundle.filterIds.toSet())
        assertEquals(8, bundle.deltaDays)
        assertEquals(FilterColorLevel.REPLACE_SOON, snapshot.filters[0].colorLevel)
        assertEquals(FilterColorLevel.REPLACE_SOON, snapshot.filters[1].colorLevel)
        assertTrue(snapshot.filters[0].showReplacementRequest)
        assertTrue(snapshot.filters[1].showReplacementRequest)
        assertEquals(FilterColorLevel.SAFE, snapshot.filters[2].colorLevel)
    }

    @Test
    fun replaceSoonUsesExhaustionNotRemainingDays() {
        val now = now()
        val original = DashboardFixtures.kitchenPurifier(today, now)
        val slowUse = original.copy(dailyAvgL = 1.0)
        val snapshot = buildDashboardSnapshot(slowUse, now, timeZone)
        val stage1 = snapshot.filters.single { it.id == "kitchen-1" }

        assertTrue(stage1.remainingDays > 14)
        assertTrue(stage1.exhaustionPercent >= FilterLifeCalculator.REPLACE_SOON_EXHAUSTION_PERCENT)
        assertEquals(FilterColorLevel.REPLACE_SOON, stage1.colorLevel)
        assertTrue(stage1.showReplacementRequest)
    }

    @Test
    fun officeFixtureIsColdStartWithStaleSensorAndOverCapacity() {
        val now = now()
        val device = DashboardFixtures.officePurifier(today, now)
        val snapshot = buildDashboardSnapshot(device, now, timeZone)

        assertEquals(ForecastStage.COLD_START, snapshot.forecastStage)
        assertEquals(19, snapshot.installedDayIndex)
        assertTrue(snapshot.sensorStale)
        assertEquals(1, snapshot.overCapacityFilters.size)
        assertEquals(45.0, snapshot.overCapacityFilters.single().excessL, 0.01)
        assertEquals(11, snapshot.overCapacityFilters.single().excessDays)
        assertNull(snapshot.bundle)
        assertEquals(2, snapshot.filters.size)
    }

    @Test
    fun selfReplacementUpdatesOnlySelectedBaseline() {
        val now = now()
        val original = DashboardFixtures.kitchenPurifier(today, now)
        val replaced = original.withSelfReplaced("kitchen-1")
        val snapshot = buildDashboardSnapshot(replaced, now, timeZone)

        assertEquals(original.totalCumulativeL, replaced.totalCumulativeL)
        val stage1 = replaced.filters.single { it.id == "kitchen-1" }
        assertEquals(original.totalCumulativeL, stage1.baselineL)
        original.filters.filter { it.id != "kitchen-1" }.forEach { untouched ->
            assertEquals(untouched.baselineL, replaced.filters.single { it.id == untouched.id }.baselineL)
        }
        val replacedSnap = snapshot.filters.single { it.id == "kitchen-1" }
        assertEquals(0.0, replacedSnap.usageL, 0.01)
        assertEquals(0.0, replacedSnap.exhaustionPercent, 0.01)
        assertNull(snapshot.bundle)
    }

    @Test
    fun forecastStageSplitsAtSixtyDays() {
        val now = now()
        val mature = DashboardFixtures.kitchenPurifier(today, now)
        val cold = mature.copy(installedOn = today)
        assertEquals(
            ForecastStage.MATURE,
            buildDashboardSnapshot(mature, now, timeZone).forecastStage,
        )
        assertEquals(
            ForecastStage.COLD_START,
            buildDashboardSnapshot(cold, now, timeZone).forecastStage,
        )
        val day59 = mature.copy(
            installedOn = LocalDate(2026, 7, 13),
        )
        assertEquals(
            ForecastStage.COLD_START,
            buildDashboardSnapshot(day59, now, timeZone).forecastStage,
        )
        val day60 = mature.copy(installedOn = LocalDate(2026, 7, 12))
        assertEquals(
            ForecastStage.MATURE,
            buildDashboardSnapshot(day60, now, timeZone).forecastStage,
        )
    }
}
