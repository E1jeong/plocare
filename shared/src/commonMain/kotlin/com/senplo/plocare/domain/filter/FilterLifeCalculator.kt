package com.senplo.plocare.domain.filter

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.round

object FilterLifeCalculator {
    const val MIN_DAILY_AVG_L = 1.0
    const val BUNDLE_PROXIMITY_DAYS = 14
    const val COLD_START_DAYS = 60
    const val SENSOR_STALE_HOURS = 24
    const val REPLACE_SOON_EXHAUSTION_PERCENT = 90.0

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
}
