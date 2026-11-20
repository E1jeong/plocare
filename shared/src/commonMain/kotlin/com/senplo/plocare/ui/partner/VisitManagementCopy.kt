package com.senplo.plocare.ui.partner

import com.senplo.plocare.domain.filter.FilterType
import com.senplo.plocare.ui.consumer.dashboard.formatKoreanMonthDay
import com.senplo.plocare.ui.consumer.dashboard.formatKoreanWeekday
import kotlin.math.roundToInt

/** Figma `김*정• C-1024`, with the bullet spacing normalized. */
internal fun visitTitle(maskedName: String, customerId: String): String = "$maskedName • $customerId"

internal fun visitFullAddress(address: String, detail: String): String =
    listOf(address, detail).filter { it.isNotBlank() }.joinToString(" ")

/** Figma 127:1727 `2/4`: the current visit's place in today's order. */
internal fun currentVisitPosition(views: List<PartnerVisitView>): String? {
    val index = views.indexOfFirst { !it.completed }
    return if (index < 0) null else "${index + 1}/${views.size}"
}

/** Figma 126:1228 / 127:1620 chips: `1단 세디먼트`. */
internal fun stageFilterLabel(stage: Int, type: FilterType): String = "${stage}단 ${type.label}"

internal fun visitTargetFilters(view: PartnerVisitView): List<Pair<Int, FilterType>> {
    val targets = view.visit.targetFilterIds.toSet()
    return view.snapshot?.filters.orEmpty()
        .filter { it.id in targets }
        .sortedBy { it.stage }
        .map { it.stage to it.filterType }
}

/** Figma `최고 소진율 103%`. */
internal fun maxExhaustionLabel(view: PartnerVisitView): String {
    val snapshot = view.snapshot ?: return view.visit.exhaustion
    return "최고 소진율 ${householdMaxExhaustionPercent(snapshot).roundToInt()}%"
}

/** Figma 127:1296: `약 12분 • 고객 희망 오전 → 통화 후 도착 시간 입력`. */
internal fun arrivalGuide(visit: PartnerVisitFixture): String =
    "약 ${visit.travelMinutes}분 • 고객 희망 ${visit.preferredPeriod} → 통화 후 도착 시간 입력"

/** Figma 126:1254 two-line safe-number text. */
internal fun safeNumberLines(visit: PartnerVisitFixture): String =
    "안심번호 ${visit.safeNumber}\n방문 완료 24시간 뒤 만료"

/** Figma 127:1750 cell: `11/5(목) 오후`. */
internal fun shortDateOptionLabel(option: VisitDateOption): String {
    val date = option.date
    return "${date.month.ordinal + 1}/${date.day}(${formatKoreanWeekday(date)}) ${option.period}"
}

/** Figma 127:1875 row title: `11월 5일(목) 오후`. */
internal fun dateOptionLabel(option: VisitDateOption): String =
    "${formatKoreanMonthDay(option.date)}(${formatKoreanWeekday(option.date)}) ${option.period}"

/** Figma 127:1875 row sub-text: `그날 4건 • 오후 꽉 참`. */
internal fun dateOptionDetail(option: VisitDateOption): String =
    listOfNotNull("그날 ${option.dayVisitCount}건", option.note).joinToString(" • ")

/** Figma 127:1610 header note: `24시간 안에 확정해 주세요 • 남은 21시간`. */
internal fun confirmDeadlineLabel(requests: List<PartnerVisitRequest>): String? {
    val hours = requests.minOfOrNull { it.remainingHours } ?: return null
    return "24시간 안에 확정해 주세요 • 남은 ${hours}시간"
}

/** Figma 127:1869 subtitle: `정*아 • 서울 성동구 뚝섬로 27 • 고객 희망 순서`. */
internal fun dateSheetSubtitle(request: PartnerVisitRequest): String =
    "${request.maskedName} • ${request.address} • 고객 희망 순서"

/** The first selectable date, in the customer's preference order. */
internal fun defaultDateOption(request: PartnerVisitRequest): Int? =
    request.options.indexOfFirst { it.available }.takeIf { it >= 0 }

/** A visit position inside the map box, as fractions of its width and height (0..1). */
internal data class MapPoint(val x: Float, val y: Float)

/**
 * Projects visit coordinates into the schematic map shown where the Kakao map is unavailable. North is up;
 * a single point or a zero span lands in the middle of that axis.
 */
internal fun schematicMapPoints(views: List<PartnerVisitView>): List<MapPoint> {
    if (views.isEmpty()) return emptyList()
    val lats = views.map { it.visit.latitude }
    val lngs = views.map { it.visit.longitude }
    fun fraction(value: Double, min: Double, max: Double): Float =
        if (max - min < 1e-9) 0.5f else ((value - min) / (max - min)).toFloat()
    return views.map { view ->
        MapPoint(
            x = fraction(view.visit.longitude, lngs.min(), lngs.max()),
            y = 1f - fraction(view.visit.latitude, lats.min(), lats.max()),
        )
    }
}
