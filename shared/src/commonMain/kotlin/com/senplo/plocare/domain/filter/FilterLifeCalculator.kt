package com.senplo.plocare.domain.filter

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

    fun remainingCapacityL(filterUsageL: Double, ratedCapacityL: Double): Double =
        max(0.0, ratedCapacityL - filterUsageL)

    fun remainingDays(
        filterUsageL: Double,
        ratedCapacityL: Double,
        dailyAvgL: Double,
    ): Int {
        if (filterUsageL >= ratedCapacityL) return 0
        val remaining = remainingCapacityL(filterUsageL, ratedCapacityL)
        val avg = max(dailyAvgL, MIN_DAILY_AVG_L)
        return round(remaining / avg).toInt()
    }

    fun excessLiters(filterUsageL: Double, ratedCapacityL: Double): Double =
        max(0.0, filterUsageL - ratedCapacityL)

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
