package com.senplo.plocare.ui.partner

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.senplo.plocare.ui.theme.PloCareColor
import com.senplo.plocare.ui.theme.PloCareTheme
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.painterResource
import plocare.shared.generated.resources.Res
import plocare.shared.generated.resources.ic_distance
import plocare.shared.generated.resources.ic_error
import plocare.shared.generated.resources.ic_schedule

// Sample partner profile until partner accounts exist (NV-07).
private const val PARTNER_SAMPLE_NAME = "김파트너"
private const val PARTNER_SAMPLE_REGION = "서울 성동구/광진구"

// Figma 16 파트너 업무 홈 (129:2285)
@Composable
internal fun WorkDashboardTab(
    visitViews: List<PartnerVisitView>,
    stock: List<StockItem>,
    today: LocalDate,
    onReorderVisits: () -> Unit,
) {
    val summary = workSummary(visitViews)
    val required = inventoryRequirements(visitViews)
    val stockItems = vehicleStockItems(required, stockByType(stock))
    val openCount = visitViews.count { !it.completed }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PloCareColor.White)
            .verticalScroll(rememberScrollState()),
    ) {
        WorkHeader()
        SummarySection(summary, workDurationLabel(required.values.sum() * MINUTES_PER_FILTER))
        AssignmentSection(assignmentRows(visitViews), today, onReorderVisits)
        VehicleStockSection(stockItems, openCount)
    }
}

@Composable
private fun WorkHeader() {
    val colors = PloCareColor
    val typography = PloCareTheme.typography
    // Header (129:2288)
    Column(
        modifier = Modifier.padding(start = 16.dp, top = 24.dp, end = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = buildAnnotatedString {
                append("${PARTNER_SAMPLE_NAME}님 ")
                withStyle(typography.titleMid.toSpanStyle()) { append("좋은 아침이에요.") }
            },
            style = typography.titleLarge,
            color = colors.TextMain,
            modifier = Modifier.semantics { heading() },
        )
        // Region box (129:2431)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.Black0Surface, RoundedCornerShape(8.dp))
                .padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_distance),
                contentDescription = null,
                tint = colors.Primary,
                modifier = Modifier.size(18.dp),
            )
            Text(text = "담당지역", style = typography.labelMidMedium, color = colors.Primary)
            Text(
                text = PARTNER_SAMPLE_REGION,
                style = typography.labelMidMedium,
                color = colors.Primary,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun SummarySection(summary: WorkSummary, durationLabel: String) {
    val colors = PloCareColor
    val typography = PloCareTheme.typography
    // 오늘의 업무 요약 (129:2328)
    Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        PartnerSectionHeading("오늘의 업무 요약")
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.Black0Surface, RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp, vertical = 16.dp),
            ) {
                SummaryMetric("예정", summary.scheduled, Modifier.weight(1f))
                SummaryMetric("긴급", summary.urgent, Modifier.weight(1f))
                SummaryMetric("완료", summary.done, Modifier.weight(1f))
            }
            // 예상 작업 시간 (129:2473)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.PrimaryTint.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_schedule),
                    contentDescription = null,
                    tint = colors.Primary,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = "예상 작업 시간",
                    style = typography.labelMidMedium,
                    color = colors.Primary,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = "$durationLabel • 이동 시간 미연동",
                    style = typography.labelMidMedium,
                    color = colors.Primary,
                )
            }
        }
    }
}

@Composable
private fun SummaryMetric(label: String, count: Int, modifier: Modifier = Modifier) {
    val typography = PloCareTheme.typography
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = typography.labelMidMedium, color = PloCareColor.Black50Muted)
        Text(
            text = "${count}건",
            // Figma value: Bold 700, 20/29, -0.02em
            style = typography.titleLarge.copy(fontWeight = FontWeight.Bold, fontSize = 20.sp, letterSpacing = (-0.4).sp),
            color = PloCareColor.TextMain,
        )
    }
}

@Composable
private fun AssignmentSection(rows: List<AssignmentRow>, today: LocalDate, onReorderVisits: () -> Unit) {
    val colors = PloCareColor
    val typography = PloCareTheme.typography
    // 센터 배정표 (129:2491). The 48dp `순서 바꾸기` touch target adds ~13dp above and below the title row,
    // which stands in for Figma's 12dp top padding and 12dp gap.
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PartnerSectionHeading("센터 배정표")
                Box(
                    modifier = Modifier
                        .background(colors.Primary.copy(alpha = 0.1f), RoundedCornerShape(100.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                ) {
                    Text(text = "${rows.size}건", style = typography.labelSmall, color = colors.Primary)
                }
            }
            ReorderLink(onClick = onReorderVisits)
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = assignmentDateLabel(today),
                style = typography.labelMidMedium,
                color = colors.TextMain,
                modifier = Modifier.padding(horizontal = 8.dp),
            )
            Column {
                rows.forEach { AssignmentRowItem(it) }
            }
        }
    }
}

@Composable
private fun AssignmentRowItem(row: AssignmentRow) {
    val colors = PloCareColor
    val typography = PloCareTheme.typography
    val done = row.badge == VisitBadgeKind.DONE
    val accent = if (done) colors.Black40Ghost else colors.Primary
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (row.current) Modifier.background(colors.Black0Surface, RoundedCornerShape(8.dp)) else Modifier,
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = row.order.toString(), style = typography.labelMidMedium, color = accent)
        Text(text = row.view.time, style = typography.labelMidMedium, color = accent)
        Text(
            text = row.view.visit.maskedName,
            style = typography.labelMidMedium,
            color = if (done) colors.Black40Ghost else colors.TextMain,
        )
        Text(
            text = row.district,
            style = typography.labelMidMedium,
            color = if (done) colors.Black40Ghost else colors.Black50Muted,
            modifier = Modifier.weight(1f),
        )
        VisitStatusBadge(row.badge)
    }
}

@Composable
private fun VehicleStockSection(items: List<VehicleStockItem>, openCount: Int) {
    val colors = PloCareColor
    val typography = PloCareTheme.typography
    val shortageMessage = stockShortageMessage(items)
    // 차량 준비 재고 (129:2595)
    Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PartnerSectionHeading("차량 준비 재고")
            Text(text = "남은 방문 ${openCount}건 기준", style = caption13(FontWeight.Medium), color = colors.Primary)
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            if (shortageMessage != null) {
                // Shortage warning (146:2676)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.StatusAlert.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_error),
                        contentDescription = null,
                        tint = colors.StatusAlert,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        text = shortageMessage,
                        style = typography.labelMidMedium,
                        color = colors.StatusAlert,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            items.chunked(2).forEach { pair ->
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    pair.forEach { StockCell(it, Modifier.weight(1f)) }
                }
            }
        }
    }
}

@Composable
private fun StockCell(item: VehicleStockItem, modifier: Modifier = Modifier) {
    val colors = PloCareColor
    val typography = PloCareTheme.typography
    val shape = RoundedCornerShape(8.dp)
    Row(
        modifier = modifier
            .background(colors.White, shape)
            .border(1.dp, colors.Black10, shape)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(colors.filterTypeColor(item.type), RoundedCornerShape(4.dp)),
        )
        Text(text = item.type.label, style = typography.labelMidMedium, color = colors.TextMain)
        Text(
            text = "${item.loaded}/${item.required}개",
            style = typography.labelMidMedium,
            color = if (item.shortage > 0) colors.StatusAlert else colors.TextMain,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f),
        )
    }
}
