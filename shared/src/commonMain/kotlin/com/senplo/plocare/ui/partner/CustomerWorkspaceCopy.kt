package com.senplo.plocare.ui.partner

import com.senplo.plocare.domain.filter.DashboardSnapshot
import com.senplo.plocare.domain.filter.FilterColorLevel
import com.senplo.plocare.domain.filter.FilterType
import kotlin.math.roundToInt

/** One Figma 146:2969 row: `1단 세디먼트` + `즉시 교체` badge + `103%`. */
internal data class WorkspaceFilterRow(
    val label: String,
    val badge: String,
    val percent: Int,
    /** Red badge and percentage (Figma `즉시 교체`); every other state uses the primary badge. */
    val exhausted: Boolean,
)

/**
 * Every filter of the visit's purifier, named by stage and filter type like the other Partner chips
 * (`2단 카본 블록` in Figma becomes `2단 프리카본`). Without a linked purifier, the visit's own sample rows are used.
 */
internal fun workspaceFilterRows(snapshot: DashboardSnapshot?, visit: PartnerVisitFixture): List<WorkspaceFilterRow> {
    if (snapshot != null) {
        return snapshot.filters.map { filter ->
            WorkspaceFilterRow(
                label = stageFilterLabel(filter.stage, filter.filterType),
                badge = partnerFilterLabel(snapshot, filter),
                percent = filter.exhaustionPercent.roundToInt(),
                exhausted = filter.colorLevel == FilterColorLevel.EXHAUSTED,
            )
        }
    }
    return visit.filterStatuses.map { status ->
        WorkspaceFilterRow(
            label = stageFilterLabel(status.stage, FilterType.fromName(status.name)),
            badge = status.label,
            percent = status.exhaustionPercent,
            exhausted = status.exhaustionPercent >= 100,
        )
    }
}

/** Figma 146:2939 banner kind: urgent unless the household has no exhausted filter or the visit is done. */
internal fun workspaceVisitBadge(snapshot: DashboardSnapshot?, visit: PartnerVisitFixture): VisitBadgeKind = when {
    visit.completed -> VisitBadgeKind.DONE
    (snapshot?.let(::householdIsUrgent) ?: visit.urgent) -> VisitBadgeKind.URGENT
    else -> VisitBadgeKind.NORMAL
}
