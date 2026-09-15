package com.senplo.plocare.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.senplo.plocare.domain.filter.DashboardFixtures
import com.senplo.plocare.domain.filter.FilterLifeCalculator
import com.senplo.plocare.navigation.Route
import com.senplo.plocare.ui.theme.PloCareColor
import kotlin.math.round

@Composable
fun SettingsWizardScreen(
    context: Route.DeviceSettings,
    onBack: () -> Unit,
    onFinished: () -> Unit,
) {
    var draft by remember { mutableStateOf(SettingsDraft()) }
    var stepError by remember { mutableStateOf<String?>(null) }
    var synced by remember { mutableStateOf(false) }
    val deviceNickname = remember(context.deviceId, context.deviceNickname) {
        context.deviceNickname.ifBlank {
            when (context.deviceId) {
                DashboardFixtures.KITCHEN_ID -> "우리 집 주방 정수기"
                DashboardFixtures.OFFICE_ID -> "사무실 직수 정수기"
                else -> context.deviceId
            }
        }
    }
    val contextLabel = if (context.isPartnerContext) {
        "파트너 · ${context.customerId}"
    } else {
        "소비자"
    }
    val headingDetail = if (context.isPartnerContext) {
        "고객 ${context.customerId} · $deviceNickname"
    } else {
        deviceNickname
    }

    fun goBack() {
        val previous = draft.step.previousOrNull()
        if (synced || previous == null) {
            if (synced) onFinished() else onBack()
        } else {
            draft = draft.back()
            stepError = null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PloCareColor.BrandNavy)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(8.dp))
        Text(
            text = "← 뒤로",
            color = PloCareColor.AquaTeal,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .clickable(onClick = { goBack() })
                .padding(vertical = 8.dp),
        )
        Text(contextLabel, color = PloCareColor.AquaTeal, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text("정수기 설정", color = PloCareColor.TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text(headingDetail, color = PloCareColor.TextSecondary, fontSize = 13.sp)
        Spacer(Modifier.height(12.dp))
        StepMeter(current = draft.step)
        Spacer(Modifier.height(16.dp))

        if (synced) {
            WizardCard {
                Text("동기화 완료", color = PloCareColor.StatusGood, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text(
                    "설정값이 이 기기에 반영된 것으로 표시합니다. 실제 BLE 전송은 이후 연결됩니다.",
                    color = PloCareColor.TextSecondary,
                    fontSize = 13.sp,
                )
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = onFinished,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = PloCareColor.AquaTeal),
                ) {
                    Text("돌아가기", color = PloCareColor.BgDeep, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                Text(draft.step.title, color = PloCareColor.TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text(draft.step.caption, color = PloCareColor.TextSecondary, fontSize = 13.sp)
                Spacer(Modifier.height(14.dp))
                when (draft.step) {
                    SettingsStep.BLE -> BleStep(draft) { draft = it; stepError = null }
                    SettingsStep.MODEL -> ModelStep(draft) { draft = it; stepError = null }
                    SettingsStep.HYDRAULIC -> HydraulicStep(draft) { draft = it; stepError = null }
                    SettingsStep.CONNECTIVITY -> ConnectivityStep(draft) { draft = it; stepError = null }
                    SettingsStep.FLOW_TEST -> FlowTestStep(draft) { draft = it; stepError = null }
                    SettingsStep.SUMMARY -> SummaryStep(draft)
                }
                stepError?.let {
                    Spacer(Modifier.height(12.dp))
                    Text(it, color = PloCareColor.StatusAlert, fontSize = 13.sp)
                }
                Spacer(Modifier.height(16.dp))
            }
            Button(
                onClick = {
                    val error = validateSettingsStep(draft)
                    if (error != null) {
                        stepError = error
                    } else if (draft.step == SettingsStep.SUMMARY) {
                        synced = true
                    } else {
                        draft = draft.advance()
                        stepError = null
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PloCareColor.AquaTeal),
            ) {
                Text(
                    text = if (draft.step == SettingsStep.SUMMARY) "저장하고 동기화" else "다음",
                    color = PloCareColor.BgDeep,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun StepMeter(current: SettingsStep) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        SettingsStep.entries.forEach { step ->
            Box(
                Modifier
                    .weight(1f)
                    .height(4.dp)
                    .background(
                        if (step.ordinal <= current.ordinal) PloCareColor.AquaTeal else PloCareColor.SurfaceBorder,
                        RoundedCornerShape(2.dp),
                    ),
            )
        }
    }
    Spacer(Modifier.height(6.dp))
    Text(
        "${current.ordinal + 1} / ${SettingsStep.entries.size}",
        color = PloCareColor.TextTertiary,
        fontSize = 11.sp,
    )
}

@Composable
private fun BleStep(draft: SettingsDraft, onChange: (SettingsDraft) -> Unit) {
    Text("권한 안내", color = PloCareColor.TextSecondary, fontSize = 12.sp)
    Spacer(Modifier.height(6.dp))
    Text(
        "근처 센서를 찾으려면 블루투스 권한이 필요합니다. 지금은 검색 목록만 보여 줍니다.",
        color = PloCareColor.TextTertiary,
        fontSize = 12.sp,
    )
    Spacer(Modifier.height(12.dp))
    stubBleDevices.forEach { device ->
        val selected = draft.connectedDeviceId == device.id
        WizardCard(onClick = {
            onChange(draft.copy(connectedDeviceId = device.id, connectedDeviceName = device.name))
        }) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(device.name, color = PloCareColor.TextPrimary, fontWeight = FontWeight.SemiBold)
                    Text("RSSI ${device.rssi} dBm", color = PloCareColor.TextTertiary, fontSize = 11.sp)
                }
                Text(
                    if (selected) "연결됨" else "연결",
                    color = if (selected) PloCareColor.StatusGood else PloCareColor.AquaTeal,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                )
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun ModelStep(draft: SettingsDraft, onChange: (SettingsDraft) -> Unit) {
    Text("브랜드", color = PloCareColor.TextSecondary, fontSize = 12.sp)
    Spacer(Modifier.height(8.dp))
    FlowChoices(settingsBrands, draft.brand) { onChange(draft.copy(brand = it)) }
    Spacer(Modifier.height(14.dp))
    WizardField(value = draft.model, onValueChange = { onChange(draft.copy(model = it)) }, label = "모델명")
    Spacer(Modifier.height(14.dp))
    Text("필터 단수", color = PloCareColor.TextSecondary, fontSize = 12.sp)
    Spacer(Modifier.height(8.dp))
    FlowChoices((1..5).map { "${it}단" }, "${draft.stageCount}단") { label ->
        onChange(draft.copy(stageCount = label.removeSuffix("단").toInt()))
    }
}

@Composable
private fun HydraulicStep(draft: SettingsDraft, onChange: (SettingsDraft) -> Unit) {
    Text("배관 규격", color = PloCareColor.TextSecondary, fontSize = 12.sp)
    Spacer(Modifier.height(8.dp))
    FlowChoices(pipeSizes, draft.pipeSize) { onChange(draft.copy(pipeSize = it)) }
    Spacer(Modifier.height(14.dp))
    Text("밸브 종류", color = PloCareColor.TextSecondary, fontSize = 12.sp)
    Spacer(Modifier.height(8.dp))
    FlowChoices(valveTypes, draft.valveType) { onChange(draft.copy(valveType = it)) }
    Spacer(Modifier.height(14.dp))
    WizardField(
        value = draft.pressureKgf,
        onValueChange = { onChange(draft.copy(pressureKgf = it)) },
        label = "수압 (kgf/cm²)",
        keyboardType = KeyboardType.Decimal,
    )
    Spacer(Modifier.height(12.dp))
    WizardCard {
        Text("Kv 미리보기", color = PloCareColor.TextSecondary, fontSize = 11.sp)
        Spacer(Modifier.height(4.dp))
        Text(
            "${formatKv(draft.previewKv())} L/s",
            color = PloCareColor.AquaTeal,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(4.dp))
        Text("공식 값은 직접 수정할 수 없습니다.", color = PloCareColor.TextTertiary, fontSize = 11.sp)
    }
}

@Composable
private fun ConnectivityStep(draft: SettingsDraft, onChange: (SettingsDraft) -> Unit) {
    FlowChoices(listOf("가정용 Wi-Fi", "상업용 NB-IoT"), if (draft.householdMode) "가정용 Wi-Fi" else "상업용 NB-IoT") {
        onChange(draft.copy(householdMode = it.startsWith("가정용")))
    }
    Spacer(Modifier.height(14.dp))
    if (draft.householdMode) {
        WizardField(value = draft.wifiSsid, onValueChange = { onChange(draft.copy(wifiSsid = it)) }, label = "Wi-Fi 이름")
        Spacer(Modifier.height(10.dp))
        WizardField(
            value = draft.wifiPassword,
            onValueChange = { onChange(draft.copy(wifiPassword = it)) },
            label = "Wi-Fi 비밀번호",
            keyboardType = KeyboardType.Password,
            password = true,
        )
        Spacer(Modifier.height(8.dp))
        Text("비밀번호는 전송 전에 암호화되며 화면에 다시 보여 주지 않습니다.", color = PloCareColor.TextTertiary, fontSize = 11.sp)
    } else {
        WizardCard(onClick = { onChange(draft.copy(nbiotChecked = true)) }) {
            Text("NB-IoT 모뎀 신호", color = PloCareColor.TextPrimary, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            Text(
                if (draft.nbiotChecked) "신호 확인됨 · 서버 핑 대기(스텁)" else "탭해서 신호 확인",
                color = if (draft.nbiotChecked) PloCareColor.StatusGood else PloCareColor.AquaTeal,
                fontSize = 12.sp,
            )
        }
    }
}

@Composable
private fun FlowTestStep(draft: SettingsDraft, onChange: (SettingsDraft) -> Unit) {
    val predicted = round(FilterLifeCalculator.predictedOneMinuteMl(draft.previewKv())).toInt()
    WizardCard {
        Text("예상 1분 출수량", color = PloCareColor.TextSecondary, fontSize = 11.sp)
        Text("${predicted} mL", color = PloCareColor.TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text("실제 60초 카운트다운은 이후 센서 연동 때 연결됩니다.", color = PloCareColor.TextTertiary, fontSize = 11.sp)
    }
    Spacer(Modifier.height(12.dp))
    WizardField(
        value = draft.measuredMl,
        onValueChange = { onChange(draft.copy(measuredMl = it, skippedFlowTest = false)) },
        label = "측정 용량 (mL)",
        keyboardType = KeyboardType.Decimal,
    )
    Spacer(Modifier.height(10.dp))
    OutlinedButton(
        onClick = { onChange(draft.copy(skippedFlowTest = true, measuredMl = "")) },
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            if (draft.skippedFlowTest) "기본 Kv 유지로 건너뜀" else "테스트 건너뛰고 기본 Kv 유지",
            color = PloCareColor.AquaTeal,
        )
    }
}

@Composable
private fun SummaryStep(draft: SettingsDraft) {
    WizardCard {
        SummaryRow("센서", draft.connectedDeviceName.orEmpty())
        SummaryRow("모델", "${draft.brand} ${draft.model} · ${draft.stageCount}단")
        SummaryRow("배관", "${draft.pipeSize} · ${draft.valveType} · ${draft.pressureKgf} kgf/cm²")
        SummaryRow("통신", if (draft.householdMode) "${draft.wifiSsid} · 비밀번호 저장됨" else "NB-IoT")
        SummaryRow(
            "Kv",
            if (draft.skippedFlowTest) "${formatKv(draft.previewKv())} L/s (기본)"
            else "${formatKv(draft.calibratedKv())} L/s (보정)",
        )
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Column(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Text(label, color = PloCareColor.TextTertiary, fontSize = 11.sp)
        Text(value, color = PloCareColor.TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun FlowChoices(options: List<String>, selected: String, onSelect: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        options.chunked(3).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { option ->
                    val active = option == selected
                    Box(
                        Modifier
                            .weight(1f)
                            .border(1.dp, if (active) PloCareColor.AquaTeal else PloCareColor.SurfaceBorder, RoundedCornerShape(10.dp))
                            .background(if (active) PloCareColor.AquaTeal.copy(alpha = 0.16f) else PloCareColor.SurfaceCard, RoundedCornerShape(10.dp))
                            .clickable { onSelect(option) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(option, color = if (active) PloCareColor.AquaTeal else PloCareColor.TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
                repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun WizardField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    password: Boolean = false,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = if (password) PasswordVisualTransformation() else VisualTransformation.None,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = PloCareColor.TextPrimary,
            unfocusedTextColor = PloCareColor.TextPrimary,
            focusedBorderColor = PloCareColor.AquaTeal,
            unfocusedBorderColor = PloCareColor.SurfaceBorder,
            focusedLabelColor = PloCareColor.AquaTeal,
            unfocusedLabelColor = PloCareColor.TextSecondary,
            focusedContainerColor = PloCareColor.SurfaceDark,
            unfocusedContainerColor = PloCareColor.SurfaceDark,
        ),
    )
}

@Composable
private fun WizardCard(onClick: (() -> Unit)? = null, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = if (onClick == null) Modifier.fillMaxWidth() else Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PloCareColor.SurfaceCard),
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp), content = content)
    }
}

private fun formatKv(value: Double): String {
    val scaled = round(value * 10_000.0).toInt()
    val whole = scaled / 10_000
    val frac = (scaled % 10_000).toString().padStart(4, '0').trimEnd('0').ifEmpty { "0" }
    return if (frac == "0") whole.toString() else "$whole.$frac"
}
