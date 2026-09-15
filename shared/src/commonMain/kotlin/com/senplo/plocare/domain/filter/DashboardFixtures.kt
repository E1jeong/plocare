package com.senplo.plocare.domain.filter

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

object DashboardFixtures {
    const val KITCHEN_ID = "device-kitchen"
    const val OFFICE_ID = "device-office"

    fun devices(now: Instant, timeZone: TimeZone): List<PurifierDevice> {
        val today = now.toLocalDateTime(timeZone).date
        return listOf(
            kitchenPurifier(today, now),
            officePurifier(today, now),
        )
    }

    fun kitchenPurifier(today: LocalDate, now: Instant): PurifierDevice {
        val totalCumulativeL = 4_820.0
        val recentDailyUsageL = listOf(
            3.8, 4.5, 4.1, 4.2, 3.6, 4.9, 4.4, 4.6, 3.9, 4.7, 4.2, 4.1, 3.0, 4.8,
        )
        val dailyAvgL = FilterLifeCalculator.dailyAverage(recentDailyUsageL)
        return PurifierDevice(
            id = KITCHEN_ID,
            nickname = "우리 집 주방 정수기",
            installedOn = today.minus(120, DateTimeUnit.DAY),
            householdSize = 3,
            totalCumulativeL = totalCumulativeL,
            dailyAvgL = dailyAvgL,
            recentDailyUsageL = recentDailyUsageL,
            lastTelemetryAt = now - 2.minutes,
            filters = listOf(
                cartridge(
                    id = "kitchen-1",
                    stage = 1,
                    name = "세디먼트 카본",
                    ratedCapacityL = 1_500.0,
                    remainingDays = 12,
                    totalCumulativeL = totalCumulativeL,
                    dailyAvgL = dailyAvgL,
                ),
                cartridge(
                    id = "kitchen-2",
                    stage = 2,
                    name = "프리카본",
                    ratedCapacityL = 1_800.0,
                    remainingDays = 20,
                    totalCumulativeL = totalCumulativeL,
                    dailyAvgL = dailyAvgL,
                ),
                cartridge(
                    id = "kitchen-3",
                    stage = 3,
                    name = "RO 멤브레인",
                    ratedCapacityL = 3_600.0,
                    remainingDays = 180,
                    totalCumulativeL = totalCumulativeL,
                    dailyAvgL = dailyAvgL,
                ),
                cartridge(
                    id = "kitchen-4",
                    stage = 4,
                    name = "포스트 실버 항균",
                    ratedCapacityL = 4_000.0,
                    remainingDays = 320,
                    totalCumulativeL = totalCumulativeL,
                    dailyAvgL = dailyAvgL,
                ),
            ),
            visitTicket = VisitTicket(
                scheduledDate = today.plus(2, DateTimeUnit.DAY),
                hour = 14,
                minute = 0,
                technicianMaskedName = "김*수",
                targetFilterIds = listOf("kitchen-1", "kitchen-2"),
            ),
            replacementHistory = listOf(
                FilterReplacementRecord(
                    replacedOn = LocalDate(2026, 8, 12),
                    filterIds = listOf("kitchen-1"),
                    source = ReplacementSource.SELF,
                    cumulativeL = 4_320.0,
                ),
                FilterReplacementRecord(
                    replacedOn = LocalDate(2026, 3, 15),
                    filterIds = listOf("kitchen-1", "kitchen-2"),
                    source = ReplacementSource.PARTNER,
                    technicianMaskedName = "김*수",
                    cumulativeL = 3_120.0,
                ),
                FilterReplacementRecord(
                    replacedOn = LocalDate(2025, 11, 2),
                    filterIds = listOf("kitchen-4"),
                    source = ReplacementSource.SELF,
                    cumulativeL = 1_940.0,
                ),
            ),
        )
    }

    fun officePurifier(today: LocalDate, now: Instant): PurifierDevice {
        val totalCumulativeL = 2_100.0
        val recentDailyUsageL = listOf(
            2.8, 3.1, 3.4, 5.8, 6.2, 3.0, 2.9, 3.3, 5.5, 6.0, 3.2, 3.6, 5.0, 5.0,
        )
        val dailyAvgL = FilterLifeCalculator.dailyAverage(recentDailyUsageL)
        return PurifierDevice(
            id = OFFICE_ID,
            nickname = "사무실 직수 정수기",
            installedOn = today.minus(18, DateTimeUnit.DAY),
            householdSize = 2,
            totalCumulativeL = totalCumulativeL,
            dailyAvgL = dailyAvgL,
            recentDailyUsageL = recentDailyUsageL,
            lastTelemetryAt = now - 30.hours,
            filters = listOf(
                FilterCartridge(
                    id = "office-1",
                    stage = 1,
                    name = "세디먼트 카본",
                    ratedCapacityL = 1_500.0,
                    baselineL = totalCumulativeL - 1_545.0,
                ),
                cartridge(
                    id = "office-2",
                    stage = 2,
                    name = "카본 블록",
                    ratedCapacityL = 1_800.0,
                    remainingDays = 90,
                    totalCumulativeL = totalCumulativeL,
                    dailyAvgL = dailyAvgL,
                ),
            ),
        )
    }

    private fun cartridge(
        id: String,
        stage: Int,
        name: String,
        ratedCapacityL: Double,
        remainingDays: Int,
        totalCumulativeL: Double,
        dailyAvgL: Double,
    ): FilterCartridge {
        val remainingL = remainingDays * dailyAvgL
        val usageL = ratedCapacityL - remainingL
        return FilterCartridge(
            id = id,
            stage = stage,
            name = name,
            ratedCapacityL = ratedCapacityL,
            baselineL = totalCumulativeL - usageL,
        )
    }
}
