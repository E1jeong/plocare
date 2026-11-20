package com.senplo.plocare.ui.consumer.waterreport

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.senplo.plocare.domain.filter.DashboardFixtures
import com.senplo.plocare.domain.filter.DashboardSnapshot
import com.senplo.plocare.domain.filter.PricingPolicy
import com.senplo.plocare.domain.filter.SamplePricingPolicy
import com.senplo.plocare.domain.filter.SavingsEstimate
import com.senplo.plocare.domain.filter.SavingsReportMode
import com.senplo.plocare.domain.filter.WaterReportHistoryItem
import com.senplo.plocare.domain.filter.buildWaterReportChart
import com.senplo.plocare.domain.filter.buildWaterReportHistory
import com.senplo.plocare.ui.consumer.dashboard.HeaderChip
import com.senplo.plocare.ui.theme.PloCareColor
import com.senplo.plocare.ui.theme.PloCareTheme

// Figma 05 물 사용 리포트: profit with history (79:3806), loss before break-even without history (94:4652)
@Composable
fun WaterReportScreen(
    snapshot: DashboardSnapshot,
    // Fixed Figma sample amounts until approved prices exist (BP-01; 2026-11-20 user decision).
    savings: SavingsEstimate = DashboardFixtures.sampleSavings(snapshot.device.id),
    pricing: PricingPolicy = SamplePricingPolicy(),
) {
    val colors = PloCareColor
    val typography = PloCareTheme.typography
    val device = snapshot.device
    val chart = buildWaterReportChart(device)
    val history = buildWaterReportHistory(device)

    Column(modifier = Modifier.fillMaxSize()) {
        // Header (79:3809)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "물 사용 리포트",
                style = typography.titleLarge,
                color = colors.TextMain,
                modifier = Modifier.semantics { heading() },
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(top = 20.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp),
        ) {
            // 79:3816
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = device.nickname,
                        style = typography.titleMid,
                        color = colors.TextMain,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false).semantics { heading() },
                    )
                    HeaderChip(text = installedForLabel(device.installedOn, snapshot.today), color = colors.Primary)
                }
                // 79:3819
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.White, RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 20.dp),
                ) {
                    SummaryStat("오늘", litersLabel(chart.todayL), highlight = true, modifier = Modifier.weight(1f))
                    SummaryStat("14일 평균", litersLabel(chart.averageL), modifier = Modifier.weight(1f))
                    SummaryStat("설치 후 누적", lifetimeLitersLabel(device.totalCumulativeL), modifier = Modifier.weight(1f))
                }
            }

            // 94:4651: the chart and the savings card sit 12dp apart.
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    SectionTitle("최근 14일 사용량")
                    UsageChart(
                        dailyUsageL = chart.dailyUsageL,
                        description = "최근 14일 사용량, 평균 ${litersLabel(chart.averageL)}, 오늘 ${litersLabel(chart.todayL)}",
                    )
                }
                SavingsCard(
                    estimate = savings,
                    note = savingsNote(savings, pricing),
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }

            // 94:4555
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                SectionTitle("교체 이력")
                HistoryCard(history)
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = PloCareTheme.typography.titleMid,
        color = PloCareColor.TextMain,
        modifier = Modifier.semantics { heading() },
    )
}

@Composable
private fun SummaryStat(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    highlight: Boolean = false,
) {
    val colors = PloCareColor
    Column(
        modifier = modifier.semantics(mergeDescendants = true) {},
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = label,
            style = PloCareTheme.typography.labelMidMedium,
            color = if (highlight) colors.GradientMain else colors.Black50Muted,
        )
        Text(
            text = value,
            // Figma unnamed style: Bold 20 / 29, -0.02em
            style = PloCareTheme.typography.titleMid.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                lineHeight = 29.sp,
                letterSpacing = (-0.02).em,
            ),
            color = if (highlight) colors.Primary else colors.TextMain,
        )
    }
}

private const val CHART_DAYS = 14

// Chart (79:3861): 14 plain bars, tallest 54dp, no axis, labels, or highlight.
@Composable
private fun UsageChart(dailyUsageL: List<Double>, description: String) {
    val colors = PloCareColor
    val maxL = dailyUsageL.maxOrNull()?.takeIf { it > 0.0 } ?: 1.0
    // Fewer than 14 days leaves the oldest slots empty so today stays at the right edge.
    val slots = List(CHART_DAYS - dailyUsageL.size.coerceAtMost(CHART_DAYS)) { 0.0 } + dailyUsageL
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.White, RoundedCornerShape(12.dp))
            .padding(horizontal = 18.dp, vertical = 16.dp)
            .height(54.dp)
            .clearAndSetSemantics { contentDescription = description },
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        slots.forEach { liters ->
            val fraction = (liters / maxL).toFloat().coerceIn(0f, 1f)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(fraction)
                    .background(colors.Black10),
            )
        }
    }
}

// Note-box fill of the profit state (94:4548); Figma uses it only here and it is outside the palette.
private val ProfitNoteFill = Color(0xFFE2F8FF)

// Savings card (79:3873 / 94:4697)
@Composable
private fun SavingsCard(
    estimate: SavingsEstimate,
    note: String?,
    modifier: Modifier = Modifier,
) {
    val colors = PloCareColor
    val typography = PloCareTheme.typography
    val profit = estimate.mode == SavingsReportMode.PROFIT
    val shape = RoundedCornerShape(12.dp)
    val accent = if (profit) colors.GradientMain else colors.TextMain
    val totalLabelColor = if (profit) colors.GradientMain else colors.Black50Muted

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(if (profit) colors.PrimaryContainerLight else colors.White, shape)
            .then(if (profit) Modifier.border(1.dp, colors.GradientSecondary, shape) else Modifier)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SectionTitle("플로케어로 아낀 금액")
            Text(
                text = "예시 금액 • 실제 금액은 필터 가격 확인 후 확정",
                style = typography.labelMidMedium,
                color = colors.Black50Muted,
            )
        }
        Text(
            text = signedWonLabel(estimate.netSavingsKrw),
            // Figma unnamed style: Bold 32 / 1.4em, -0.03em
            style = typography.headLarge.copy(
                fontWeight = FontWeight.Bold,
                lineHeight = 1.4.em,
                letterSpacing = (-0.03).em,
            ),
            color = accent,
        )
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            CostRow("기존 정기교체방식", wonLabel(estimate.scheduledReplacementCostKrw))
            CostRow("플로케어 (모듈 • 구독 포함)", wonLabel(estimate.ploCareTotalCostKrw))
        }
        HorizontalDivider(thickness = 1.dp, color = colors.Black10)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = "절약한 금액", style = typography.labelMidMedium, color = totalLabelColor)
            Text(
                text = signedWonLabel(estimate.netSavingsKrw),
                style = typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = accent,
            )
        }
        if (note != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (profit) ProfitNoteFill else colors.Black0Surface, RoundedCornerShape(8.dp))
                    .padding(12.dp),
            ) {
                Text(
                    text = note,
                    style = typography.labelMidMedium,
                    color = if (profit) colors.GradientMain else colors.Black50Muted,
                )
            }
        }
    }
}

@Composable
private fun CostRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = label, style = PloCareTheme.typography.labelMidMedium, color = PloCareColor.Black50Muted)
        Text(text = value, style = PloCareTheme.typography.bodyLarge, color = PloCareColor.TextMain)
    }
}

// History card (94:4584); empty state (94:4718) keeps the 124dp card.
@Composable
private fun HistoryCard(history: List<WaterReportHistoryItem>) {
    val colors = PloCareColor
    val typography = PloCareTheme.typography
    val shape = RoundedCornerShape(12.dp)
    if (history.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(124.dp)
                .background(colors.White, shape)
                .padding(20.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = "아직 교체 기록이 없어요", style = typography.labelMidMedium, color = colors.Black50Muted)
        }
        return
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.White, shape)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        history.forEach { item ->
            Row(
                modifier = Modifier.semantics(mergeDescendants = true) {},
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = item.dateLabel, style = typography.labelMidMedium, color = colors.Black50Muted)
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(colors.filterTypeColor(item.filterType), RoundedCornerShape(4.dp)),
                    )
                    Text(
                        text = item.title,
                        style = typography.labelMidMedium,
                        color = colors.TextMain,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(text = item.sourceLabel, style = typography.labelMidMedium, color = colors.Primary)
            }
        }
    }
}
