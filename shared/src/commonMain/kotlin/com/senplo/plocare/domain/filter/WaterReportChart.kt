package com.senplo.plocare.domain.filter

data class WaterReportChart(
    val dailyUsageL: List<Double>,
    val averageL: Double,
    val todayL: Double,
    val periodSumL: Double,
    val periodDays: Int,
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
