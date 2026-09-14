package com.senplo.plocare.ui.consumer.filtercare

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.plus

internal const val DEFAULT_VISIT_ADDRESS = "서울 성동구 성수일로 10"

internal enum class VisitTimeWindow(val label: String) {
    MORNING("10:00–12:00"),
    AFTERNOON("14:00–16:00"),
    EVENING("16:00–18:00"),
}

internal data class VisitRequestDraft(
    val contact: String,
    val address: String,
    val selectedFilterIds: Set<String>,
    val date: LocalDate?,
    val window: VisitTimeWindow?,
    val notes: String,
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

internal fun visitDateChipLabel(date: LocalDate): String {
    val iso = date.toString()
    return "${iso.substring(5, 7).toInt()}/${iso.substring(8, 10).toInt()}"
}

internal fun visitDateOptions(today: LocalDate, count: Int = 5): List<LocalDate> {
    val start = addBusinessDays(today, 2)
    val dates = mutableListOf<LocalDate>()
    var date = start
    while (dates.size < count) {
        if (!date.isWeekend()) dates.add(date)
        date = date.plus(1, DateTimeUnit.DAY)
    }
    return dates
}

internal fun validateVisitRequest(draft: VisitRequestDraft, today: LocalDate): String? {
    val phoneDigits = draft.contact.filter { it.isDigit() }
    val earliest = addBusinessDays(today, 2)
    return when {
        draft.address.trim().isEmpty() -> "주소를 입력해 주세요."
        phoneDigits.length < 10 -> "연락처를 입력해 주세요."
        draft.selectedFilterIds.isEmpty() -> "교체할 필터를 선택해 주세요."
        draft.date == null -> "희망 날짜를 선택해 주세요."
        draft.date < earliest -> "방문일은 영업일 기준 이틀 이후부터 선택할 수 있습니다."
        draft.date.isWeekend() -> "주말에는 방문 예약이 불가합니다."
        draft.window == null -> "희망 시간대를 선택해 주세요."
        else -> null
    }
}

private fun LocalDate.isWeekend(): Boolean {
    val iso = dayOfWeek.isoDayNumber
    return iso == 6 || iso == 7
}
