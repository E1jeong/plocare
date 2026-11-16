package com.senplo.plocare.domain.filter

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.round

object FilterLifeCalculator {
    const val MIN_DAILY_AVG_L = 1.0
    const val BUNDLE_PROXIMITY_DAYS = 14
    const val COLD_START_DAYS = 60
    const val SENSOR_STALE_HOURS = 24
    const val REPLACE_SOON_EXHAUSTION_PERCENT = 90.0
    const val DEFAULT_KV_L_PER_SEC = 0.015
    const val KV_PHYSICAL_MIN = 0.010
    const val KV_PHYSICAL_MAX = 0.025
    const val KV_CALIBRATION_MIN = 0.008
    const val KV_CALIBRATION_MAX = 0.030
    const val FLOW_TEST_MIN_ML = 500.0
    const val FLOW_TEST_MAX_ML = 2_000.0
    const val BOTTLE_LITERS = 2.0
    const val PLASTIC_KG_PER_BOTTLE = 0.025

    fun bottlesSaved(totalCumulativeL: Double): Int =
        floor(max(0.0, totalCumulativeL) / BOTTLE_LITERS).toInt()

    fun plasticSavedKg(totalCumulativeL: Double): Double =
        bottlesSaved(totalCumulativeL) * PLASTIC_KG_PER_BOTTLE

    fun dailyAverage(recentDailyUsageL: List<Double>): Double {
        if (recentDailyUsageL.isEmpty()) return MIN_DAILY_AVG_L
        val window = recentDailyUsageL.takeLast(14)
        return max(window.sum() / window.size, MIN_DAILY_AVG_L)
    }

    fun filterUsageL(totalCumulativeL: Double, baselineL: Double): Double =
        max(0.0, totalCumulativeL - baselineL)

    fun exhaustionRatePercent(filterUsageL: Double, ratedCapacityL: Double): Double {
        if (ratedCapacityL <= 0.0) return 0.0
        return (filterUsageL / ratedCapacityL) * 100.0
    }

    fun cycleExhaustionPercent(
        cycleStartDate: LocalDate,
        today: LocalDate,
        cycleExpiryDate: LocalDate,
    ): Double {
        val totalDays = (cycleExpiryDate.toEpochDays() - cycleStartDate.toEpochDays()).toDouble()
        if (totalDays <= 0.0) return 100.0
        val elapsedDays = max(0.0, (today.toEpochDays() - cycleStartDate.toEpochDays()).toDouble())
        return (elapsedDays / totalDays) * 100.0
    }

    fun remainingCapacityL(filterUsageL: Double, ratedCapacityL: Double): Double {
        if (ratedCapacityL <= 0.0) return 0.0
        return max(0.0, ratedCapacityL - filterUsageL)
    }

    fun remainingDays(
        filterUsageL: Double,
        ratedCapacityL: Double,
        dailyAvgL: Double,
    ): Int {
        if (ratedCapacityL <= 0.0 || filterUsageL >= ratedCapacityL) return 0
        val remaining = remainingCapacityL(filterUsageL, ratedCapacityL)
        val avg = max(dailyAvgL, MIN_DAILY_AVG_L)
        return round(remaining / avg).toInt()
    }

    /**
     * Returns the capacity-based replacement date when `ratedCapacityL > 0`,
     * or `null` when rated capacity is unpopulated (`CAT-01` fallback).
     */
    fun capacityDate(
        today: LocalDate,
        filterUsageL: Double,
        ratedCapacityL: Double,
        dailyAvgL: Double,
    ): LocalDate? {
        if (ratedCapacityL <= 0.0) return null
        val days = remainingDays(filterUsageL, ratedCapacityL, dailyAvgL)
        return today.plus(days, DateTimeUnit.DAY)
    }

    /**
     * Computes `EffectiveDate = min(CapacityDate, CycleExpiryDate)` and its reason.
     * Falls back to `CycleExpiryDate` when `CapacityDate` is absent (`CAT-01`),
     * and to `CapacityDate` when `CycleExpiryDate` is absent.
     */
    fun effectiveDateAndReason(
        today: LocalDate,
        capacityDate: LocalDate?,
        cycleExpiryDate: LocalDate?,
    ): Pair<LocalDate, ReplacementReason> = when {
        capacityDate != null && cycleExpiryDate != null -> {
            if (cycleExpiryDate < capacityDate) {
                cycleExpiryDate to ReplacementReason.CYCLE_EXPIRY
            } else {
                capacityDate to ReplacementReason.CAPACITY
            }
        }
        cycleExpiryDate != null -> cycleExpiryDate to ReplacementReason.CYCLE_EXPIRY
        capacityDate != null -> capacityDate to ReplacementReason.CAPACITY
        else -> today to ReplacementReason.CAPACITY
    }

    fun effectiveRemainingDays(today: LocalDate, effectiveDate: LocalDate): Int =
        max(0, (effectiveDate.toEpochDays() - today.toEpochDays()).toInt())

    fun isUrgent(
        filterUsageL: Double,
        ratedCapacityL: Double,
        today: LocalDate,
        cycleExpiryDate: LocalDate?,
    ): Boolean {
        val capacityExhausted = ratedCapacityL > 0.0 && filterUsageL >= ratedCapacityL
        val cycleExpired = cycleExpiryDate != null && cycleExpiryDate <= today
        return capacityExhausted || cycleExpired
    }

    fun excessLiters(filterUsageL: Double, ratedCapacityL: Double): Double {
        if (ratedCapacityL <= 0.0) return 0.0
        return max(0.0, filterUsageL - ratedCapacityL)
    }

    fun excessDays(excessL: Double, dailyAvgL: Double): Int {
        if (excessL <= 0.0) return 0
        val avg = max(dailyAvgL, MIN_DAILY_AVG_L)
        return round(excessL / avg).toInt()
    }

    fun bundleClusters(remainingDaysById: List<Pair<String, Int>>): List<List<String>> {
        val n = remainingDaysById.size
        if (n < 2) return emptyList()
        val parent = IntArray(n) { it }
        fun find(i: Int): Int {
            var current = i
            while (parent[current] != current) {
                parent[current] = parent[parent[current]]
                current = parent[current]
            }
            return current
        }
        fun union(i: Int, j: Int) {
            val pi = find(i)
            val pj = find(j)
            if (pi != pj) parent[pj] = pi
        }
        for (i in 0 until n) {
            for (j in i + 1 until n) {
                val delta = abs(remainingDaysById[i].second - remainingDaysById[j].second)
                if (delta <= BUNDLE_PROXIMITY_DAYS) union(i, j)
            }
        }
        return remainingDaysById.indices
            .groupBy { find(it) }
            .values
            .map { indices -> indices.map { remainingDaysById[it].first } }
            .filter { it.size >= 2 }
    }

    fun predictedOneMinuteMl(kv: Double): Double = 60.0 * kv * 1000.0

    fun calibrateKv(currentKv: Double, measuredMl: Double): Double {
        val predicted = predictedOneMinuteMl(currentKv)
        if (predicted <= 0.0) return currentKv.coerceIn(KV_CALIBRATION_MIN, KV_CALIBRATION_MAX)
        return (currentKv * (measuredMl / predicted)).coerceIn(KV_CALIBRATION_MIN, KV_CALIBRATION_MAX)
    }

    /** Fixture preview until the specified 2D Kv lookup table exists. */
    fun previewKv(pipeSize: String, pressureKgf: Double): Double {
        val pipeFactor = when (pipeSize) {
            "3/8\"" -> 1.08
            "1/2\"" -> 1.16
            else -> 1.0
        }
        val pressureFactor = (pressureKgf / 2.0).coerceIn(0.7, 1.3)
        return (DEFAULT_KV_L_PER_SEC * pipeFactor * pressureFactor).coerceIn(KV_PHYSICAL_MIN, KV_PHYSICAL_MAX)
    }
}
