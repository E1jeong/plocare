package com.senplo.plocare.ui.partner

import com.senplo.plocare.domain.filter.DashboardSnapshot
import com.senplo.plocare.domain.filter.FilterLifeCalculator
import kotlin.time.Instant

internal data class SensorDiagnosticsDraft(
    val clampSeated: Boolean,
    val noLeak: Boolean,
    val commsObserved: Boolean,
)

internal data class SensorDiagnosticsView(
    val deviceNickname: String,
    val deviceId: String,
    val installedOn: String,
    val telemetryLabel: String,
    val telemetryFresh: Boolean,
    val wifiLabel: String,
    val nbiotLabel: String,
    val signalLabel: String,
)

internal fun sensorDiagnosticsView(
    snapshot: DashboardSnapshot,
    now: Instant,
): SensorDiagnosticsView {
    val device = snapshot.device
    val elapsed = now - device.lastTelemetryAt
    val minutes = elapsed.inWholeMinutes
    val hours = elapsed.inWholeHours
    val stale = snapshot.sensorStale || hours >= FilterLifeCalculator.SENSOR_STALE_HOURS
    val telemetryLabel = when {
        stale -> "24시간 이상 미수신"
        minutes < 1L -> "방금 전 수신 기록"
        minutes < 60L -> "${minutes}분 전 수신 기록"
        else -> "${hours}시간 전 수신 기록"
    }
    return SensorDiagnosticsView(
        deviceNickname = device.nickname,
        deviceId = device.id,
        installedOn = device.installedOn.toString(),
        telemetryLabel = telemetryLabel,
        telemetryFresh = !stale,
        wifiLabel = if (stale) "24시간 이상 미수신" else "마지막 수신 기록 있음",
        nbiotLabel = "NB-IoT 모뎀 점검은 연결되지 않았습니다",
        signalLabel = "실시간 신호 강도는 측정하지 않습니다",
    )
}

internal fun validateSensorDiagnostics(draft: SensorDiagnosticsDraft): String? = when {
    !draft.clampSeated || !draft.noLeak || !draft.commsObserved ->
        "현장 육안 확인을 모두 표시해 주세요."
    else -> null
}

internal fun diagnosticsConfirmationTitle(): String = "점검 내용을 확인했습니다."

internal fun diagnosticsConfirmationDetail(): String =
    "로컬 점검 미리보기입니다. 실시간 센서 값은 읽지 않습니다. Wi-Fi·NB-IoT 모뎀과 정전용량 신호 강도는 연결되지 않았습니다. 서버에 저장되지 않습니다."
