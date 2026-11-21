package com.senplo.plocare.ui.splash

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.FirstBaseline
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.senplo.plocare.ui.theme.LightSystemBarIconsEffect
import com.senplo.plocare.ui.theme.PloCareColor
import com.senplo.plocare.ui.theme.rememberBrandFontFamily
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import plocare.shared.generated.resources.Res
import plocare.shared.generated.resources.plocare_logo

private const val SPLASH_DURATION_MS = 2800L

/**
 * Intro screen matching the 390x844 brand artwork (KakaoTalk_20261117_093015674.png, @3x).
 * Vertical positions are measured from that artwork: the logo and wordmark keep their offset from
 * the screen center, and the loading dots and tagline keep their distance from the bottom edge.
 */
@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    // Finish once: a quick double tap (or a tap racing the timer) must not navigate twice.
    val currentOnSplashFinished by rememberUpdatedState(onSplashFinished)
    var finished by remember { mutableStateOf(false) }
    val finish = {
        if (!finished) {
            finished = true
            currentOnSplashFinished()
        }
    }

    LaunchedEffect(Unit) {
        delay(SPLASH_DURATION_MS)
        finish()
    }

    val colors = PloCareColor
    val brandFont = rememberBrandFontFamily()
    val navigationBarBottom = WindowInsets.navigationBars
    LightSystemBarIconsEffect()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.Primary)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
            ) {
                // Tap to skip
                finish()
            },
    ) {
        Layout(
            content = {
                Image(
                    painter = painterResource(Res.drawable.plocare_logo),
                    contentDescription = null,
                    modifier = Modifier.size(width = LOGO_WIDTH, height = LOGO_HEIGHT),
                )
                Text(
                    text = "PloCare",
                    color = colors.White,
                    style = TextStyle(fontFamily = brandFont, fontWeight = FontWeight.Bold, fontSize = 34.sp),
                )
                Text(
                    text = "by 센플로",
                    color = colors.PrimaryTint,
                    style = TextStyle(fontFamily = brandFont, fontWeight = FontWeight.Normal, fontSize = 14.sp),
                )
                LoadingDots()
                Text(
                    text = "남은 만큼 쓰고, 제때 바꾸고.",
                    color = colors.PrimaryTint.copy(alpha = 0.8f),
                    style = TextStyle(fontFamily = brandFont, fontWeight = FontWeight.Normal, fontSize = 12.sp),
                )
            },
            modifier = Modifier.fillMaxSize(),
        ) { measurables, constraints ->
            val loose = constraints.copy(minWidth = 0, minHeight = 0)
            val (logo, title, byline, dots, tagline) = measurables.map { it.measure(loose) }
            val width = constraints.maxWidth
            val height = constraints.maxHeight
            val centerX = width / 2
            val centerY = height / 2
            // The artwork's home-indicator area is 34dp; only push the bottom group up for taller bars.
            val bottomExtra = (navigationBarBottom.getBottom(this) - 34.dp.roundToPx()).coerceAtLeast(0)
            val bottom = height - bottomExtra

            layout(width, height) {
                // Bottle (not the whole logo box) is horizontally centered.
                logo.place(centerX - BOTTLE_CENTER_FROM_LOGO_LEFT.roundToPx(), centerY - LOGO_TOP_FROM_CENTER.roundToPx())
                title.place(centerX - title.width / 2, centerY + TITLE_BASELINE_FROM_CENTER.roundToPx() - title[FirstBaseline])
                byline.place(centerX - byline.width / 2, centerY + BYLINE_BASELINE_FROM_CENTER.roundToPx() - byline[FirstBaseline])
                dots.place(centerX - dots.width / 2, bottom - DOTS_TOP_FROM_BOTTOM.roundToPx())
                tagline.place(centerX - tagline.width / 2, bottom - TAGLINE_BASELINE_FROM_BOTTOM.roundToPx() - tagline[FirstBaseline])
            }
        }
    }
}

@Composable
private fun LoadingDots() {
    val colors = PloCareColor
    val transition = rememberInfiniteTransition()
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 900, easing = LinearEasing), RepeatMode.Restart),
    )
    // Artwork frame: dots at 40% / 70% / 100%; the brightest dot steps left to right.
    val brightest = (phase.toInt() + 2) % 3
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(3) { index ->
            val alpha = when ((brightest - index + 3) % 3) {
                0 -> 1f
                1 -> 0.7f
                else -> 0.4f
            }
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(colors.GradientSecondary.copy(alpha = alpha), CircleShape),
            )
        }
    }
}

// Measured from the 390x844 artwork (pixels / 3).
private val LOGO_WIDTH = 80.9.dp
private val LOGO_HEIGHT = 110.3.dp
private val BOTTLE_CENTER_FROM_LOGO_LEFT = 23.8.dp
private val LOGO_TOP_FROM_CENTER = 139.7.dp
private val TITLE_BASELINE_FROM_CENTER = 58.0.dp
private val BYLINE_BASELINE_FROM_CENTER = 86.3.dp
private val DOTS_TOP_FROM_BOTTOM = 108.dp
private val TAGLINE_BASELINE_FROM_BOTTOM = 43.7.dp
