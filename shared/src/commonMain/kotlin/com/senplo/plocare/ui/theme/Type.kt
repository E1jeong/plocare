package com.senplo.plocare.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.Font
import plocare.shared.generated.resources.Res
import plocare.shared.generated.resources.ibmplexsanskr_bold
import plocare.shared.generated.resources.ibmplexsanskr_regular
import plocare.shared.generated.resources.pretendard_bold
import plocare.shared.generated.resources.pretendard_medium
import plocare.shared.generated.resources.pretendard_regular
import plocare.shared.generated.resources.pretendard_semibold

@Immutable
data class PloCareTypography(
    val headLarge: TextStyle,
    val headMid: TextStyle,
    val headSmall: TextStyle,
    val titleLarge: TextStyle,
    val titleMid: TextStyle,
    val titleSmall: TextStyle,
    val bodyLarge: TextStyle,
    val bodyMid: TextStyle,
    val bodySmall: TextStyle,
    val bodyTiny: TextStyle,
    val labelLarge: TextStyle,
    val labelMid: TextStyle,
    val labelMidMedium: TextStyle,
    val labelSmall: TextStyle,
    val labelTiny: TextStyle,
    val gnbLabel: TextStyle,
) {
    fun toMaterialTypography(): Typography = Typography(
        displayLarge = headLarge,
        displayMedium = headLarge,
        displaySmall = headMid,
        headlineLarge = headLarge,
        headlineMedium = headMid,
        headlineSmall = headSmall,
        titleLarge = titleLarge,
        titleMedium = titleMid,
        titleSmall = titleSmall,
        bodyLarge = bodyLarge,
        bodyMedium = bodyMid,
        bodySmall = bodySmall,
        labelLarge = labelLarge,
        labelMedium = labelMid,
        labelSmall = labelSmall,
    )
}

fun buildPloCareTypography(fontFamily: FontFamily = FontFamily.Default): PloCareTypography =
    PloCareTypography(
        headLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 32.sp,
            lineHeight = 38.sp,
            letterSpacing = (-0.64).sp,
        ),
        headMid = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 28.sp,
            lineHeight = 34.sp,
            letterSpacing = (-0.56).sp,
        ),
        headSmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 24.sp,
            lineHeight = 29.sp,
            letterSpacing = (-0.48).sp,
        ),
        titleLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 22.sp,
            lineHeight = 29.sp,
            letterSpacing = (-0.44).sp,
        ),
        titleMid = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            lineHeight = 23.sp,
            letterSpacing = (-0.36).sp,
        ),
        titleSmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            lineHeight = 21.sp,
            letterSpacing = (-0.32).sp,
        ),
        bodyLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 15.sp,
            lineHeight = 22.sp,
            letterSpacing = (-0.45).sp,
        ),
        bodyMid = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = (-0.14).sp,
        ),
        bodySmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            lineHeight = 17.sp,
            letterSpacing = (-0.12).sp,
        ),
        bodyTiny = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 11.sp,
            lineHeight = 15.sp,
            letterSpacing = (-0.11).sp,
        ),
        labelLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 22.sp,
            letterSpacing = (-0.16).sp,
        ),
        labelMid = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 18.sp,
            letterSpacing = (-0.14).sp,
        ),
        labelMidMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = (-0.14).sp,
        ),
        // Corrected from Figma 8px line-height anomaly on label/small (12pt) & Label/tiny (10pt)
        labelSmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = (-0.12).sp,
        ),
        labelTiny = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 10.sp,
            lineHeight = 14.sp,
            letterSpacing = (-0.10).sp,
        ),
        gnbLabel = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp,
            lineHeight = 18.sp,
            letterSpacing = (-0.26).sp,
        ),
    )

@Composable
fun rememberPretendardFontFamily(): FontFamily = FontFamily(
    Font(Res.font.pretendard_regular, FontWeight.Normal),
    Font(Res.font.pretendard_medium, FontWeight.Medium),
    Font(Res.font.pretendard_semibold, FontWeight.SemiBold),
    Font(Res.font.pretendard_bold, FontWeight.Bold),
)

/** Brand typeface (IBM Plex Sans KR) used by the intro artwork. */
@Composable
fun rememberBrandFontFamily(): FontFamily = FontFamily(
    Font(Res.font.ibmplexsanskr_regular, FontWeight.Normal),
    Font(Res.font.ibmplexsanskr_bold, FontWeight.Bold),
)

internal val LocalPloCareTypography = staticCompositionLocalOf { buildPloCareTypography() }
