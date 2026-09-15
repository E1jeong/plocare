package com.senplo.plocare.ui.partner

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.senplo.plocare.domain.filter.DashboardSnapshot
import com.senplo.plocare.ui.theme.PloCareColor
import kotlin.time.Instant

@Composable
internal fun SensorDiagnosticsScreen(
    visit: PartnerVisitFixture?,
    snapshot: DashboardSnapshot?,
    now: Instant,
    onBack: () -> Unit,
    onConfirmed: () -> Unit,
) {
    val view = remember(snapshot, now) {
        snapshot?.let { sensorDiagnosticsView(it, now) }
    }
    var clampSeated by remember { mutableStateOf(false) }
    var noLeak by remember { mutableStateOf(false) }
    var commsObserved by remember { mutableStateOf(false) }
    var formError by remember { mutableStateOf<String?>(null) }
    var confirmed by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PloCareColor.BrandNavy)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(8.dp))
        Text(
            text = "← 현장 콘솔",
            color = PloCareColor.AquaTeal,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.clickable(onClick = onBack).padding(vertical = 8.dp),
        )
        Text("센서·하드웨어 점검", color = PloCareColor.TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        Text(
            text = visit?.let { "${it.displayName} · ${view?.deviceNickname ?: it.deviceId}" }
                ?: "고객을 찾을 수 없습니다",
            color = PloCareColor.TextSecondary,
            fontSize = 13.sp,
        )
        Spacer(Modifier.height(18.dp))

        if (visit == null || snapshot == null || view == null) {
            PartnerCard {
                Text("방문과 연결된 정수기를 찾을 수 없습니다.", color = PloCareColor.TextSecondary, fontSize = 13.sp)
            }
        } else if (confirmed) {
            PartnerCard {
                Text(
                    diagnosticsConfirmationTitle(),
                    color = PloCareColor.TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(8.dp))
                Text(diagnosticsConfirmationDetail(), color = PloCareColor.TextSecondary, fontSize = 13.sp)
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = onConfirmed,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PloCareColor.AquaTeal),
                ) {
                    Text("현장 콘솔로", color = PloCareColor.BgDeep, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            PartnerCard {
                Text("로컬 미리보기", color = PloCareColor.AquaTeal, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Text(
                    "실시간 센서 값은 표시하지 않습니다. Wi-Fi·NB-IoT와 정전용량 신호는 연결되지 않았습니다.",
                    color = PloCareColor.TextSecondary,
                    fontSize = 13.sp,
                )
            }
            Spacer(Modifier.height(18.dp))
            Text("기기", color = PloCareColor.TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            PartnerCard {
                InfoRow("기기 ID", view.deviceId)
                InfoRow("센서 설치일", view.installedOn)
                InfoRow("마지막 수신", view.telemetryLabel)
            }
            Spacer(Modifier.height(18.dp))
            Text("통신", color = PloCareColor.TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            PartnerCard {
                StatusRow("Wi-Fi / BLE", view.wifiLabel, view.telemetryFresh)
                StatusRow("NB-IoT", view.nbiotLabel, fresh = false)
            }
            Spacer(Modifier.height(18.dp))
            Text("정전용량 센서", color = PloCareColor.TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            PartnerCard {
                Text(view.signalLabel, color = PloCareColor.TextSecondary, fontSize = 13.sp)
                Spacer(Modifier.height(6.dp))
                Text(
                    "표시 값은 기기의 마지막 수신 기록입니다.",
                    color = PloCareColor.TextTertiary,
                    fontSize = 12.sp,
                )
            }
            Spacer(Modifier.height(18.dp))
            Text("현장 육안 확인", color = PloCareColor.TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text("실시간 측정이 아닙니다.", color = PloCareColor.TextTertiary, fontSize = 11.sp)
            Spacer(Modifier.height(10.dp))
            CheckRow("클램프가 파이프에 고정됨", clampSeated) { clampSeated = it }
            Spacer(Modifier.height(8.dp))
            CheckRow("배관 누수 없음", noLeak) { noLeak = it }
            Spacer(Modifier.height(8.dp))
            CheckRow("통신 상태를 현장에서 확인함", commsObserved) { commsObserved = it }
            formError?.let {
                Spacer(Modifier.height(12.dp))
                Text(it, color = PloCareColor.StatusAlert, fontSize = 13.sp)
            }
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    val error = validateSensorDiagnostics(
                        SensorDiagnosticsDraft(
                            clampSeated = clampSeated,
                            noLeak = noLeak,
                            commsObserved = commsObserved,
                        ),
                    )
                    if (error == null) {
                        formError = null
                        confirmed = true
                    } else {
                        formError = error
                    }
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PloCareColor.AquaTeal),
            ) {
                Text("점검 내용 확인", color = PloCareColor.BgDeep, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, color = PloCareColor.TextTertiary, fontSize = 12.sp, modifier = Modifier.weight(1f))
        Text(value, color = PloCareColor.TextSecondary, fontSize = 12.sp)
    }
}

@Composable
private fun StatusRow(label: String, value: String, fresh: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(if (fresh) PloCareColor.StatusGood else PloCareColor.StatusWarn),
        )
        Text(
            label,
            modifier = Modifier.padding(start = 8.dp).weight(1f),
            color = PloCareColor.TextSecondary,
            fontSize = 12.sp,
        )
        Text(value, color = PloCareColor.TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun CheckRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    PartnerCard(onClick = { onCheckedChange(!checked) }) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = if (checked) "✓" else "○",
                color = if (checked) PloCareColor.AquaTeal else PloCareColor.TextTertiary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                label,
                color = PloCareColor.TextPrimary,
                fontSize = 14.sp,
                modifier = Modifier.padding(start = 10.dp),
            )
        }
    }
}
