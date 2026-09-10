package com.senplo.plocare.domain.filter

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant
import kotlin.math.max

enum class ForecastStage {
    COLD_START,
    MATURE,
}

enum class FilterColorLevel {
    SAFE,
    REPLACE_SOON,
    EXHAUSTED,
}

data class FilterSnapshot(
    val id: String,
    val stage: Int,
    val name: String,
    val ratedCapacityL: Double,
    val baselineL: Double,
    val usageL: Double,
    val remainingL: Double,
    val exhaustionPercent: Double,
    val remainingDays: Int,
    val estimatedDate: LocalDate,
    val excessL: Double,
    val excessDays: Int,
    val colorLevel: FilterColorLevel,
    val showReplacementRequest: Boolean,
) {
    val fillFraction: Float
        get() = if (ratedCapacityL <= 0.0) 0f else (remainingL / ratedCapacityL).toFloat().coerceIn(0f, 1f)
}

data class BundleProposal(
    val filterIds: List<String>,
    val stageLabels: List<String>,
    val names: List<String>,
    val deltaDays: Int,
    val targetDate: LocalDate,
)

data class DashboardSnapshot(
    val device: PurifierDevice,
    val today: LocalDate,
    val filters: List<FilterSnapshot>,
    val forecastStage: ForecastStage,
    val installedDayIndex: Int,
    val representativeRemainingDays: Int,
    val representativeDate: LocalDate,
    val sensorStale: Boolean,
    val overCapacityFilters: List<FilterSnapshot>,
    val bundle: BundleProposal?,
)

fun buildDashboardSnapshot(
    device: PurifierDevice,
    now: Instant,
    timeZone: TimeZone,
): DashboardSnapshot {
    val today = now.toLocalDateTime(timeZone).date
    val daysSinceInstall = (today.toEpochDays() - device.installedOn.toEpochDays()).toInt()
    val forecastStage = if (daysSinceInstall < FilterLifeCalculator.COLD_START_DAYS) {
        ForecastStage.COLD_START
    } else {
        ForecastStage.MATURE
    }
    val hoursSinceTelemetry = (now - device.lastTelemetryAt).inWholeHours
    val sensorStale = hoursSinceTelemetry >= FilterLifeCalculator.SENSOR_STALE_HOURS

    val filters = device.filters.map { cartridge ->
        val usage = FilterLifeCalculator.filterUsageL(device.totalCumulativeL, cartridge.baselineL)
        val remaining = FilterLifeCalculator.remainingCapacityL(usage, cartridge.ratedCapacityL)
        val remainingDays = FilterLifeCalculator.remainingDays(
            filterUsageL = usage,
            ratedCapacityL = cartridge.ratedCapacityL,
            dailyAvgL = device.dailyAvgL,
        )
        val excessL = FilterLifeCalculator.excessLiters(usage, cartridge.ratedCapacityL)
        val exhaustion = FilterLifeCalculator.exhaustionRatePercent(usage, cartridge.ratedCapacityL)
        val colorLevel = when {
            remaining <= 0.0 || exhaustion >= 100.0 -> FilterColorLevel.EXHAUSTED
            exhaustion >= FilterLifeCalculator.REPLACE_SOON_EXHAUSTION_PERCENT ->
                FilterColorLevel.REPLACE_SOON
            else -> FilterColorLevel.SAFE
        }
        FilterSnapshot(
            id = cartridge.id,
            stage = cartridge.stage,
            name = cartridge.name,
            ratedCapacityL = cartridge.ratedCapacityL,
            baselineL = cartridge.baselineL,
            usageL = usage,
            remainingL = remaining,
            exhaustionPercent = exhaustion,
            remainingDays = remainingDays,
            estimatedDate = today.plus(remainingDays, DateTimeUnit.DAY),
            excessL = excessL,
            excessDays = FilterLifeCalculator.excessDays(excessL, device.dailyAvgL),
            colorLevel = colorLevel,
            showReplacementRequest = colorLevel != FilterColorLevel.SAFE,
        )
    }

    val clusters = FilterLifeCalculator.bundleClusters(
        filters.map { it.id to it.remainingDays },
    )
    val bundleFilters = clusters
        .map { ids -> filters.filter { it.id in ids } }
        .minByOrNull { cluster -> cluster.minOf { it.remainingDays } }
    val bundle = bundleFilters?.let { cluster ->
        val days = cluster.map { it.remainingDays }
        BundleProposal(
            filterIds = cluster.map { it.id },
            stageLabels = cluster.map { "${it.stage}단" },
            names = cluster.map { it.name },
            deltaDays = (days.maxOrNull() ?: 0) - (days.minOrNull() ?: 0),
            targetDate = cluster.minBy { it.remainingDays }.estimatedDate,
        )
    }

    val representative = filters.minByOrNull { it.remainingDays }
    return DashboardSnapshot(
        device = device,
        today = today,
        filters = filters,
        forecastStage = forecastStage,
        installedDayIndex = max(1, daysSinceInstall + 1),
        representativeRemainingDays = representative?.remainingDays ?: 0,
        representativeDate = representative?.estimatedDate ?: today,
        sensorStale = sensorStale,
        overCapacityFilters = filters.filter { it.colorLevel == FilterColorLevel.EXHAUSTED },
        bundle = bundle,
    )
}
