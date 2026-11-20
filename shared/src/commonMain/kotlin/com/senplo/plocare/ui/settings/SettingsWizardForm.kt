package com.senplo.plocare.ui.settings

import com.senplo.plocare.domain.filter.FilterType
import com.senplo.plocare.domain.filter.MakerGroup
import com.senplo.plocare.scanner.ScanFrame
import com.senplo.plocare.scanner.parseWifiQr

/** Figma 4-step wizard (user decision 2026-11-19): 79:2246 → 79:3086 → 79:3305 / 94:4155 → 94:4460. */
internal enum class SettingsStep(val title: String) {
    PURIFIER("정수기 설정"),
    SENSOR("센서 연결"),
    MODEL("정수기 연결"),
    NETWORK("통신 연결"),
}

internal data class ModelFilterTag(val stage: Int, val name: String, val type: FilterType) {
    val label: String get() = "${stage}단 $name"
}

internal data class PurifierModelChoice(
    val makerGroup: MakerGroup,
    val makerName: String,
    val modelName: String,
    val filters: List<ModelFilterTag>,
)

/**
 * Figma 94:4155 sample model. There is no model catalog yet, so manual selection and any scanned model
 * code resolve to this sample (user decision 2026-11-20).
 */
internal val sampleModelChoice = PurifierModelChoice(
    makerGroup = MakerGroup.MAJOR,
    makerName = "제조사 A",
    modelName = "A-IN900",
    filters = listOf(
        ModelFilterTag(1, "세디먼트", FilterType.SEDIMENT),
        ModelFilterTag(2, "프리카본", FilterType.PRE_CARBON),
        ModelFilterTag(3, "포스트카본", FilterType.POST_CARBON),
        ModelFilterTag(4, "UF 멤브레인", FilterType.MEMBRANE),
    ),
)

internal data class SettingsDraft(
    val step: SettingsStep = SettingsStep.PURIFIER,
    /** No pressure picker exists yet (Figma has no option list), so this stays null for now. */
    val pressureKgf: Double? = null,
    val sensorCode: String? = null,
    val scannedModelCode: String? = null,
    val model: PurifierModelChoice? = null,
    val wifiSsid: String = "",
    val wifiPassword: String = "",
)

/** Step 1 has no required input; each scan step needs its code before moving on. */
internal fun SettingsDraft.canAdvance(): Boolean = when (step) {
    SettingsStep.PURIFIER -> true
    SettingsStep.SENSOR -> sensorCode != null
    SettingsStep.MODEL -> model != null
    SettingsStep.NETWORK -> wifiSsid.isNotBlank()
}

internal enum class ScanTarget(val frame: ScanFrame) {
    SENSOR(ScanFrame.QR),
    MODEL(ScanFrame.BARCODE),
    WIFI(ScanFrame.QR),
}

internal fun SettingsStep.scanTarget(): ScanTarget? = when (this) {
    SettingsStep.PURIFIER -> null
    SettingsStep.SENSOR -> ScanTarget.SENSOR
    SettingsStep.MODEL -> ScanTarget.MODEL
    SettingsStep.NETWORK -> ScanTarget.WIFI
}

/** Applies scanned text for [target]; null when the text is unusable. */
internal fun SettingsDraft.applyScan(target: ScanTarget, raw: String): SettingsDraft? {
    val text = raw.trim()
    if (text.isEmpty()) return null
    return when (target) {
        ScanTarget.SENSOR -> copy(sensorCode = text)
        ScanTarget.MODEL -> copy(scannedModelCode = text, model = sampleModelChoice)
        ScanTarget.WIFI -> parseWifiQr(text)?.let { wifi ->
            copy(wifiSsid = wifi.ssid, wifiPassword = wifi.password)
        }
    }
}

internal fun SettingsDraft.advance(): SettingsDraft {
    val next = SettingsStep.entries.getOrNull(step.ordinal + 1) ?: return this
    return copy(step = next)
}

internal fun SettingsDraft.back(): SettingsDraft {
    val previous = SettingsStep.entries.getOrNull(step.ordinal - 1) ?: return this
    return copy(step = previous)
}

internal const val UNKNOWN_VALUE = "알 수 없음"

/** Step-1 summary rows (79:2246): label to value, `알 수 없음` until a model is set. */
internal fun SettingsDraft.modelSummaryRows(): List<Pair<String, String>> {
    val model = model
    return listOf(
        "제조사 구분" to (model?.makerGroup?.label ?: UNKNOWN_VALUE),
        "제조사" to (model?.makerName ?: UNKNOWN_VALUE),
        "모델명" to (model?.modelName ?: UNKNOWN_VALUE),
        "필터 구성" to (model?.let { "${it.filters.size}개" } ?: UNKNOWN_VALUE),
    )
}

internal fun PurifierModelChoice.recognizedHeadline(): String = "$makerName• $modelName 으로 인식했어요"

internal fun PurifierModelChoice.filterCountLine(): String = "필터 ${filters.size}개 구성이 자동으로 들어갑니다"
