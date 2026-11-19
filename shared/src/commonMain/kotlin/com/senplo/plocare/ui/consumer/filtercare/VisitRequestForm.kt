package com.senplo.plocare.ui.consumer.filtercare

import com.senplo.plocare.ui.consumer.dashboard.formatKoreanMonthDay
import com.senplo.plocare.ui.consumer.dashboard.formatKoreanWeekday
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.plus

// Local sample contact until the contact/address screen (Figma 11) and MyPage provide it.
internal const val DEFAULT_VISIT_ADDRESS = "서울 성동구 성수일로 10"
internal const val DEFAULT_VISIT_ADDRESS_DETAIL = "OO아파트 101동 1203호"
internal const val DEFAULT_VISIT_CONTACT_MASKED = "010-****-1234"

/** Visit preferences take only AM/PM; the exact arrival time is agreed by phone. */
internal enum class VisitTimeWindow(val label: String, val startHour: Int, val endHour: Int) {
    MORNING("오전", 10, 12),
    AFTERNOON("오후", 14, 16),
}

internal const val MAX_VISIT_PREFERENCES = 3
private const val VISIT_CALENDAR_WEEKS = 3

internal data class VisitPreference(
    val date: LocalDate,
    val window: VisitTimeWindow,
)

internal data class VisitRequestDraft(
    val contact: String,
    val address: String,
    val selectedFilterIds: Set<String>,
    /** In priority order; the first one seeds the local visit ticket. */
    val preferences: List<VisitPreference>,
)

internal fun encodeVisitFilterIds(ids: List<String>): String = ids.joinToString(",")

internal fun decodeVisitFilterIds(raw: String): List<String> =
    raw.split(',').map { it.trim() }.filter { it.isNotEmpty() }

internal fun addBusinessDays(from: LocalDate, days: Int): LocalDate {
    var date = from
    var added = 0
    while (added < days) {
        date = date.plus(1, DateTimeUnit.DAY)
        if (!date.isWeekend()) added++
    }
    return date
}

/** Figma 08 calendar: Monday-first weeks starting with the current week. */
internal fun visitCalendarDays(today: LocalDate, weeks: Int = VISIT_CALENDAR_WEEKS): List<LocalDate> {
    val monday = today.minus(today.dayOfWeek.isoDayNumber - 1, DateTimeUnit.DAY)
    return List(weeks * 7) { monday.plus(it, DateTimeUnit.DAY) }
}

/** Past dates, the next two business days, and weekends (sample days off) cannot be chosen. */
internal fun isVisitDateSelectable(date: LocalDate, today: LocalDate): Boolean =
    date >= addBusinessDays(today, 2) && !date.isWeekend()

internal fun toggleVisitDate(
    current: List<LocalDate>,
    date: LocalDate,
    max: Int = MAX_VISIT_PREFERENCES,
): List<LocalDate> = when {
    date in current -> current - date
    current.size >= max -> current
    else -> current + date
}

internal fun visitPreferenceLabel(date: LocalDate): String =
    "${formatKoreanMonthDay(date)}(${formatKoreanWeekday(date)})"

private fun LocalDate.isWeekend(): Boolean {
    val iso = dayOfWeek.isoDayNumber
    return iso == 6 || iso == 7
}
