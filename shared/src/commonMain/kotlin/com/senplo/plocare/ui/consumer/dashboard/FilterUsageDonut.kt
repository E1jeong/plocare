package com.senplo.plocare.ui.consumer.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.senplo.plocare.domain.filter.FilterSnapshot
import com.senplo.plocare.ui.theme.PloCareColor
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private val DonutSize = 220.dp
private val DonutStroke = 32.dp
private val CalloutLeader = 16.dp
private val CalloutElbow = 8.dp
private val CalloutDotRadius = 3.5.dp
private const val SliceGapDegrees = 4f
private const val TrackAlpha = 0.28f

@Composable
fun FilterUsageDonut(
    filters: List<FilterSnapshot>,
    onSelfReplace: (FilterSnapshot) -> Unit,
    onRequestReplacement: (FilterSnapshot) -> Unit,
    modifier: Modifier = Modifier,
) {
    val sliceColors = filters.map { PloCareColor.filterStageColor(it.stage) }
    val remainingFills = filters.map { it.fillFraction }
    val stages = filters.map { it.stage }
    val cardColor = PloCareColor.SurfaceCard
    val textMeasurer = rememberTextMeasurer()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(PloCareColor.SurfaceCard)
            .border(1.dp, PloCareColor.SurfaceBorder, RoundedCornerShape(20.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(DonutSize),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val count = filters.size
                if (count == 0) return@Canvas
                val stroke = DonutStroke.toPx()
                val diameter = DonutSize.toPx() - stroke
                val center = Offset(size.width / 2f, size.height / 2f)
                val topLeft = Offset(center.x - diameter / 2f, center.y - diameter / 2f)
                val arcSize = Size(diameter, diameter)
                val gap = donutSliceGapDegrees(count)
                val sweep = donutSliceSweepDegrees(count)
                val strokeStyle = Stroke(width = stroke, cap = StrokeCap.Butt)
                var start = -90f + gap / 2f
                sliceColors.forEachIndexed { index, color ->
                    drawArc(
                        color = color.copy(alpha = TrackAlpha),
                        startAngle = start,
                        sweepAngle = sweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = strokeStyle,
                    )
                    val remainingSweep = donutRemainingSweep(sweep, remainingFills[index])
                    if (remainingSweep > 0f) {
                        drawArc(
                            color = color,
                            startAngle = start,
                            sweepAngle = remainingSweep,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = strokeStyle,
                        )
                    }
                    start += sweep + gap
                }
                drawStageCallouts(
                    center = center,
                    ringRadius = diameter / 2f,
                    outerRadius = diameter / 2f + stroke / 2f,
                    stages = stages,
                    colors = sliceColors,
                    sliceStart = -90f + gap / 2f,
                    sliceSweep = sweep,
                    sliceGap = gap,
                    cardColor = cardColor,
                    textMeasurer = textMeasurer,
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "다음 교체",
                    color = PloCareColor.TextTertiary,
                    fontSize = 12.sp,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = donutCenterValue(filters),
                    color = PloCareColor.TextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
        if (filters.isNotEmpty()) {
            Spacer(modifier = Modifier.height(20.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                filters.forEachIndexed { index, filter ->
                    FilterLegendRow(
                        filter = filter,
                        color = sliceColors[index],
                        onSelfReplace = { onSelfReplace(filter) },
                        onRequestReplacement = { onRequestReplacement(filter) },
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterLegendRow(
    filter: FilterSnapshot,
    color: Color,
    onSelfReplace: () -> Unit,
    onRequestReplacement: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(color),
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "${filter.stage}단계 ${filter.name}",
                color = PloCareColor.TextPrimary,
                fontSize = 13.sp,
                lineHeight = 13.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = TightLegendTextStyle,
                modifier = Modifier.weight(1f),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = remainingOverRated(filter),
                color = PloCareColor.TextPrimary,
                fontSize = 13.sp,
                lineHeight = 13.sp,
                fontWeight = FontWeight.SemiBold,
                style = TightLegendTextStyle,
            )
        }
        Row(
            modifier = Modifier.padding(start = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "자가 교체",
                color = PloCareColor.TextSecondary,
                fontSize = 11.sp,
                lineHeight = 11.sp,
                style = TightLegendTextStyle,
                modifier = Modifier.clickable(onClick = onSelfReplace),
            )
            if (filter.showReplacementRequest) {
                Text(
                    text = "교체 신청",
                    color = color,
                    fontSize = 11.sp,
                    lineHeight = 11.sp,
                    fontWeight = FontWeight.Bold,
                    style = TightLegendTextStyle,
                    modifier = Modifier.clickable(onClick = onRequestReplacement),
                )
            }
        }
    }
}

private val TightLegendTextStyle = TextStyle(
    lineHeightStyle = LineHeightStyle(
        alignment = LineHeightStyle.Alignment.Center,
        trim = LineHeightStyle.Trim.Both,
    ),
)

internal fun donutSliceValues(count: Int): List<Float> = List(count) { 1f }

internal fun donutSliceGapDegrees(count: Int): Float =
    if (count <= 1) 0f else SliceGapDegrees

internal fun donutSliceSweepDegrees(count: Int): Float {
    if (count <= 0) return 0f
    return 360f / count - donutSliceGapDegrees(count)
}

internal fun donutRemainingSweep(sliceSweep: Float, fillFraction: Float): Float =
    sliceSweep * fillFraction.coerceIn(0f, 1f)

internal fun donutCenterValue(filters: List<FilterSnapshot>): String {
    val representative = filters.minByOrNull { it.remainingDays } ?: return "—"
    return dDayLabel(representative.remainingDays)
}

internal fun donutCalloutLabel(stage: Int): String = "${stage}단계"

internal fun donutSliceMidAngle(sliceStart: Float, sliceSweep: Float): Float =
    sliceStart + sliceSweep / 2f

internal data class DonutCalloutLayout(
    val dot: Offset,
    val lineStart: Offset,
    val elbow: Offset,
    val lineEnd: Offset,
    val rightSide: Boolean,
)

internal fun donutCalloutLayout(
    center: Offset,
    ringRadius: Float,
    outerRadius: Float,
    leaderLength: Float,
    elbowLength: Float,
    angleDegrees: Float,
): DonutCalloutLayout {
    val rightSide = cos(angleDegrees * DegToRad) >= 0f
    val dot = polarOffset(center, ringRadius, angleDegrees)
    val elbow = polarOffset(center, outerRadius + leaderLength, angleDegrees)
    return DonutCalloutLayout(
        dot = dot,
        lineStart = dot,
        elbow = elbow,
        lineEnd = Offset(
            x = elbow.x + if (rightSide) elbowLength else -elbowLength,
            y = elbow.y,
        ),
        rightSide = rightSide,
    )
}

private const val DegToRad = (PI / 180.0).toFloat()

private fun polarOffset(center: Offset, radius: Float, angleDegrees: Float): Offset {
    val radians = angleDegrees * DegToRad
    return Offset(
        x = center.x + radius * cos(radians),
        y = center.y + radius * sin(radians),
    )
}

private fun DrawScope.drawStageCallouts(
    center: Offset,
    ringRadius: Float,
    outerRadius: Float,
    stages: List<Int>,
    colors: List<Color>,
    sliceStart: Float,
    sliceSweep: Float,
    sliceGap: Float,
    cardColor: Color,
    textMeasurer: TextMeasurer,
) {
    val leader = CalloutLeader.toPx()
    val elbowLength = CalloutElbow.toPx()
    val dotRadius = CalloutDotRadius.toPx()
    val labelGap = 6.dp.toPx()
    val lineWidth = 1.2.dp.toPx()
    var start = sliceStart
    stages.forEachIndexed { index, stage ->
        val color = colors[index]
        val layout = donutCalloutLayout(
            center = center,
            ringRadius = ringRadius,
            outerRadius = outerRadius,
            leaderLength = leader,
            elbowLength = elbowLength,
            angleDegrees = donutSliceMidAngle(start, sliceSweep),
        )
        drawLine(color, layout.lineStart, layout.elbow, strokeWidth = lineWidth, cap = StrokeCap.Round)
        drawLine(color, layout.elbow, layout.lineEnd, strokeWidth = lineWidth, cap = StrokeCap.Round)
        drawCircle(color = cardColor, radius = dotRadius + 1.5.dp.toPx(), center = layout.dot)
        drawCircle(color = color, radius = dotRadius, center = layout.dot)
        val textLayout = textMeasurer.measure(
            text = donutCalloutLabel(stage),
            style = TextStyle(
                color = color,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
            ),
        )
        val textX = if (layout.rightSide) {
            layout.lineEnd.x + labelGap
        } else {
            layout.lineEnd.x - labelGap - textLayout.size.width
        }
        drawText(
            textLayoutResult = textLayout,
            topLeft = Offset(textX, layout.lineEnd.y - textLayout.size.height / 2f),
        )
        start += sliceSweep + sliceGap
    }
}
