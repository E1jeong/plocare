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
    val lastTelemetryAt: Instant,
    val filters: List<FilterCartridge>,
    val visitTicket: VisitTicket? = null,
)

fun PurifierDevice.withSelfReplaced(filterId: String): PurifierDevice {
    require(filters.any { it.id == filterId }) { "Unknown filter: $filterId" }
    return copy(
        filters = filters.map { filter ->
            if (filter.id == filterId) filter.copy(baselineL = totalCumulativeL) else filter
        },
    )
}

fun PurifierDevice.withTelemetryAt(at: Instant): PurifierDevice = copy(lastTelemetryAt = at)
