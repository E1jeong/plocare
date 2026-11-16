package com.senplo.plocare.domain.filter

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import kotlin.time.Instant

const val MIN_FILTER_STAGES = 1
const val MAX_FILTER_STAGES = 10

enum class MakerGroup(val label: String) {
    MAJOR("대기업"),
    OTHER("그 외 제조사"),
}

enum class FiltrationType(val label: String) {
    DIRECT("직수"),
    TANK("탱크형"),
    ;

    /** Tank-type cumulative-only rule (`한계6`): tank purifiers must never display single-pour readouts. */
    val supportsSinglePourReadout: Boolean
        get() = this == DIRECT
}

enum class ManagementType(val label: String) {
    SELF("자가관리"),
    VISIT("방문관리"),
    VISIT_2M("방문주기 2개월"),
    VISIT_4M("방문주기 4개월"),
}

enum class ReplacementReason(val label: String) {
    CAPACITY("용량 도달 예상"),
    CYCLE_EXPIRY("교환주기 만료"),
}

enum class FilterType(val label: String) {
    SEDIMENT("세디먼트"),
    PRE_CARBON("프리카본"),
    POST_CARBON("포스트카본"),
    MEMBRANE("멤브레인"),
    COMPOSITE("복합필터"),
    UNSPECIFIED("기타"),
    ;

    companion object {
        fun fromName(name: String): FilterType {
            val normalized = name.lowercase()
            return when {
                "세디" in normalized || "sediment" in normalized -> SEDIMENT
                "멤브레인" in normalized || "멤프레인" in normalized ||
                    "ro" in normalized || "uf" in normalized || "나노" in normalized -> MEMBRANE
                "포스트" in normalized || "후처리" in normalized || "실버" in normalized -> POST_CARBON
                "프리카본" in normalized || "카본" in normalized || "전처리" in normalized -> PRE_CARBON
                else -> UNSPECIFIED
            }
        }
    }
}

data class FilterCartridge(
    val id: String,
    val stage: Int,
    val name: String,
    val ratedCapacityL: Double,
    val baselineL: Double,
    val filterType: FilterType = FilterType.fromName(name),
    val cycleMonths: Int? = null,
    val installedOrReplacedOn: LocalDate? = null,
    val cycleExpiryDate: LocalDate? = null,
) {
    val hasRatedCapacity: Boolean
        get() = ratedCapacityL > 0.0

    fun effectiveCycleExpiryDate(deviceInstalledOn: LocalDate): LocalDate? =
        cycleExpiryDate ?: cycleMonths?.let { months ->
            (installedOrReplacedOn ?: deviceInstalledOn).plus(months, DateTimeUnit.MONTH)
        }
}

data class FilterStagePreset(
    val stage: Int,
    val name: String,
    val filterType: FilterType = FilterType.fromName(name),
    val cycleMonths: Int,
    val ratedCapacityL: Double = 0.0,
)

data class PurifierModelCatalogEntry(
    val id: String,
    val makerGroup: MakerGroup,
    val makerName: String,
    val modelName: String,
    val modelCode: String,
    val filtrationType: FiltrationType,
    val presetsByManagement: Map<ManagementType, List<FilterStagePreset>>,
) {
    fun presetsFor(managementType: ManagementType): List<FilterStagePreset> =
        presetsByManagement[managementType]
            ?: presetsByManagement.values.firstOrNull().orEmpty()
}

data class VisitTicket(
    val scheduledDate: LocalDate,
    val hour: Int,
    val minute: Int,
    val technicianMaskedName: String?,
    val targetFilterIds: List<String>,
    val windowEndHour: Int? = null,
    val contact: String? = null,
    val address: String? = null,
    val notes: String? = null,
)

enum class ReplacementSource {
    SELF,
    PARTNER,
}

data class FilterReplacementRecord(
    val replacedOn: LocalDate,
    val filterIds: List<String>,
    val source: ReplacementSource,
    val technicianMaskedName: String? = null,
    val cumulativeL: Double,
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
    val replacementHistory: List<FilterReplacementRecord> = emptyList(),
    val makerGroup: MakerGroup = MakerGroup.MAJOR,
    val makerName: String = "",
    val modelCode: String = "",
    val filtrationType: FiltrationType = FiltrationType.DIRECT,
    val managementType: ManagementType = ManagementType.SELF,
    val prvPressureKgf: Double = 2.0,
) {
    val supportsSinglePourReadout: Boolean
        get() = filtrationType.supportsSinglePourReadout
}

fun PurifierDevice.withSelfReplaced(
    filterId: String,
    replacedOn: LocalDate? = null,
): PurifierDevice = withReplacedFilters(listOf(filterId), replacedOn = replacedOn)

fun PurifierDevice.withReplacedFilters(
    filterIds: Collection<String>,
    replacedOn: LocalDate? = null,
): PurifierDevice {
    val ids = filterIds.toSet()
    require(ids.isNotEmpty()) { "No filters selected" }
    val known = filters.map { it.id }.toSet()
    val unknown = ids - known
    require(unknown.isEmpty()) { "Unknown filter: $unknown" }
    return copy(
        filters = filters.map { filter ->
            if (filter.id !in ids) {
                filter
            } else {
                val updatedCycleExpiry = if (replacedOn != null && filter.cycleMonths != null) {
                    replacedOn.plus(filter.cycleMonths, DateTimeUnit.MONTH)
                } else {
                    filter.cycleExpiryDate
                }
                filter.copy(
                    baselineL = totalCumulativeL,
                    installedOrReplacedOn = replacedOn ?: filter.installedOrReplacedOn,
                    cycleExpiryDate = updatedCycleExpiry,
                )
            }
        },
    )
}

fun PurifierDevice.withTelemetryAt(at: Instant): PurifierDevice = copy(lastTelemetryAt = at)
