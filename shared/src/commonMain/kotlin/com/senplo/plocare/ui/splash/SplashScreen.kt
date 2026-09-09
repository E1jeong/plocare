package com.senplo.plocare.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.senplo.plocare.ui.theme.PloCareColor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    // 1. Animation Progress Controllers
    val dropProgress = remember { Animatable(0f) }
    val rippleProgress = remember { Animatable(0f) }
    val stemProgress = remember { Animatable(0f) }
    val loopProgress = remember { Animatable(0f) }
    val apexSparkProgress = remember { Animatable(0f) }
    val iotPulseProgress = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Phase 1: Water drop fall (0 ~ 550ms)
        launch {
            dropProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(550, easing = FastOutSlowInEasing)
            )
        }

        delay(480)
        // Phase 2: Ripple expand (480 ~ 1100ms)
        launch {
            rippleProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(620, easing = LinearEasing)
            )
        }

        delay(80)
        // Phase 3: Stem column leap (560 ~ 1250ms)
        launch {
            stemProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(690, easing = FastOutSlowInEasing)
            )
        }

        delay(220)
        // Phase 4: Loop branch & reveal (780 ~ 1400ms)
        launch {
            loopProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(620, easing = FastOutSlowInEasing)
            )
        }

        delay(550)
        // Phase 5: Apex Star Spark & IoT Sensor Laser Pulse (1330 ~ 1850ms)
        launch {
            apexSparkProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(400, easing = FastOutSlowInEasing)
            )
        }
        launch {
            iotPulseProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(550, easing = LinearEasing)
            )
        }

        delay(250)
        // Phase 6: Brand typography fade-in (1580 ~ 2100ms)
        launch {
            textAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(520, easing = FastOutSlowInEasing)
            )
        }

        // Wait and transition
        delay(1200)
        onSplashFinished()
    }

    val colors = PloCareColor

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(colors.BgMid, colors.BgDeep),
                    radius = 1200f
                )
            )
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                // Tap to skip
                onSplashFinished()
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val logoCy = size.height * 0.38f
            val impactY = size.height * 0.68f

            val totW = 74.dp.toPx()
            val totH = 114.dp.toPx()
            val stemW = 16.dp.toPx()
            val loopH = 70.dp.toPx()

            val x0 = cx - totW / 2f
            val x1 = cx + totW / 2f
            val y0 = logoCy - totH / 2f
            val y1 = logoCy + totH / 2f
            val loopBot = y0 + loopH
            val stemCenterX = x0 + stemW / 2f
            val waveY = y0 + loopH / 2f
            val waveTh = 5.dp.toPx()

            // 1. Water Drop Falling
            if (dropProgress.value in 0.001f..0.999f) {
                val startDropY = size.height * 0.18f
                val curDropY = startDropY + (impactY - startDropY) * dropProgress.value
                val dropR = 5.5.dp.toPx()
                val dropAlpha = (1f - dropProgress.value * 0.3f).coerceIn(0f, 1f)

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(colors.LuminousIce, colors.VividCyan),
                        center = Offset(cx, curDropY),
                        radius = dropR * 1.5f
                    ),
                    radius = dropR,
                    center = Offset(cx, curDropY),
                    alpha = dropAlpha
                )
            }

            // 2. Ripple Rings
            if (rippleProgress.value in 0.001f..0.999f) {
                val rp = rippleProgress.value
                val maxRadius = 75.dp.toPx()
                val ringCount = 3
                for (i in 0 until ringCount) {
                    val delay = i * 0.18f
                    val ringP = ((rp - delay) / (1f - delay)).coerceIn(0f, 1f)
                    if (ringP > 0f) {
                        val radiusX = maxRadius * ringP
                        val radiusY = radiusX * 0.32f
                        val alpha = (1f - ringP) * 0.65f
                        drawOval(
                            color = colors.AquaTeal.copy(alpha = alpha),
                            topLeft = Offset(cx - radiusX, impactY - radiusY),
                            size = Size(radiusX * 2, radiusY * 2),
                            style = Stroke(width = max(1f, 2.dp.toPx() * (1f - ringP)))
                        )
                    }
                }
            }

            // 3. Concept 3 P Logo Base Geometry
            val logoBrush = Brush.verticalGradient(
                colors = listOf(colors.AquaTeal, colors.VividCyan),
                startY = y0,
                endY = y1
            )

            // Construct Full Master P Path
            val masterStem = Path().apply {
                addRoundRect(
                    RoundRect(
                        left = x0,
                        top = y0,
                        right = x0 + stemW,
                        bottom = y1,
                        cornerRadius = CornerRadius(stemW / 2f, stemW / 2f)
                    )
                )
                // Top junction fill
                addRect(Rect(x0, y0, x0 + stemW, y0 + stemW))
            }

            val outerLoop = Path().apply {
                arcTo(
                    rect = Rect(x1 - loopH, y0, x1, loopBot),
                    startAngleDegrees = -90f,
                    sweepAngleDegrees = 180f,
                    forceMoveTo = false
                )
                lineTo(x0 + stemW, loopBot)
                lineTo(x0 + stemW, y0)
                close()
            }

            val masterBase = Path().apply {
                op(masterStem, outerLoop, PathOperation.Union)
            }

            val innerCutout = Path().apply {
                val inY0 = y0 + stemW
                val inY1 = loopBot - stemW
                val inH = inY1 - inY0
                arcTo(
                    rect = Rect(x1 - stemW - inH, inY0, x1 - stemW, inY1),
                    startAngleDegrees = -90f,
                    sweepAngleDegrees = 180f,
                    forceMoveTo = false
                )
                lineTo(x0 + stemW, inY1)
                lineTo(x0 + stemW, inY0)
                close()
            }

            val masterHollow = Path().apply {
                op(masterBase, innerCutout, PathOperation.Difference)
            }

            // IoT Sensor Pulse Slit (Horizontal precision channel)
            val slitCutout = Path().apply {
                addRoundRect(
                    RoundRect(
                        left = x0 - 4.dp.toPx(),
                        top = waveY - waveTh / 2f,
                        right = x1 + 4.dp.toPx(),
                        bottom = waveY + waveTh / 2f,
                        cornerRadius = CornerRadius(waveTh / 2f, waveTh / 2f)
                    )
                )
            }

            val finalMasterPath = Path().apply {
                op(masterHollow, slitCutout, PathOperation.Difference)
            }

            // 4. Kinetic Stem Rise & Fountain Branch
            val sp = stemProgress.value
            val lp = loopProgress.value

            if (sp > 0f) {
                val curStemTop = y1 - (y1 - y0) * sp

                // Single column fountain leap from water impact up to logo
                if (sp < 1f) {
                    val streamBottom = impactY - (impactY - y1) * sp
                    drawLine(
                        brush = Brush.verticalGradient(
                            colors = listOf(colors.AquaTeal, colors.VividCyan.copy(alpha = 0.4f)),
                            startY = curStemTop,
                            endY = streamBottom
                        ),
                        start = Offset(stemCenterX, curStemTop),
                        end = Offset(stemCenterX, streamBottom),
                        strokeWidth = stemW * (1f - sp * 0.2f),
                        pathEffect = PathEffect.cornerPathEffect(stemW / 2f)
                    )
                }

                // Unmask the stem
                clipRect(
                    left = x0 - 10f,
                    top = curStemTop,
                    right = x0 + stemW + 1f,
                    bottom = y1 + 10f
                ) {
                    drawPath(path = finalMasterPath, brush = logoBrush, style = Fill)
                }
            }

            // 5. Unmask the Loop (Progressive reveal starting when stem passes waist)
            if (lp > 0f) {
                val loopClipHeight = loopH * lp
                clipRect(
                    left = x0 + stemW - 2f,
                    top = loopBot - loopClipHeight,
                    right = x1 + 10f,
                    bottom = loopBot + 10f
                ) {
                    drawPath(path = finalMasterPath, brush = logoBrush, style = Fill)
                }
            }

            // 6. Apex Diamond Star Spark
            val asp = apexSparkProgress.value
            if (asp in 0.01f..0.99f) {
                val intensity = sin(asp * PI.toFloat())
                val sparkCore = 5.dp.toPx() * intensity
                val sparkCross = 16.dp.toPx() * intensity

                drawCircle(
                    color = Color.White,
                    radius = sparkCore,
                    center = Offset(stemCenterX, y0)
                )
                drawLine(
                    color = Color.White,
                    start = Offset(stemCenterX - sparkCross, y0),
                    end = Offset(stemCenterX + sparkCross, y0),
                    strokeWidth = 2.dp.toPx()
                )
                drawLine(
                    color = Color.White,
                    start = Offset(stemCenterX, y0 - sparkCross),
                    end = Offset(stemCenterX, y0 + sparkCross),
                    strokeWidth = 2.dp.toPx()
                )
            }

            // 7. IoT Sensor Laser Pulse along the horizontal slit
            val iotp = iotPulseProgress.value
            if (iotp in 0.01f..0.99f) {
                val pulseX = (x0 - 10.dp.toPx()) + (totW + 20.dp.toPx()) * iotp
                val trailLen = 18.dp.toPx()

                // Laser head glow & trail
                drawLine(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            colors.VividCyan.copy(alpha = 0.8f),
                            Color.White
                        ),
                        startX = pulseX - trailLen,
                        endX = pulseX
                    ),
                    start = Offset(pulseX - trailLen, waveY),
                    end = Offset(pulseX, waveY),
                    strokeWidth = waveTh * 0.9f
                )
                drawCircle(
                    color = colors.LuminousIce,
                    radius = 3.5.dp.toPx(),
                    center = Offset(pulseX, waveY)
                )
            }
        }

        // 8. Brand Typography & Subtitle (Fade & Slide in)
        if (textAlpha.value > 0f) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(top = 180.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "P L O C A R E",
                    color = Color.White.copy(alpha = textAlpha.value),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 8.sp,
                    fontFamily = FontFamily.SansSerif
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "INTELLIGENT FILTER SYSTEM",
                    color = colors.TextSecondary.copy(alpha = textAlpha.value * 0.85f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 3.5.sp,
                    fontFamily = FontFamily.SansSerif
                )
            }
        }
    }
}
