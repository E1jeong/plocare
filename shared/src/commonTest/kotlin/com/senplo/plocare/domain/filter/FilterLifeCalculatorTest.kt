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
    fun bottlesAndPlasticFollowLifetimeFixtureFormula() {
        assertEquals(2_410, FilterLifeCalculator.bottlesSaved(4_820.0))
        assertEquals(60.25, FilterLifeCalculator.plasticSavedKg(4_820.0), 0.0001)
        assertEquals(1_050, FilterLifeCalculator.bottlesSaved(2_100.0))
        assertEquals(26.25, FilterLifeCalculator.plasticSavedKg(2_100.0), 0.0001)
        assertEquals(0, FilterLifeCalculator.bottlesSaved(0.0))
        assertEquals(0.0, FilterLifeCalculator.plasticSavedKg(-12.0), 0.0001)
    }

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

    @Test
    fun kvPreviewAndCalibrationFollowSpec() {
        assertEquals(900.0, FilterLifeCalculator.predictedOneMinuteMl(0.015), 0.01)
        assertEquals(0.015, FilterLifeCalculator.previewKv("1/4\"", 2.0), 0.0001)
        val calibrated = FilterLifeCalculator.calibrateKv(0.015, 1_200.0)
        assertEquals(0.02, calibrated, 0.0001)
        assertEquals(0.030, FilterLifeCalculator.calibrateKv(0.015, 10_000.0), 0.0001)
        assertEquals(0.008, FilterLifeCalculator.calibrateKv(0.015, 10.0), 0.0001)
    }

    @Test
    fun effectiveDatePicksEarlierOfCapacityAndCycleExpiryAndSupportsCat01Fallback() {
        val today = LocalDate(2026, 9, 10)
        val capDate = LocalDate(2026, 10, 20)
        val earlierCycle = LocalDate(2026, 9, 25)
        val laterCycle = LocalDate(2026, 12, 1)

        val (effCycle, reasonCycle) = FilterLifeCalculator.effectiveDateAndReason(today, capDate, earlierCycle)
        assertEquals(earlierCycle, effCycle)
        assertEquals(ReplacementReason.CYCLE_EXPIRY, reasonCycle)
        assertEquals(15, FilterLifeCalculator.effectiveRemainingDays(today, effCycle))

        val (effCap, reasonCap) = FilterLifeCalculator.effectiveDateAndReason(today, capDate, laterCycle)
        assertEquals(capDate, effCap)
        assertEquals(ReplacementReason.CAPACITY, reasonCap)

        // CAT-01 fallback: ratedCapacityL == 0.0 produces null capacityDate and uses cycleExpiryDate safely
        val nullCap = FilterLifeCalculator.capacityDate(today, filterUsageL = 320.0, ratedCapacityL = 0.0, dailyAvgL = 4.0)
        assertNull(nullCap)
        val (effFallback, reasonFallback) = FilterLifeCalculator.effectiveDateAndReason(today, nullCap, earlierCycle)
        assertEquals(earlierCycle, effFallback)
        assertEquals(ReplacementReason.CYCLE_EXPIRY, reasonFallback)
    }

    @Test
    fun savingsCalculatorHandlesInitialLossBreakEvenProfitAndHeavyUsageHygieneMode() {
        val policy = SamplePricingPolicy(
            modulePriceKrw = 50_000,
            monthlySubscriptionFeeKrw = 500,
            defaultFilterUnitPriceKrw = 24_000,
        )
        // Month 2: initial loss period (scheduled 22,500/mo vs PloCare actual 14,000/mo + 500/mo + 50,000 module)
        val early = SavingsCalculator.calculate(
            elapsedMonths = 2,
            scheduledMonthlyFilterCostKrw = 22_500.0,
            actualMonthlyFilterCostKrw = 14_000.0,
            pricingPolicy = policy,
        )
        assertTrue(early.netSavingsKrw < 0)
        assertEquals(SavingsReportMode.BREAK_EVEN_PENDING, early.mode)
        assertEquals(7, early.estimatedBreakEvenMonth)
        assertEquals(5, early.remainingMonthsToBreakEven)

        // Month 10: cumulative profit reached
        val mature = SavingsCalculator.calculate(
            elapsedMonths = 10,
            scheduledMonthlyFilterCostKrw = 22_500.0,
            actualMonthlyFilterCostKrw = 14_000.0,
            pricingPolicy = policy,
        )
        assertTrue(mature.netSavingsKrw > 0)
        assertEquals(SavingsReportMode.PROFIT, mature.mode)
        assertEquals(0, mature.remainingMonthsToBreakEven)

        // Heavy usage: actual filter cost >= scheduled cost -> hygiene value card mode
        val heavy = SavingsCalculator.calculate(
            elapsedMonths = 12,
            scheduledMonthlyFilterCostKrw = 20_000.0,
            actualMonthlyFilterCostKrw = 22_000.0,
            pricingPolicy = policy,
        )
        assertEquals(SavingsReportMode.HYGIENE_VALUE, heavy.mode)
        assertNull(heavy.estimatedBreakEvenMonth)
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
    fun replacedFiltersUpdateOnlySelectedBaselines() {
        val now = now()
        val original = DashboardFixtures.kitchenPurifier(today, now)
        val replaced = original.withReplacedFilters(listOf("kitchen-1", "kitchen-2"))

        assertEquals(original.totalCumulativeL, replaced.totalCumulativeL)
        assertEquals(original.totalCumulativeL, replaced.filters.single { it.id == "kitchen-1" }.baselineL)
        assertEquals(original.totalCumulativeL, replaced.filters.single { it.id == "kitchen-2" }.baselineL)
        original.filters.filter { it.id !in setOf("kitchen-1", "kitchen-2") }.forEach { untouched ->
            assertEquals(untouched.baselineL, replaced.filters.single { it.id == untouched.id }.baselineL)
        }
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

    @Test
    fun cycleExpiryDrivesUrgencyBundleAndTankCumulativeRule() {
        val now = now()
        val base = DashboardFixtures.kitchenPurifier(today, now)
        // Ten-stage tank purifier with cycle-only filters (ratedCapacityL = 0.0, CAT-01 fallback)
        val tenFilters = (1..10).map { stage ->
            FilterCartridge(
                id = "tank-$stage",
                stage = stage,
                name = if (stage == 1) "세디먼트" else "복합필터 $stage",
                ratedCapacityL = 0.0,
                baselineL = base.totalCumulativeL - 100.0,
                cycleMonths = 6,
                installedOrReplacedOn = LocalDate(2026, 3, 10),
                cycleExpiryDate = when (stage) {
                    1 -> LocalDate(2026, 9, 10) // Expired today -> EXHAUSTED
                    2 -> LocalDate(2026, 9, 18) // Within 14 days of stage 1 -> bundled
                    else -> LocalDate(2026, 12, 10)
                },
            )
        }
        val tankDevice = base.copy(
            filtrationType = FiltrationType.TANK,
            managementType = ManagementType.VISIT,
            filters = tenFilters,
        )
        assertFalse(tankDevice.supportsSinglePourReadout)
        val snapshot = buildDashboardSnapshot(tankDevice, now, timeZone)

        assertEquals(10, snapshot.filters.size)
        assertEquals(0, snapshot.representativeRemainingDays)
        assertEquals(LocalDate(2026, 9, 10), snapshot.representativeDate)
        assertEquals(1, snapshot.overCapacityFilters.size)
        assertEquals("tank-1", snapshot.overCapacityFilters.single().id)
        assertEquals(ReplacementReason.CYCLE_EXPIRY, snapshot.filters[0].replacementReason)
        val bundle = assertNotNull(snapshot.bundle)
        assertEquals(setOf("tank-1", "tank-2"), bundle.filterIds.toSet())
    }
}
