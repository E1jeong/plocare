package com.senplo.plocare.ui.consumer.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.senplo.plocare.domain.filter.FilterColorLevel
import com.senplo.plocare.domain.filter.FilterSnapshot
import com.senplo.plocare.ui.theme.PloCareColor

@Composable
fun FilterCylinderGrid(
    filters: List<FilterSnapshot>,
    onSelfReplace: (FilterSnapshot) -> Unit,
    onRequestReplacement: (FilterSnapshot) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        chunkFilterRows(filters).forEach { row ->
            when {
                row.size == 1 && filters.size == 1 -> {
                    FilterCylinderCard(
                        filter = row.single(),
                        onSelfReplace = { onSelfReplace(row.single()) },
                        onRequestReplacement = { onRequestReplacement(row.single()) },
                    )
                }
                row.size == 1 -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .align(Alignment.CenterHorizontally),
                    ) {
                        FilterCylinderCard(
                            filter = row.single(),
                            onSelfReplace = { onSelfReplace(row.single()) },
                            onRequestReplacement = { onRequestReplacement(row.single()) },
                        )
                    }
                }
                else -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        row.forEach { filter ->
                            FilterCylinderCard(
                                filter = filter,
                                onSelfReplace = { onSelfReplace(filter) },
                                onRequestReplacement = { onRequestReplacement(filter) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterCylinderCard(
    filter: FilterSnapshot,
    onSelfReplace: () -> Unit,
    onRequestReplacement: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = filter.colorLevel.color()
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(PloCareColor.SurfaceCard)
            .border(1.dp, accent.copy(alpha = 0.22f), RoundedCornerShape(20.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StageChip(stage = filter.stage)
            DDayChip(label = dDayLabel(filter.remainingDays), color = accent)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = filter.name,
            color = PloCareColor.TextPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(10.dp))
        val tankHeight = 228.dp
        val waterFill = filter.fillFraction.coerceIn(0f, 1f)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(tankHeight),
        ) {
            WaterCartridge(
                fillFraction = waterFill,
                color = accent,
                modifier = Modifier.fillMaxSize(),
            )
            RemainingOnWater(
                remainingLabel = formatLiters(filter.remainingL),
                waterFill = waterFill,
                tankHeight = tankHeight,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
            Text(
                text = "기준 ${formatLiters(filter.ratedCapacityL)}",
                color = Color.White.copy(alpha = 0.48f),
                fontSize = 11.sp,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp),
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        TextButton(
            onClick = onSelfReplace,
            contentPadding = ButtonDefaults.TextButtonContentPadding,
        ) {
            Text(
                text = "자가 교체",
                color = PloCareColor.TextSecondary,
                fontSize = 11.sp,
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp),
        ) {
            if (filter.showReplacementRequest) {
                Button(
                    onClick = onRequestReplacement,
                    modifier = Modifier.fillMaxWidth().height(36.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accent),
                    shape = RoundedCornerShape(11.dp),
                ) {
                    Text(
                        text = "교체 신청",
                        color = PloCareColor.BgDeep,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
private fun RemainingOnWater(
    remainingLabel: String,
    waterFill: Float,
    tankHeight: Dp,
    modifier: Modifier = Modifier,
) {
    val sitsOnWater = waterFill >= 0.28f
    Box(
        modifier = modifier
            .padding(
                bottom = if (sitsOnWater) tankHeight * waterFill + 6.dp else tankHeight * 0.42f,
            )
            .clip(RoundedCornerShape(20.dp))
            .background(Color.Black.copy(alpha = 0.38f))
            .padding(horizontal = 10.dp, vertical = 5.dp),
    ) {
        Text(
            text = "${remainingLabel} 남음",
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun StageChip(stage: Int) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(PloCareColor.SurfaceDark)
            .padding(horizontal = 8.dp, vertical = 3.dp),
    ) {
        Text(
            text = "${stage}단계",
            color = PloCareColor.AquaTeal,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun DDayChip(label: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.16f))
            .padding(horizontal = 8.dp, vertical = 3.dp),
    ) {
        Text(
            text = label,
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun WaterCartridge(
    fillFraction: Float,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val stroke = 2.2f
        val rx = size.width * 0.40f
        val ry = rx * 0.26f
        val cx = size.width / 2f
        val left = cx - rx
        val right = cx + rx
        val topCy = ry + stroke
        val bottomCy = size.height - ry - stroke
        val bodyHeight = bottomCy - topCy
        val topOval = Rect(left, topCy - ry, right, topCy + ry)
        val bottomOval = Rect(left, bottomCy - ry, right, bottomCy + ry)
        val outline = Color.White.copy(alpha = 0.78f)
        val silhouette = cylinderSilhouette(left, right, topCy, bottomCy, topOval, bottomOval)

        clipPath(silhouette) {
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF152238),
                        Color(0xFF0B1424),
                        Color(0xFF070E18),
                    ),
                ),
            )
            val waterHeight = bodyHeight * fillFraction.coerceIn(0f, 1f)
            if (waterHeight > 0f) {
                val waterCy = (bottomCy - waterHeight).coerceIn(topCy, bottomCy)
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            color.copy(alpha = 0.32f),
                            color.copy(alpha = 0.68f),
                            color.copy(alpha = 0.96f),
                        ),
                        startY = waterCy,
                        endY = bottomCy + ry,
                    ),
                    topLeft = Offset(left, waterCy),
                    size = Size(rx * 2f, bottomCy + ry - waterCy),
                )
                drawOval(
                    color = color.copy(alpha = 0.55f),
                    topLeft = Offset(left, waterCy - ry),
                    size = Size(rx * 2f, ry * 2f),
                )
                drawOval(
                    color = Color.White.copy(alpha = 0.22f),
                    topLeft = Offset(left, waterCy - ry),
                    size = Size(rx * 2f, ry * 2f),
                    style = Stroke(width = 1.4f),
                )
            }
        }

        drawLine(outline, Offset(left, topCy), Offset(left, bottomCy), strokeWidth = stroke)
        drawLine(outline, Offset(right, topCy), Offset(right, bottomCy), strokeWidth = stroke)
        drawOval(outline, topLeft = Offset(left, topCy - ry), size = Size(rx * 2f, ry * 2f), style = Stroke(width = stroke))
        drawOval(outline, topLeft = Offset(left, bottomCy - ry), size = Size(rx * 2f, ry * 2f), style = Stroke(width = stroke))
    }
}

private fun cylinderSilhouette(
    left: Float,
    right: Float,
    topCy: Float,
    bottomCy: Float,
    topOval: Rect,
    bottomOval: Rect,
): Path = Path().apply {
    moveTo(left, topCy)
    arcTo(topOval, 180f, -180f, false)
    lineTo(right, bottomCy)
    arcTo(bottomOval, 0f, 180f, false)
    close()
}

@Composable
private fun FilterColorLevel.color(): Color = when (this) {
    FilterColorLevel.SAFE -> PloCareColor.AquaTeal
    FilterColorLevel.REPLACE_SOON -> PloCareColor.StatusWarn
    FilterColorLevel.EXHAUSTED -> PloCareColor.StatusAlert
}

internal fun chunkFilterRows(filters: List<FilterSnapshot>): List<List<FilterSnapshot>> {
    if (filters.isEmpty()) return emptyList()
    return when (filters.size) {
        1 -> listOf(filters)
        3 -> listOf(filters.take(2), filters.drop(2))
        5 -> listOf(filters.take(2), filters.drop(2).take(2), filters.drop(4))
        else -> filters.chunked(2)
    }
}
