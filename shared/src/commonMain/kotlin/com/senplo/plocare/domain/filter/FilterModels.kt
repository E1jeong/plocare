package com.senplo.plocare.domain.filter

import kotlinx.datetime.LocalDate
import kotlin.time.Instant

data class FilterCartridge(
    val id: String,
    val stage: Int,
    val name: String,
    val ratedCapacityL: Double,
    val baselineL: Double,
)

data class VisitTicket(
    val scheduledDate: LocalDate,
    val hour: Int,
    val minute: Int,
    val technicianMaskedName: String?,
    val targetFilterIds: List<String>,
)

data class PurifierDevice(
    val id: String,
    val nickname: String,
    val installedOn: LocalDate,
    val householdSize: Int,
    val totalCumulativeL: Double,
    val dailyAvgL: Double,
    val recentDailyUsageL: List<Double>,
    val lastTelemetryAt: Instant,
    val filters: List<FilterCartridge>,
    val visitTicket: VisitTicket? = null,
)

fun PurifierDevice.withSelfReplaced(filterId: String): PurifierDevice =
    withReplacedFilters(listOf(filterId))

fun PurifierDevice.withReplacedFilters(filterIds: Collection<String>): PurifierDevice {
    val ids = filterIds.toSet()
    require(ids.isNotEmpty()) { "No filters selected" }
    val known = filters.map { it.id }.toSet()
    val unknown = ids - known
    require(unknown.isEmpty()) { "Unknown filter: $unknown" }
    return copy(
        filters = filters.map { filter ->
            if (filter.id in ids) filter.copy(baselineL = totalCumulativeL) else filter
        },
    )
}

fun PurifierDevice.withTelemetryAt(at: Instant): PurifierDevice = copy(lastTelemetryAt = at)
