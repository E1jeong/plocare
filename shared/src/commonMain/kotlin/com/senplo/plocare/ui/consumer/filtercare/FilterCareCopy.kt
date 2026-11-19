package com.senplo.plocare.ui.consumer.filtercare

import com.senplo.plocare.domain.filter.FilterSnapshot
import com.senplo.plocare.domain.filter.ReplacementReason
import com.senplo.plocare.ui.consumer.dashboard.formatIntWithComma
import com.senplo.plocare.ui.consumer.dashboard.formatKoreanMonthDay
import com.senplo.plocare.ui.consumer.dashboard.formatKoreanWeekday
import kotlinx.datetime.LocalDate
import kotlin.math.round

internal fun dDayBadge(remainingDays: Int): String = when {
    remainingDays > 0 -> "D-$remainingDays"
    remainingDays == 0 -> "D-day"
    else -> "D+${-remainingDays}"
}

/** Figma 74:721: `11월 10일(화) 도달 예상` this year, `2027년 4월 8일 교환주기 만료` in another year. */
internal fun filterStatusDateLabel(filter: FilterSnapshot, today: LocalDate): String {
    val date = filter.effectiveDate
    val dateLabel = if (date.year == today.year) {
        "${formatKoreanMonthDay(date)}(${formatKoreanWeekday(date)})"
    } else {
        "${date.year}년 ${formatKoreanMonthDay(date)}"
    }
    val reason = when (filter.replacementReason) {
        ReplacementReason.CAPACITY -> "도달 예상"
        ReplacementReason.CYCLE_EXPIRY -> "교환주기 만료"
    }
    return "$dateLabel $reason"
}

/** `2,950 / 3,000L`; null for cycle-only filters without rated liters (`CAT-01`). */
internal fun usageOverRatedSpaced(filter: FilterSnapshot): String? =
    if (filter.ratedCapacityL > 0.0) {
        "${formatIntWithComma(round(filter.usageL).toInt())} / ${formatIntWithComma(round(filter.ratedCapacityL).toInt())}L"
    } else {
        null
    }

internal fun sortedByReplacement(filters: List<FilterSnapshot>): List<FilterSnapshot> =
    filters.sortedWith(compareBy({ it.effectiveRemainingDays }, { it.stage }))

internal fun usageFraction(filter: FilterSnapshot): Float =
    (filter.exhaustionPercent / 100.0).toFloat().coerceIn(0f, 1f)

internal fun wonFrom(amountKrw: Int): String = "${formatIntWithComma(amountKrw)}원~"
