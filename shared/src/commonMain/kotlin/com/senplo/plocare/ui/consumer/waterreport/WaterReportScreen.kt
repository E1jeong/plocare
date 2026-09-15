package com.senplo.plocare.ui.consumer.waterreport

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import com.senplo.plocare.domain.filter.PurifierDevice
import com.senplo.plocare.domain.filter.buildWaterReportChart
import com.senplo.plocare.ui.consumer.ConsumerCard
import com.senplo.plocare.ui.consumer.ConsumerScreenHeader
import com.senplo.plocare.ui.consumer.ConsumerSectionTitle
import com.senplo.plocare.ui.theme.PloCareColor
import kotlin.math.floor
import kotlin.math.round

private fun groupedInt(value: Int): String {
    val sign = if (value < 0) "-" else ""
    val digits = kotlin.math.abs(value).toString()
    return sign + digits.reversed().chunked(3).joinToString(",").reversed()
}

private fun formatOneDecimal(value: Double): String {
    val tenths = round(value * 10.0).toInt()
    return if (tenths % 10 == 0) (tenths / 10).toString() else "${tenths / 10}.${tenths % 10}"
}

@Composable
fun WaterReportScreen(device: PurifierDevice) {
    val lifetimeLiters = round(device.totalCumulativeL).toInt()
    val savedBottles = floor(lifetimeLiters / 2.0).toInt()
    val chart = buildWaterReportChart(device)
    val averageLabel = "14일 평균 ${formatOneDecimal(chart.averageL)} L"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        ConsumerScreenHeader(
            title = "물 사용 리포트",
            subtitle = "${device.nickname}의 사용 습관과 정수 생활의 가치를 확인하세요.",
        )
        Spacer(Modifier.height(18.dp))
        ConsumerCard(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Metric("오늘", "${formatOneDecimal(chart.todayL)} L", Modifier.weight(1f), highlight = true)
                Metric(
                    if (chart.periodDays == 0) "기간 누적" else "${chart.periodDays}일 누적",
                    "${groupedInt(round(chart.periodSumL).toInt())} L",
                    Modifier.weight(1f),
                )
                Metric("전체 누적", "${groupedInt(lifetimeLiters)} L", Modifier.weight(1f))
            }
        }

        Spacer(Modifier.height(22.dp))
        ConsumerSectionTitle(title = "최근 14일 사용량", caption = "$averageLabel/일")
        Spacer(Modifier.height(10.dp))
        ConsumptionChart(dailyUsage = chart.dailyUsageL, average = chart.averageL, averageLabel = averageLabel)
        Spacer(Modifier.height(8.dp))
        Text(
            "실제 14일 물 사용량을 기준으로 필터 교체 D-Day를 계산해요.",
            color = PloCareColor.TextTertiary,
            fontSize = 11.sp,
        )

        Spacer(Modifier.height(22.dp))
        ConsumerSectionTitle(title = "환경 절감 효과")
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ImpactCard("♻", "2 L 생수병", "${savedBottles}개", Modifier.weight(1f))
            ImpactCard("🌿", "플라스틱 절감", "약 60.3 kg", Modifier.weight(1f))
        }
        Spacer(Modifier.height(10.dp))
        ConsumerCard(modifier = Modifier.fillMaxWidth()) {
            Text("탄소 배출 절감 추정", color = PloCareColor.TextSecondary, fontSize = 11.sp)
            Spacer(Modifier.height(4.dp))
            Text("정수 생활로 만든 작은 변화가 쌓이고 있어요", color = PloCareColor.TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }

        Spacer(Modifier.height(22.dp))
        ConsumerSectionTitle(title = "필터 교체 이력")
        Spacer(Modifier.height(10.dp))
        ConsumerCard(modifier = Modifier.fillMaxWidth()) {
            HistoryItem("2026.08.12", "1단 세디먼트 카본", "직접 교체 · 누적 4,320 L", active = true)
            HistoryItem("2026.03.15", "1단 세디먼트 · 2단 프리카본", "PloCare 파트너 김*수 · 누적 3,120 L")
            HistoryItem("2025.11.02", "4단 포스트 실버 항균", "직접 교체 · 누적 1,940 L", last = true)
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun Metric(label: String, value: String, modifier: Modifier, highlight: Boolean = false) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = PloCareColor.TextSecondary, fontSize = 10.sp)
        Spacer(Modifier.height(5.dp))
        Text(
            value,
            color = if (highlight) PloCareColor.AquaTeal else PloCareColor.TextPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun ConsumptionChart(
    dailyUsage: List<Double>,
    average: Double,
    averageLabel: String,
) {
    val pastBar = PloCareColor.SurfaceBorder
    val todayBar = PloCareColor.AquaTeal
    val averageLine = PloCareColor.VividCyan
    val oldestLabel = if (dailyUsage.isEmpty()) "D-14" else "D-${dailyUsage.lastIndex}"
    ConsumerCard(modifier = Modifier.fillMaxWidth()) {
        Canvas(modifier = Modifier.fillMaxWidth().height(150.dp)) {
            val maxLiters = 10f
            if (dailyUsage.isNotEmpty()) {
                val gap = 5.dp.toPx()
                val barWidth = (size.width - gap * (dailyUsage.size - 1)) / dailyUsage.size
                dailyUsage.forEachIndexed { index, liters ->
                    val height = size.height * (liters.toFloat() / maxLiters).coerceIn(0f, 1f)
                    drawRoundRect(
                        color = if (index == dailyUsage.lastIndex) todayBar else pastBar,
                        topLeft = Offset(index * (barWidth + gap), size.height - height),
                        size = Size(barWidth, height),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx()),
                    )
                }
            }
            val averageY = size.height - size.height * (average.toFloat() / maxLiters).coerceIn(0f, 1f)
            drawLine(
                color = averageLine,
                start = Offset(0f, averageY),
                end = Offset(size.width, averageY),
                strokeWidth = 1.5.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 7f)),
            )
        }
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(oldestLabel, color = PloCareColor.TextTertiary, fontSize = 10.sp)
            Text(averageLabel, color = PloCareColor.VividCyan, fontSize = 10.sp)
            Text("오늘", color = PloCareColor.AquaTeal, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ImpactCard(emoji: String, label: String, value: String, modifier: Modifier) {
    ConsumerCard(modifier = modifier.height(126.dp)) {
        Text(emoji, fontSize = 22.sp)
        Spacer(Modifier.weight(1f))
        Text(label, color = PloCareColor.TextSecondary, fontSize = 11.sp)
        Text(value, color = PloCareColor.TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun HistoryItem(
    date: String,
    title: String,
    detail: String,
    active: Boolean = false,
    last: Boolean = false,
) {
    Row {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                Modifier
                    .size(9.dp)
                    .background(if (active) PloCareColor.AquaTeal else PloCareColor.SurfaceBorder, CircleShape),
            )
            if (!last) {
                Box(Modifier.size(width = 1.dp, height = 58.dp).background(PloCareColor.SurfaceBorder))
            }
        }
        Column(Modifier.padding(start = 12.dp)) {
            Text(date, color = PloCareColor.TextTertiary, fontSize = 10.sp)
            Text(title, color = PloCareColor.TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text(detail, color = PloCareColor.TextSecondary, fontSize = 11.sp)
        }
    }
}
