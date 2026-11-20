package com.senplo.plocare.ui.components

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
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.senplo.plocare.ui.theme.PloCareColor
import com.senplo.plocare.ui.theme.PloCareTheme
import org.jetbrains.compose.resources.painterResource
import plocare.shared.generated.resources.Res
import plocare.shared.generated.resources.ic_back
import plocare.shared.generated.resources.ic_split_scene

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
    // Figma GNB (74:878 / 126:1033) bar, pill, and active-tab gradients
    val outerBrush = Brush.verticalGradient(
        colors = listOf(
            colors.White,
            colors.White.copy(alpha = 0.6f),
        ),
    )
    val topStrokeBrush = Brush.horizontalGradient(
        0f to colors.White.copy(alpha = 0.05f),
        0.18f to colors.White,
        0.86f to colors.White,
        1f to colors.White.copy(alpha = 0.05f),
    )
    val pillShape = RoundedCornerShape(100.dp)
    val pillBorderBrush = Brush.verticalGradient(
        colors = listOf(
            colors.White.copy(alpha = 0.2f),
            colors.White.copy(alpha = 0.05f),
        ),
    )
    val selectedFillBrush = Brush.verticalGradient(
        colors = listOf(
            colors.GradientSecondary.copy(alpha = 0.1f),
            colors.GradientSecondary.copy(alpha = 0.2f),
        ),
    )
    val selectedBorderBrush = Brush.verticalGradient(
        colors = listOf(
            colors.GradientSecondary.copy(alpha = 0.05f),
            colors.GradientSecondary.copy(alpha = 0.2f),
        ),
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .dropShadow(
                shape = RectangleShape,
                shadow = Shadow(
                    radius = 80.dp,
                    color = Color(0xFF222C3A).copy(alpha = 0.03f),
                    offset = DpOffset(0.dp, (-24).dp),
                ),
            )
            .background(outerBrush)
            .drawWithContent {
                drawContent()
                val strokeWidth = 1.dp.toPx()
                drawLine(
                    brush = topStrokeBrush,
                    start = Offset(0f, strokeWidth / 2),
                    end = Offset(size.width, strokeWidth / 2),
                    strokeWidth = strokeWidth,
                )
            }
            .navigationBarsPadding()
            .padding(start = 10.dp, top = 10.dp, end = 10.dp, bottom = 18.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .dropShadow(
                    shape = pillShape,
                    shadow = Shadow(
                        radius = 24.dp,
                        color = Color(0xFF1F5F8B).copy(alpha = 0.05f),
                        offset = DpOffset(0.dp, 4.dp),
                    ),
                )
                .background(colors.White, pillShape)
                .border(1.dp, pillBorderBrush, pillShape)
                .padding(4.dp)
                .selectableGroup(),
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
                        .selectable(selected = selected, role = Role.Tab) { onSelect(item.id) }
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
                ) {
                    Icon(
                        painter = item.icon,
                        // The label below already names the tab for screen readers.
                        contentDescription = null,
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
    onTestClick: (() -> Unit)? = null,
    testLabel: String = "테스트",
    backLabel: String = "뒤로",
    modifier: Modifier = Modifier,
) {
    val colors = PloCareColor
    val typography = PloCareTheme.typography
    val steps = totalSteps.coerceAtLeast(1)

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
                Icon(
                    painter = painterResource(Res.drawable.ic_back),
                    contentDescription = backLabel,
                    tint = colors.TextMain,
                )
                Text(
                    text = title,
                    style = typography.titleLarge,
                    color = colors.TextMain,
                )
            }

            // Figma step-1 `테스트` pill (79:2408); the 4-step wizard has no header scan button.
            if (onTestClick != null) {
                val pillShape = RoundedCornerShape(100.dp)
                Row(
                    modifier = Modifier
                        .dropShadow(
                            shape = pillShape,
                            shadow = Shadow(
                                radius = 24.dp,
                                color = Color.Black.copy(alpha = 0.04f),
                                offset = DpOffset(0.dp, 4.dp),
                            ),
                        )
                        .clip(pillShape)
                        .background(colors.White, pillShape)
                        .clickable(role = Role.Button, onClick = onTestClick)
                        .padding(horizontal = 18.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_split_scene),
                        contentDescription = null,
                        tint = colors.Primary,
                    )
                    Text(
                        text = testLabel,
                        // Figma component body/large: Regular 400, 15/22, -0.03em
                        style = typography.bodyLarge.copy(fontWeight = FontWeight.Normal),
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
                        .padding(horizontal = 6.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "${currentStep.coerceIn(1, steps)}/$steps",
                        // Figma 79:2256 counter: Regular 400, 13/22, -0.03em
                        style = typography.gnbLabel.copy(
                            fontWeight = FontWeight.Normal,
                            lineHeight = 22.sp,
                            letterSpacing = (-0.39).sp,
                        ),
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
                for (step in 1..steps) {
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
