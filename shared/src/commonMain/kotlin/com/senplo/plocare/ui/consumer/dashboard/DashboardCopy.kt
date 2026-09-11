package com.senplo.plocare.ui.consumer.dashboard

import com.senplo.plocare.domain.filter.DashboardSnapshot
import com.senplo.plocare.domain.filter.FilterSnapshot
import com.senplo.plocare.domain.filter.ForecastStage
import com.senplo.plocare.domain.filter.VisitTicket
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlin.math.round
import kotlin.time.Duration

internal fun remainingOverRated(filter: FilterSnapshot): String =
    "${formatLiters(filter.remainingL)} / ${formatLiters(filter.ratedCapacityL)}"

internal fun dDayLabel(remainingDays: Int): String =
    if (remainingDays <= 0) "수명 초과" else "D-${remainingDays}일"

internal fun formatLiters(value: Double): String =
    "${formatIntWithComma(round(value).toInt())} L"

internal fun formatIntWithComma(value: Int): String {
    val sign = if (value < 0) "-" else ""
    val digits = kotlin.math.abs(value).toString()
    val grouped = digits.reversed().chunked(3).joinToString(",").reversed()
    return sign + grouped
}

internal fun formatOneDecimal(value: Double): String {
    val tenths = round(value * 10.0).toInt()
    return if (tenths % 10 == 0) (tenths / 10).toString() else "${tenths / 10}.${tenths % 10}"
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

internal fun briefingTitle(snapshot: DashboardSnapshot): String {
    val avg = formatOneDecimal(snapshot.device.dailyAvgL)
    val date = formatKoreanMonthDay(snapshot.representativeDate)
    val dDay = if (snapshot.representativeRemainingDays <= 0) {
        "즉시 교체"
    } else {
        "D-${snapshot.representativeRemainingDays}"
    }
    return when (snapshot.forecastStage) {
        ForecastStage.COLD_START ->
            "[${snapshot.device.householdSize}인 가구 기준 예측] 일평균 ${avg} L/일 ➔ 목표일: $date ($dDay)"
        ForecastStage.MATURE ->
            "[AI 정밀 추론 가동 중] 14일 평균 ${avg} L/일 ➔ 목표일: $date ($dDay)"
    }
}

internal fun briefingCaption(snapshot: DashboardSnapshot): String = when (snapshot.forecastStage) {
    ForecastStage.COLD_START ->
        "설치 초기에는 가구원 수 기준으로 예측하고, 2개월(60일) 후 실제 사용량 기반 AI 정밀 모델로 전환돼요"
    ForecastStage.MATURE ->
        "2개월 누적 패턴 반영 · 매일 자정(00시) 정밀 추론 완료"
}

internal fun overCapacityMessage(filter: FilterSnapshot): String {
    val excessL = formatIntWithComma(round(filter.excessL).toInt())
    return "${filter.stage}단계 필터 정격 수명 초과! (+${filter.excessDays}일 / ${excessL}L 초과 음용 중) 정수 성능 보장을 위해 즉시 교체하세요."
}

internal fun bundleMessage(snapshot: DashboardSnapshot): String? {
    val bundle = snapshot.bundle ?: return null
    val stages = joinStages(bundle.stageLabels)
    return "출장비 절약 특허 추천! ${stages} 필터 교체 시기가 ${bundle.deltaDays}일 차이로 인접해요. 함께 교체하고 출장비 1회를 아끼세요."
}

internal fun visitWhen(ticket: VisitTicket): String {
    val date = formatKoreanMonthDay(ticket.scheduledDate)
    val weekday = formatKoreanWeekday(ticket.scheduledDate)
    val minute = ticket.minute.toString().padStart(2, '0')
    return "${date}(${weekday}) ${ticket.hour}:${minute}"
}

internal fun visitTargets(ticket: VisitTicket, filters: List<FilterSnapshot>): String {
    val names = ticket.targetFilterIds.mapNotNull { id ->
        filters.find { it.id == id }?.let { "${it.stage}단계 ${it.name}" }
    }
    return "대상: ${names.joinToString(", ")} 묶음 교체"
}

internal fun selfReplaceSummary(filter: FilterSnapshot, totalCumulativeL: Double): String =
    "「${filter.stage}단계 ${filter.name}」의 기준점만 ${formatLiters(totalCumulativeL)}로 갱신됩니다. 다른 필터와 정수기 총 누적 통수량은 그대로입니다."

internal fun joinStages(labels: List<String>): String = when (labels.size) {
    0 -> ""
    1 -> labels[0]
    2 -> "${labels[0]}과 ${labels[1]}"
    else -> labels.dropLast(1).joinToString(", ") + ", " + labels.last()
}
