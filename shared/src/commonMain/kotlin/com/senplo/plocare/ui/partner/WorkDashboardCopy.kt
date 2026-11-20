package com.senplo.plocare.ui.partner

import com.senplo.plocare.domain.filter.FilterType
import com.senplo.plocare.ui.consumer.dashboard.formatKoreanMonthDay
import com.senplo.plocare.ui.consumer.dashboard.formatKoreanWeekday
import kotlinx.datetime.LocalDate

/** Local work estimate per replaced filter until dispatch data exists. */
internal const val MINUTES_PER_FILTER = 10

internal data class AssignmentRow(
    val order: Int,
    val view: PartnerVisitView,
    val district: String,
    val badge: VisitBadgeKind,
    val current: Boolean,
)

internal data class VehicleStockItem(
    val type: FilterType,
    val loaded: Int,
    val required: Int,
) {
    val shortage: Int get() = (required - loaded).coerceAtLeast(0)
}

/** Figma 129:2453 `예정 / 긴급 / 완료`. */
internal data class WorkSummary(val scheduled: Int, val urgent: Int, val done: Int)

internal fun workSummary(views: List<PartnerVisitView>): WorkSummary {
    val open = views.filterNot { it.completed }
    return WorkSummary(
        scheduled = open.count { !it.urgent },
        urgent = open.count { it.urgent },
        done = views.count { it.completed },
    )
}

/** Figma 129:2473: `1시간 40분`. */
internal fun workDurationLabel(minutes: Int): String {
    val hours = minutes / 60
    val rest = minutes % 60
    return when {
        hours == 0 -> "${rest}분"
        rest == 0 -> "${hours}시간"
        else -> "${hours}시간 ${rest}분"
    }
}

/** Figma 136:2661: `10월 30일(금)`. */
internal fun assignmentDateLabel(date: LocalDate): String =
    "${formatKoreanMonthDay(date)}(${formatKoreanWeekday(date)})"

/** `서울 성동구 성수일로 10` → `성동구`. */
internal fun visitDistrict(address: String): String =
    address.trim().split(Regex("\\s+")).getOrNull(1).orEmpty()

/**
 * Figma 129:2494 lists the day's visits in visit order (the order the partner can rearrange in 15);
 * the first open visit is the current one (row fill).
 */
internal fun assignmentRows(views: List<PartnerVisitView>): List<AssignmentRow> {
    val currentId = currentVisit(views)?.customerId
    return views.mapIndexed { index, view ->
        AssignmentRow(
            order = index + 1,
            view = view,
            district = visitDistrict(view.address),
            badge = visitBadgeKind(view),
            current = view.customerId == currentId,
        )
    }
}

/** The first open visit in visit order: the 16 highlighted row and the 14 `지금 방문` card. */
internal fun currentVisit(views: List<PartnerVisitView>): PartnerVisitView? = views.firstOrNull { !it.completed }

/** Figma 129:2595 grid: the four filter types, stocked against today's open-visit needs. */
internal fun vehicleStockItems(required: Map<FilterType, Int>, stocked: Map<FilterType, Int>): List<VehicleStockItem> =
    defaultStockTypes.map { type ->
        VehicleStockItem(type, loaded = stocked[type] ?: 0, required = required[type] ?: 0)
    }

/** Figma 146:2676: `UF 1개 부족 • 출발 전 재고 관리에서 확인하세요`; null when nothing is short. */
internal fun stockShortageMessage(items: List<VehicleStockItem>): String? {
    val short = items.filter { it.shortage > 0 }
    if (short.isEmpty()) return null
    val parts = short.joinToString(", ") { "${it.type.label} ${it.shortage}개" }
    return "$parts 부족 • 출발 전 재고 관리에서 확인하세요"
}
