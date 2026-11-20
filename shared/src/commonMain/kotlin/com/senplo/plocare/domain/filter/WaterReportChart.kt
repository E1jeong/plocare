package com.senplo.plocare.domain.filter

import kotlinx.datetime.LocalDate
import kotlin.math.floor

data class WaterReportChart(
    val dailyUsageL: List<Double>,
    val averageL: Double,
    val todayL: Double,
    val periodSumL: Double,
    val periodDays: Int,
)

/** Figma 05 history row (94:4602): `10.12`, a filter color square, the filters, and who replaced them. */
data class WaterReportHistoryItem(
    val dateLabel: String,
    val title: String,
    val filterType: FilterType,
    val sourceLabel: String,
)

fun buildWaterReportChart(device: PurifierDevice): WaterReportChart {
    val window = device.recentDailyUsageL.takeLast(14)
    return WaterReportChart(
        dailyUsageL = window,
        averageL = FilterLifeCalculator.dailyAverage(window),
        todayL = window.lastOrNull() ?: 0.0,
        periodSumL = window.sum(),
        periodDays = window.size,
    )
}

fun buildWaterReportHistory(device: PurifierDevice): List<WaterReportHistoryItem> =
    device.replacementHistory.map { record ->
        val filters = record.filterIds.mapNotNull { id -> device.filters.find { it.id == id } }
        val title = when (filters.size) {
            0 -> "필터 교체"
            1 -> filters.single().let { "${it.stage}단 ${it.name}" }
            else -> "${filters.joinToString("•") { it.stage.toString() }}단 묶음 교체"
        }
        WaterReportHistoryItem(
            dateLabel = formatWaterReportDate(record.replacedOn),
            title = title,
            filterType = filters.firstOrNull()?.filterType ?: FilterType.UNSPECIFIED,
            sourceLabel = when (record.source) {
                ReplacementSource.SELF -> "직접 교체"
                ReplacementSource.PARTNER -> "방문 케어"
            },
        )
    }

fun formatWaterReportDecimal(value: Double): String {
    val tenths = if (value >= 0.0) {
        floor(value * 10.0 + 0.5).toInt()
    } else {
        -floor(-value * 10.0 + 0.5).toInt()
    }
    return if (tenths % 10 == 0) (tenths / 10).toString() else "${tenths / 10}.${kotlin.math.abs(tenths) % 10}"
}

/** Figma `10.12`: month without padding, two-digit day. */
internal fun formatWaterReportDate(date: LocalDate): String {
    val day = date.day.toString().padStart(2, '0')
    return "${date.month.ordinal + 1}.$day"
}
