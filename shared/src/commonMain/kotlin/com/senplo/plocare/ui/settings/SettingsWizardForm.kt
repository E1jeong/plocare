package com.senplo.plocare.ui.settings

import com.senplo.plocare.domain.filter.FilterLifeCalculator

internal enum class SettingsStep(val title: String, val caption: String) {
    BLE("BLE 페어링", "근처 PloCare 센서를 선택해 연결하세요."),
    MODEL("모델·필터 프리셋", "정수기 브랜드와 필터 단수를 확인하세요."),
    HYDRAULIC("배관·밸브·수압", "Kv 미리보기는 읽기 전용입니다."),
    CONNECTIVITY("통신 연결", "가정용 Wi-Fi 또는 상업용 NB-IoT를 설정하세요."),
    FLOW_TEST("1분 유량 테스트", "측정값을 입력하거나 기본 Kv를 유지하고 건너뛰세요."),
    SUMMARY("요약·동기화", "설정을 확인하고 기기에 저장합니다."),
}

internal data class StubBleDevice(
    val id: String,
    val name: String,
    val rssi: Int,
)

internal val stubBleDevices = listOf(
    StubBleDevice("ble-a12", "PloCare Sensor A12", -42),
    StubBleDevice("ble-b08", "PloCare Sensor B08", -61),
)

internal val settingsBrands = listOf("Cuckoo", "Coway", "SK매직", "LG", "기타")
internal val pipeSizes = listOf("1/4\"", "3/8\"", "1/2\"")
internal val valveTypes = listOf("감압 고정형", "조절형 피팅")

internal data class SettingsDraft(
    val step: SettingsStep = SettingsStep.BLE,
    val connectedDeviceId: String? = null,
    val connectedDeviceName: String? = null,
    val brand: String = "Cuckoo",
    val model: String = "CP-IN900",
    val stageCount: Int = 4,
    val pipeSize: String = "1/4\"",
    val valveType: String = "감압 고정형",
    val pressureKgf: String = "2.0",
    val householdMode: Boolean = true,
    val wifiSsid: String = "",
    val wifiPassword: String = "",
    val nbiotChecked: Boolean = false,
    val measuredMl: String = "",
    val skippedFlowTest: Boolean = false,
)

internal fun SettingsDraft.pressureOrNull(): Double? = pressureKgf.trim().toDoubleOrNull()

internal fun SettingsDraft.previewKv(): Double {
    val pressure = pressureOrNull() ?: 2.0
    return FilterLifeCalculator.previewKv(pipeSize, pressure)
}

internal fun SettingsDraft.calibratedKv(): Double {
    if (skippedFlowTest) return previewKv()
    val measured = measuredMl.trim().toDoubleOrNull() ?: return previewKv()
    return FilterLifeCalculator.calibrateKv(previewKv(), measured)
}

internal fun SettingsStep.nextOrNull(): SettingsStep? {
    val values = SettingsStep.entries
    val index = values.indexOf(this)
    return values.getOrNull(index + 1)
}

internal fun SettingsStep.previousOrNull(): SettingsStep? {
    val values = SettingsStep.entries
    val index = values.indexOf(this)
    return values.getOrNull(index - 1)
}

internal fun validateSettingsStep(draft: SettingsDraft): String? = when (draft.step) {
    SettingsStep.BLE ->
        if (draft.connectedDeviceName.isNullOrBlank()) "연결할 센서를 선택해 주세요." else null
    SettingsStep.MODEL -> when {
        draft.brand.isBlank() || draft.model.isBlank() -> "브랜드와 모델을 입력해 주세요."
        draft.stageCount !in 1..5 -> "필터 단수는 1~5단만 설정할 수 있습니다."
        else -> null
    }
    SettingsStep.HYDRAULIC -> {
        val pressure = draft.pressureOrNull()
        when {
            pressure == null -> "수압을 숫자로 입력해 주세요."
            pressure < 0.5 || pressure > 5.0 -> "수압은 0.5~5.0 kgf/cm² 범위입니다."
            draft.pipeSize !in pipeSizes -> "배관 규격을 선택해 주세요."
            else -> null
        }
    }
    SettingsStep.CONNECTIVITY -> when {
        draft.householdMode && draft.wifiSsid.trim().isEmpty() -> "Wi-Fi 이름을 입력해 주세요."
        draft.householdMode && draft.wifiPassword.isEmpty() -> "Wi-Fi 비밀번호를 입력해 주세요."
        !draft.householdMode && !draft.nbiotChecked -> "NB-IoT 신호 확인을 완료해 주세요."
        else -> null
    }
    SettingsStep.FLOW_TEST -> {
        if (draft.skippedFlowTest) {
            null
        } else {
            val measured = draft.measuredMl.trim().toDoubleOrNull()
            when {
                measured == null -> "측정 용량(mL)을 입력하거나 테스트를 건너뛰세요."
                measured < FilterLifeCalculator.FLOW_TEST_MIN_ML ||
                    measured > FilterLifeCalculator.FLOW_TEST_MAX_ML ->
                    "측정 용량은 500~2,000 mL만 허용됩니다."
                else -> null
            }
        }
    }
    SettingsStep.SUMMARY -> null
}

internal fun SettingsDraft.advance(): SettingsDraft {
    val next = step.nextOrNull() ?: return this
    return copy(step = next)
}

internal fun SettingsDraft.back(): SettingsDraft {
    val previous = step.previousOrNull() ?: return this
    return copy(step = previous)
}
