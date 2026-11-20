package com.senplo.plocare.ui.consumer.dashboard

import com.senplo.plocare.domain.filter.DashboardSnapshot
import com.senplo.plocare.domain.filter.FilterLifeCalculator
import com.senplo.plocare.domain.filter.FilterSnapshot
import com.senplo.plocare.ui.consumer.filtercare.dDayBadge
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlin.math.ceil
import kotlin.math.round
import kotlin.math.roundToInt
import kotlin.time.Duration

internal const val GAUGE_CELL_COUNT = 10

/**
 * Days between the recommended purchase date and the expected replacement date. Figma banner 380:2209
 * pairs a 11/10 replacement with an 11/3 purchase date; the server will own this value later.
 */
internal const val PURCHASE_LEAD_DAYS = 7

/** The banner pushes the content down, then closes (screen-flow spec 4.1); the delay is not specified anywhere. */
internal const val BANNER_VISIBLE_MILLIS = 5_000L

// Figma 349:1675 gauge: cell fill opacity rises in pairs (30/50/70/85/100%).
private val gaugeCellAlphas = listOf(0.3f, 0.3f, 0.5f, 0.5f, 0.7f, 0.7f, 0.85f, 0.85f, 1f, 1f)

internal fun formatIntWithComma(value: Int): String {
    val sign = if (value < 0) "-" else ""
    val digits = kotlin.math.abs(value).toString()
    val grouped = digits.reversed().chunked(3).joinToString(",").reversed()
    return sign + grouped
}

internal fun formatKoreanMonthDay(date: LocalDate): String =
    "${date.month.ordinal + 1}월 ${date.day}일"

internal fun formatKoreanWeekday(date: LocalDate): String = when (date.dayOfWeek) {
    DayOfWeek.MONDAY -> "월"
    DayOfWeek.TUESDAY -> "화"
    DayOfWeek.WEDNESDAY -> "수"
    DayOfWeek.THURSDAY -> "목"
    DayOfWeek.FRIDAY -> "금"
    DayOfWeek.SATURDAY -> "토"
    DayOfWeek.SUNDAY -> "일"
}

internal fun syncLabel(elapsed: Duration): String {
    val minutes = elapsed.inWholeMinutes
    val hours = elapsed.inWholeHours
    return when {
        minutes < 1L -> "방금 전 갱신"
        minutes < 60L -> "${minutes}분 전 갱신"
        hours < 24L -> "${hours}시간 전 갱신"
        else -> "24시간 이상 미수신"
    }
}

internal fun filterTitle(filter: FilterSnapshot): String = "${filter.stage}단 ${filter.name}"

/** `사용 L / 정격 L`; cycle-only filters (no rated liters, `CAT-01`) show their expiry date instead. */
internal fun usageOverRated(filter: FilterSnapshot): String =
    if (filter.ratedCapacityL > 0.0) {
        "${formatIntWithComma(round(filter.usageL).toInt())}/${formatIntWithComma(round(filter.ratedCapacityL).toInt())}L"
    } else {
        "${formatKoreanMonthDay(filter.effectiveDate)} 주기 만료"
    }

internal fun usagePercentLabel(filter: FilterSnapshot): String =
    "${filter.exhaustionPercent.roundToInt().coerceAtLeast(0)}%"

internal fun gaugeFilledCells(exhaustionPercent: Double): Int =
    ceil(exhaustionPercent / (100.0 / GAUGE_CELL_COUNT)).toInt().coerceIn(0, GAUGE_CELL_COUNT)

internal fun gaugeCellAlpha(index: Int): Float = gaugeCellAlphas[index.coerceIn(0, GAUGE_CELL_COUNT - 1)]

internal data class ReplacementBannerCopy(
    val filterId: String,
    val filterLabel: String,
    val headline: String,
    val remainingLabel: String?,
    val purchaseHint: String,
)

/**
 * Local stand-in for the server-timed replacement banner: shows the filter with the nearest effective
 * replacement date once it is within [FilterLifeCalculator.BUNDLE_PROXIMITY_DAYS] days.
 */
internal fun replacementBanner(snapshot: DashboardSnapshot): ReplacementBannerCopy? {
    val nearest = snapshot.filters.minByOrNull { it.effectiveRemainingDays } ?: return null
    if (nearest.effectiveRemainingDays > FilterLifeCalculator.BUNDLE_PROXIMITY_DAYS) return null

    val date = nearest.effectiveDate
    val headline = if (nearest.effectiveRemainingDays <= 0) {
        "교체 시기가 지났어요"
    } else {
        "${formatKoreanMonthDay(date)}(${formatKoreanWeekday(date)}) 교체 예상"
    }
    val purchaseBy = date.minus(PURCHASE_LEAD_DAYS, DateTimeUnit.DAY)
    val purchaseHint = if (purchaseBy > snapshot.today) {
        "${formatKoreanMonthDay(purchaseBy)}까지 구매하면 제때 받아요"
    } else {
        "지금 구매하면 가장 빨리 받아요"
    }
    return ReplacementBannerCopy(
        filterId = nearest.id,
        filterLabel = filterTitle(nearest),
        headline = headline,
        remainingLabel = if (nearest.ratedCapacityL > 0.0 && nearest.remainingL > 0.0) {
            "남은 ${formatIntWithComma(round(nearest.remainingL).toInt())}L"
        } else {
            null
        },
        purchaseHint = purchaseHint,
    )
}

internal data class ReplacementForecastCopy(
    val filterId: String,
    val headline: String,
    val chips: List<String>,
    val purchaseLine: String,
    val companionLine: String?,
)

/**
 * Figma replacement-forecast modal 74:298: the filter with the nearest effective date, plus the next
 * nearest filter as the one worth replacing together. When the modal appears is a feature-phase decision.
 */
internal fun replacementForecast(snapshot: DashboardSnapshot): ReplacementForecastCopy? {
    val sorted = snapshot.filters.sortedBy { it.effectiveRemainingDays }
    val nearest = sorted.firstOrNull() ?: return null
    val companion = sorted.getOrNull(1)
    val purchaseBy = nearest.effectiveDate.minus(PURCHASE_LEAD_DAYS, DateTimeUnit.DAY)
    return ReplacementForecastCopy(
        filterId = nearest.id,
        headline = "${formatKoreanDateWithWeekday(nearest.effectiveDate)} 교체 예상",
        chips = listOfNotNull(
            filterTitle(nearest),
            dDayBadge(nearest.effectiveRemainingDays),
            if (nearest.ratedCapacityL > 0.0 && nearest.remainingL > 0.0) {
                "남은 양 ${formatIntWithComma(round(nearest.remainingL).toInt())}L"
            } else {
                null
            },
        ),
        purchaseLine = "구매 권장일 ${formatKoreanDateWithWeekday(purchaseBy)} • 배송 기간 포함",
        companionLine = companion?.let {
            "함께 갈면 좋은 필터 ${filterTitle(it)} (${it.effectiveRemainingDays - nearest.effectiveRemainingDays}일 차이)"
        },
    )
}

private fun formatKoreanDateWithWeekday(date: LocalDate): String =
    "${formatKoreanMonthDay(date)}(${formatKoreanWeekday(date)})"
