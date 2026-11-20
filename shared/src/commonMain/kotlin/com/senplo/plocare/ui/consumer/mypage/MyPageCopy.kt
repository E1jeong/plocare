package com.senplo.plocare.ui.consumer.mypage

import com.senplo.plocare.domain.filter.PurifierDevice

/**
 * Figma 283:6603: `제조사 A • A-IN900 4개 필터 • 센서 연결됨`. The sensor part stays `센서 연결됨` like the
 * Dashboard badge until the not-connected design arrives (2026-11-20 user decision).
 */
internal fun purifierSummary(device: PurifierDevice): String {
    val model = listOf(device.makerName, device.modelCode)
        .filter { it.isNotBlank() }
        .joinToString(" • ")
    val filters = "${device.filters.size}개 필터"
    val head = if (model.isEmpty()) filters else "$model $filters"
    return "$head • 센서 연결됨"
}
