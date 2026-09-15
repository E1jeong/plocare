package com.senplo.plocare.ui.partner

import com.senplo.plocare.domain.filter.DashboardSnapshot
import com.senplo.plocare.domain.filter.FilterColorLevel
import com.senplo.plocare.domain.filter.FilterSnapshot
import com.senplo.plocare.domain.filter.PurifierDevice
import kotlin.math.round

internal enum class ReplacementType(val label: String) {
    SCHEDULED("정기 교체"),
    URGENT_LEAK("긴급 누수"),
    BUNDLE("묶음 교체"),
}

internal data class WorkInformationDraft(
    val selectedFilterIds: Set<String>,
    val type: ReplacementType?,
    val barcode: String,
    val note: String,
    val sensorClamped: Boolean,
    val noLeak: Boolean,
    val commsVerified: Boolean,
)

internal data class BaselineSafetyPreview(
    val updated: List<Pair<String, Double>>,
    val unchangedLabels: List<String>,
    val totalCumulativeL: Double,
)

internal fun validateWorkInformation(draft: WorkInformationDraft): String? = when {
    draft.selectedFilterIds.isEmpty() -> "교체한 필터를 선택해 주세요."
    draft.type == null -> "교체 유형을 선택해 주세요."
    !draft.sensorClamped || !draft.noLeak || !draft.commsVerified ->
        "하드웨어 점검을 모두 확인해 주세요."
    else -> null
}

internal fun baselineSafetyPreview(
    device: PurifierDevice,
    selectedIds: Set<String>,
): BaselineSafetyPreview = BaselineSafetyPreview(
    updated = device.filters.filter { it.id in selectedIds }.map { filter ->
        "${filter.stage}단 ${filter.name}" to device.totalCumulativeL
    },
    unchangedLabels = device.filters.filter { it.id !in selectedIds }.map { filter ->
        "${filter.stage}단 ${filter.name}"
    },
    totalCumulativeL = device.totalCumulativeL,
)

internal fun formatCumulativeL(value: Double): String {
    val amount = round(value).toInt()
    val sign = if (amount < 0) "-" else ""
    val digits = kotlin.math.abs(amount).toString()
    val grouped = digits.reversed().chunked(3).joinToString(",").reversed()
    return "$sign$grouped L"
}

internal fun partnerFilterLabel(snapshot: DashboardSnapshot, filter: FilterSnapshot): String = when {
    filter.colorLevel == FilterColorLevel.EXHAUSTED -> "즉시 교체"
    snapshot.bundle?.filterIds?.contains(filter.id) == true -> "묶음 후보"
    filter.colorLevel == FilterColorLevel.REPLACE_SOON -> "교체 임박"
    else -> "정상"
}

internal fun workConfirmationDetail(): String =
    "선택한 필터의 기준점만 이 기기에서 갱신합니다. 총 누적 통수량과 다른 필터는 그대로입니다. 서버에는 저장되지 않습니다."
