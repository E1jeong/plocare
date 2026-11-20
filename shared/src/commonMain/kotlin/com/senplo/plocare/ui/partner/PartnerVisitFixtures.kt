package com.senplo.plocare.ui.partner

import com.senplo.plocare.domain.filter.DashboardFixtures
import com.senplo.plocare.domain.filter.PurifierDevice
import com.senplo.plocare.domain.filter.FilterType
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

internal const val DEVICE_C2048 = "device-c-2048"
internal const val DEVICE_C4096 = "device-c-4096"

internal data class PartnerFilterStatus(
    val stage: Int,
    val name: String,
    val exhaustionPercent: Int,
    val label: String,
)

internal data class PartnerVisitFixture(
    val customerId: String,
    val maskedName: String,
    val address: String,
    val addressDetail: String,
    // Masked safe number; the relay is not wired yet (backlog).
    val safeNumber: String,
    // Sample travel estimate until Kakao routing exists.
    val travelMinutes: Int,
    /** Customer-preferred half day: `오전` or `오후`. */
    val preferredPeriod: String,
    // Approximate fixture coordinates for the map preview, not geocoded.
    val latitude: Double,
    val longitude: Double,
    val time: String,
    val filtersLabel: String,
    val exhaustion: String,
    val status: String,
    val deviceId: String,
    val targetFilterIds: List<String>,
    val urgent: Boolean = false,
    val completed: Boolean = false,
    val filterStatuses: List<PartnerFilterStatus>,
) {
    val displayName: String get() = "$maskedName · $customerId"
}

// Listed in today's visit order (the order the partner can rearrange).
internal val partnerVisits: List<PartnerVisitFixture> = listOf(
    PartnerVisitFixture(
        customerId = "C-4096",
        maskedName = "최*아",
        address = "서울 광진구 능동로 7",
        addressDetail = "능동하우스 201호",
        safeNumber = "0504-***-4096",
        travelMinutes = 0,
        preferredPeriod = "오전",
        latitude = 37.5428,
        longitude = 127.0738,
        time = "09:00",
        filtersLabel = "RO 멤브레인",
        exhaustion = "교체 완료",
        status = "완료",
        deviceId = DEVICE_C4096,
        targetFilterIds = listOf("c4096-3"),
        completed = true,
        filterStatuses = listOf(
            PartnerFilterStatus(3, "RO 멤브레인", 12, "교체 완료"),
        ),
    ),
    PartnerVisitFixture(
        customerId = "C-1024",
        maskedName = "김*정",
        address = "서울 성동구 성수일로 10",
        addressDetail = "OO아파트 101동 1203호",
        safeNumber = "0504-***-5678",
        travelMinutes = 12,
        preferredPeriod = "오전",
        latitude = 37.5446,
        longitude = 127.0557,
        time = "10:00",
        filtersLabel = "세디먼트 · 프리카본",
        exhaustion = "최고 소진율 112%",
        status = "긴급",
        deviceId = DashboardFixtures.KITCHEN_ID,
        targetFilterIds = listOf("kitchen-1", "kitchen-2"),
        urgent = true,
        filterStatuses = listOf(
            PartnerFilterStatus(1, "세디먼트", 112, "즉시 교체"),
            PartnerFilterStatus(2, "프리카본", 93, "묶음 후보"),
            PartnerFilterStatus(3, "RO 멤브레인", 45, "정상"),
            PartnerFilterStatus(4, "포스트카본", 41, "정상"),
        ),
    ),
    PartnerVisitFixture(
        customerId = "C-2048",
        maskedName = "박*현",
        address = "서울 광진구 아차산로 42",
        addressDetail = "아차산빌 302호",
        safeNumber = "0504-***-2048",
        travelMinutes = 18,
        preferredPeriod = "오후",
        latitude = 37.5405,
        longitude = 127.0695,
        time = "14:00",
        filtersLabel = "RO 멤브레인 · 포스트카본",
        exhaustion = "소진율 88%",
        status = "예정",
        deviceId = DEVICE_C2048,
        // The membrane target is the Figma 16/18 shortage sample (`UF 1개 부족`).
        targetFilterIds = listOf("c2048-3", "c2048-4"),
        filterStatuses = listOf(
            PartnerFilterStatus(3, "RO 멤브레인", 79, "정상"),
            PartnerFilterStatus(4, "포스트카본", 88, "교체 임박"),
        ),
    ),
    PartnerVisitFixture(
        customerId = "C-3072",
        maskedName = "이*수",
        address = "서울 성동구 왕십리로 18",
        addressDetail = "왕십리타워 7층",
        safeNumber = "0504-***-3072",
        travelMinutes = 9,
        preferredPeriod = "오후",
        latitude = 37.5472,
        longitude = 127.0447,
        time = "16:00",
        filtersLabel = "세디먼트",
        exhaustion = "소진율 91%",
        status = "예정",
        deviceId = DashboardFixtures.OFFICE_ID,
        targetFilterIds = listOf("office-1"),
        filterStatuses = listOf(
            PartnerFilterStatus(1, "세디먼트", 91, "교체 임박"),
        ),
    ),
)

internal fun partnerDevices(now: Instant, timeZone: TimeZone): List<PurifierDevice> {
    val today = now.toLocalDateTime(timeZone).date
    val kitchen = DashboardFixtures.kitchenPurifier(today, now)
    val office = DashboardFixtures.officePurifier(today, now)
    return listOf(
        kitchen,
        kitchen.copy(
            id = DEVICE_C2048,
            nickname = "아차산 정수기",
            filters = kitchen.filters.map { cartridge -> cartridge.copy(id = "c2048-${cartridge.stage}") },
            visitTicket = null,
            replacementHistory = emptyList(),
        ),
        office,
        kitchen.copy(
            id = DEVICE_C4096,
            nickname = "능동 정수기",
            filters = kitchen.filters.map { cartridge -> cartridge.copy(id = "c4096-${cartridge.stage}") },
            visitTicket = null,
            replacementHistory = emptyList(),
        ),
    )
}

internal fun findPartnerVisit(
    customerId: String,
    visits: List<PartnerVisitFixture> = partnerVisits,
): PartnerVisitFixture? {
    val needle = customerId.trim()
    if (needle.isEmpty()) return null
    return visits.find { it.customerId.equals(needle, ignoreCase = true) }
}

/** One customer-preferred date in a pending request (Figma 127:1869 rows). */
internal data class VisitDateOption(
    val date: LocalDate,
    /** `오전` or `오후`. */
    val period: String,
    val dayVisitCount: Int,
    val note: String? = null,
    val available: Boolean = true,
)

/** A new visit request waiting for the partner's date confirmation (Figma 15 `확정 대기`). */
internal data class PartnerVisitRequest(
    val id: String,
    val customerId: String,
    val maskedName: String,
    val address: String,
    val addressDetail: String,
    val filters: List<Pair<Int, FilterType>>,
    /** Three dates in the customer's preference order. */
    val options: List<VisitDateOption>,
    val remainingHours: Int,
)

internal fun partnerVisitRequests(today: LocalDate): List<PartnerVisitRequest> = listOf(
    PartnerVisitRequest(
        id = "R-5120",
        customerId = "C-5120",
        maskedName = "정*아",
        address = "서울 성동구 뚝섬로 27",
        addressDetail = "OO아파트 101동 1203호",
        filters = listOf(1 to FilterType.SEDIMENT, 2 to FilterType.PRE_CARBON),
        options = listOf(
            VisitDateOption(today.plus(6, DateTimeUnit.DAY), "오후", 4, note = "오후 꽉 참", available = false),
            VisitDateOption(today.plus(8, DateTimeUnit.DAY), "오전", 2, note = "성동구 1건"),
            VisitDateOption(today.plus(10, DateTimeUnit.DAY), "오후", 1),
        ),
        remainingHours = 21,
    ),
)
