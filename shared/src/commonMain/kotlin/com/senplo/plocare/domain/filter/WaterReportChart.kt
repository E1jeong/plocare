package com.senplo.plocare.domain.filter

import kotlinx.datetime.LocalDate
import kotlin.math.floor
import kotlin.math.round

data class WaterReportChart(
    val dailyUsageL: List<Double>,
    val averageL: Double,
    val todayL: Double,
    val periodSumL: Double,
    val periodDays: Int,
)

data class WaterReportEco(
    val bottlesSaved: Int,
    val plasticKg: Double,
    val carbonDetail: String,
)

data class WaterReportHistoryItem(
    val dateLabel: String,
    val title: String,
    val detail: String,
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

fun buildWaterReportEco(device: PurifierDevice): WaterReportEco {
    val plasticKg = FilterLifeCalculator.plasticSavedKg(device.totalCumulativeL)
    return WaterReportEco(
        bottlesSaved = FilterLifeCalculator.bottlesSaved(device.totalCumulativeL),
        plasticKg = plasticKg,
        carbonDetail = waterReportCarbonDetail(plasticKg),
    )
}

fun buildWaterReportHistory(device: PurifierDevice): List<WaterReportHistoryItem> =
    device.replacementHistory.map { record ->
        val names = record.filterIds.mapNotNull { id ->
            device.filters.find { it.id == id }?.let { filter -> "${filter.stage}단 ${filter.name}" }
        }
        val actor = when (record.source) {
            ReplacementSource.SELF -> "직접 교체"
            ReplacementSource.PARTNER ->
                record.technicianMaskedName?.let { "PloCare 파트너 $it" } ?: "PloCare 파트너"
        }
        WaterReportHistoryItem(
            dateLabel = formatWaterReportDate(record.replacedOn),
            title = names.joinToString(" · ").ifEmpty { "필터 교체" },
            detail = "$actor · 누적 ${formatWaterReportLiters(record.cumulativeL)}",
        )
    }

fun waterReportCarbonDetail(plasticKg: Double): String =
    "플라스틱 약 ${formatWaterReportDecimal(plasticKg)} kg 절감 기준 로컬 추정입니다. 탄소 kg은 서버에서 계산하지 않습니다."

fun waterReportHistoryBoundary(): String =
    "이 기기의 로컬 이력입니다. 서버에서 불러오지 않았습니다."

fun waterReportHistoryEmptyCopy(): String =
    "로컬 이력이 없습니다. 서버에서 불러오지 않았습니다."

fun formatWaterReportDecimal(value: Double): String {
    val tenths = if (value >= 0.0) {
        floor(value * 10.0 + 0.5).toInt()
    } else {
        -floor(-value * 10.0 + 0.5).toInt()
    }
    return if (tenths % 10 == 0) (tenths / 10).toString() else "${tenths / 10}.${kotlin.math.abs(tenths) % 10}"
}

internal fun formatWaterReportDate(date: LocalDate): String {
    val month = (date.month.ordinal + 1).toString().padStart(2, '0')
    val day = date.day.toString().padStart(2, '0')
    return "${date.year}.$month.$day"
}

internal fun formatWaterReportLiters(value: Double): String {
    val amount = round(value).toInt()
    val sign = if (amount < 0) "-" else ""
    val digits = kotlin.math.abs(amount).toString()
    val grouped = digits.reversed().chunked(3).joinToString(",").reversed()
    return "$sign$grouped L"
}
