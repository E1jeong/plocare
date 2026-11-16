package com.senplo.plocare.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.senplo.plocare.ui.theme.PloCareColor
import com.senplo.plocare.ui.theme.PloCareTheme

data class PloCareBottomBarItem<T>(
    val id: T,
    val label: String,
    val icon: Painter,
)

@Composable
fun <T> PloCareBottomBar(
    items: List<PloCareBottomBarItem<T>>,
    selectedId: T,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = PloCareColor
    val outerBrush = Brush.verticalGradient(
        colors = listOf(
            colors.White.copy(alpha = 0.96f),
            colors.White.copy(alpha = 0.82f),
        ),
    )
    val pillShape = RoundedCornerShape(100.dp)
    val selectedFillBrush = Brush.verticalGradient(
        colors = listOf(
            colors.GradientSecondary.copy(alpha = 0.12f),
            colors.GradientSecondary.copy(alpha = 0.22f),
        ),
    )
    val selectedBorderBrush = Brush.verticalGradient(
        colors = listOf(
            colors.GradientSecondary.copy(alpha = 0.08f),
            colors.GradientSecondary.copy(alpha = 0.25f),
        ),
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(outerBrush)
            .navigationBarsPadding()
            .padding(start = 10.dp, top = 10.dp, end = 10.dp, bottom = 18.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 12.dp,
                    shape = pillShape,
                    ambientColor = colors.Primary.copy(alpha = 0.08f),
                    spotColor = colors.Primary.copy(alpha = 0.08f),
                )
                .background(colors.White, pillShape)
                .border(1.dp, colors.Black10.copy(alpha = 0.6f), pillShape)
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items.forEach { item ->
                val selected = item.id == selectedId
                val contentColor = if (selected) colors.Primary else colors.Black60Body
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(pillShape)
                        .then(
                            if (selected) {
                                Modifier
                                    .background(selectedFillBrush, pillShape)
                                    .border(1.dp, selectedBorderBrush, pillShape)
                            } else {
                                Modifier
                            },
                        )
                        .clickable(role = Role.Tab) { onSelect(item.id) }
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
                ) {
                    Icon(
                        painter = item.icon,
                        contentDescription = item.label,
                        tint = contentColor,
                        modifier = Modifier.size(20.dp),
                    )
                    Text(
                        text = item.label,
                        style = PloCareTheme.typography.gnbLabel,
                        color = contentColor,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Composable
fun PloCareWizardHeader(
    title: String,
    deviceTitle: String,
    currentStep: Int,
    totalSteps: Int,
    onBackClick: () -> Unit,
    onScanClick: (() -> Unit)? = null,
    scanLabel: String = "스캔",
    modifier: Modifier = Modifier,
) {
    val colors = PloCareColor
    val typography = PloCareTheme.typography

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Top header bar (79:3089)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(role = Role.Button, onClick = onBackClick)
                    .padding(vertical = 4.dp, horizontal = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                BackChevronIcon(tint = colors.TextMain)
                Text(
                    text = title,
                    style = typography.titleLarge,
                    color = colors.TextMain,
                )
            }

            if (onScanClick != null) {
                val pillShape = RoundedCornerShape(100.dp)
                Row(
                    modifier = Modifier
                        .shadow(
                            elevation = 8.dp,
                            shape = pillShape,
                            ambientColor = Color.Black.copy(alpha = 0.06f),
                            spotColor = Color.Black.copy(alpha = 0.06f),
                        )
                        .clip(pillShape)
                        .background(colors.White, pillShape)
                        .clickable(role = Role.Button, onClick = onScanClick)
                        .padding(horizontal = 18.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    ScanFrameIcon(tint = colors.Primary)
                    Text(
                        text = scanLabel,
                        style = typography.bodyLarge,
                        color = colors.Primary,
                    )
                }
            }
        }

        // Device subtitle + step badge + segmented progress bar (79:3105)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = deviceTitle,
                    style = typography.titleMid,
                    color = colors.TextMain,
                )
                Box(
                    modifier = Modifier
                        .background(colors.Black10, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "${currentStep.coerceIn(1, totalSteps)}/$totalSteps",
                        style = typography.gnbLabel,
                        color = colors.Primary,
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                for (step in 1..totalSteps.coerceAtLeast(1)) {
                    val active = step <= currentStep
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .background(
                                color = if (active) colors.GradientMain else colors.Black10,
                                shape = RoundedCornerShape(100.dp),
                            ),
                    )
                }
            }
        }
    }
}

@Composable
private fun BackChevronIcon(tint: Color) {
    Canvas(modifier = Modifier.size(20.dp)) {
        val path = Path().apply {
            moveTo(size.width * 0.65f, size.height * 0.18f)
            lineTo(size.width * 0.30f, size.height * 0.50f)
            lineTo(size.width * 0.65f, size.height * 0.82f)
        }
        drawPath(
            path = path,
            color = tint,
            style = Stroke(
                width = 2.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round,
            ),
        )
    }
}

@Composable
private fun ScanFrameIcon(tint: Color) {
    Canvas(modifier = Modifier.size(18.dp)) {
        val stroke = 1.7.dp.toPx()
        val corner = size.width * 0.28f
        val pad = size.width * 0.1f
        val max = size.width - pad

        // Top-left bracket
        drawLine(tint, Offset(pad, pad), Offset(pad + corner, pad), stroke, StrokeCap.Round)
        drawLine(tint, Offset(pad, pad), Offset(pad, pad + corner), stroke, StrokeCap.Round)
        // Top-right bracket
        drawLine(tint, Offset(max, pad), Offset(max - corner, pad), stroke, StrokeCap.Round)
        drawLine(tint, Offset(max, pad), Offset(max, pad + corner), stroke, StrokeCap.Round)
        // Bottom-left bracket
        drawLine(tint, Offset(pad, max), Offset(pad + corner, max), stroke, StrokeCap.Round)
        drawLine(tint, Offset(pad, max), Offset(pad, max - corner), stroke, StrokeCap.Round)
        // Bottom-right bracket
        drawLine(tint, Offset(max, max), Offset(max - corner, max), stroke, StrokeCap.Round)
        drawLine(tint, Offset(max, max), Offset(max, max - corner), stroke, StrokeCap.Round)
        // Center scan line
        drawLine(
            color = tint,
            start = Offset(pad + 2.dp.toPx(), size.height * 0.5f),
            end = Offset(max - 2.dp.toPx(), size.height * 0.5f),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
        )
    }
}
