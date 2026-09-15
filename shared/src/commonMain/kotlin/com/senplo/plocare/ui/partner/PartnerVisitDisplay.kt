package com.senplo.plocare.ui.partner

import com.senplo.plocare.domain.filter.DashboardSnapshot
import com.senplo.plocare.domain.filter.PurifierDevice
import com.senplo.plocare.domain.filter.buildDashboardSnapshot
import kotlinx.datetime.TimeZone
import kotlin.math.roundToInt
import kotlin.time.Instant

internal data class PartnerVisitView(
    val visit: PartnerVisitFixture,
    val snapshot: DashboardSnapshot?,
    val filtersLabel: String,
    val exhaustionLabel: String,
    val status: String,
    val urgent: Boolean,
) {
    val customerId: String get() = visit.customerId
    val displayName: String get() = visit.displayName
    val address: String get() = visit.address
    val time: String get() = visit.time
    val completed: Boolean get() = visit.completed
}

internal fun householdMaxExhaustionPercent(snapshot: DashboardSnapshot): Double =
    snapshot.filters.maxOfOrNull { it.exhaustionPercent } ?: 0.0

internal fun householdIsUrgent(snapshot: DashboardSnapshot): Boolean =
    snapshot.filters.any { it.exhaustionPercent > 100.0 }

internal fun partnerVisitView(
    visit: PartnerVisitFixture,
    device: PurifierDevice?,
    now: Instant,
    timeZone: TimeZone,
): PartnerVisitView {
    val snapshot = device?.let { buildDashboardSnapshot(it, now, timeZone) }
    if (visit.completed) {
        return PartnerVisitView(
            visit = visit,
            snapshot = snapshot,
            filtersLabel = visitTargetNames(visit, device) ?: visit.filtersLabel,
            exhaustionLabel = "교체 완료",
            status = "완료",
            urgent = false,
        )
    }
    if (snapshot == null) {
        return PartnerVisitView(
            visit = visit,
            snapshot = null,
            filtersLabel = visit.filtersLabel,
            exhaustionLabel = visit.exhaustion,
            status = visit.status,
            urgent = visit.urgent,
        )
    }
    val percent = householdMaxExhaustionPercent(snapshot)
    val urgent = householdIsUrgent(snapshot)
    return PartnerVisitView(
        visit = visit,
        snapshot = snapshot,
        filtersLabel = visitTargetNames(visit, snapshot.device) ?: visit.filtersLabel,
        exhaustionLabel = "최고 소진율 ${percent.roundToInt()}%",
        status = if (urgent) "긴급" else "예정",
        urgent = urgent,
    )
}

internal fun partnerVisitViews(
    visits: List<PartnerVisitFixture>,
    devices: List<PurifierDevice>,
    now: Instant,
    timeZone: TimeZone,
): List<PartnerVisitView> {
    val byId = devices.associateBy { it.id }
    return visits.map { visit -> partnerVisitView(visit, byId[visit.deviceId], now, timeZone) }
}

private fun visitTargetNames(visit: PartnerVisitFixture, device: PurifierDevice?): String? {
    if (device == null) return null
    val names = device.filters.filter { it.id in visit.targetFilterIds }.map { it.name }
    return names.takeIf { it.isNotEmpty() }?.joinToString(" · ")
}
