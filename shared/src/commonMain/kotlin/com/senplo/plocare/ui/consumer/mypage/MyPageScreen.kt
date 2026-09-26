package com.senplo.plocare.ui.consumer.mypage

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.senplo.plocare.ui.consumer.ConsumerCard
import com.senplo.plocare.ui.consumer.DeviceNicknameDialog
import com.senplo.plocare.ui.consumer.ConsumerScreenHeader
import com.senplo.plocare.ui.consumer.ConsumerSectionTitle
import com.senplo.plocare.ui.theme.PloCareColor
import kotlin.time.Instant

@Composable
fun MyPageScreen(
    snapshot: DashboardSnapshot,
    now: Instant,
    onOpenSettings: () -> Unit = {},
    onRenameDevice: (String) -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        ConsumerScreenHeader(title = "마이페이지", subtitle = "계정과 연결된 정수기를 관리하세요.")
        Spacer(Modifier.height(18.dp))
        ProfileCard()

        Spacer(Modifier.height(22.dp))
        ConsumerSectionTitle(title = "알림 설정")
        Spacer(Modifier.height(10.dp))
        NotificationCard()

        Spacer(Modifier.height(22.dp))
        ConsumerSectionTitle(title = "내 정수기")
        Spacer(Modifier.height(10.dp))
        DeviceCard(snapshot = snapshot, onOpenSettings = onOpenSettings, onRenameDevice = onRenameDevice)
        Spacer(Modifier.height(10.dp))
        HardwareStatusCard(snapshot = snapshot, now = now)

        Spacer(Modifier.height(22.dp))
        ConsumerSectionTitle(title = "고객 지원 및 약관")
        Spacer(Modifier.height(10.dp))
        SupportCard()
        Spacer(Modifier.height(12.dp))
        OutlinedButton(
            onClick = {},
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(1.dp, PloCareColor.SurfaceBorder),
        ) {
            Text("로그아웃", color = PloCareColor.TextSecondary)
        }
        Text(
            "회원 탈퇴",
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(12.dp),
            color = PloCareColor.TextTertiary,
            fontSize = 11.sp,
        )
    }
}

@Composable
private fun ProfileCard() {
    ConsumerCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(48.dp).clip(CircleShape).background(PloCareColor.AquaTeal.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center,
            ) {
                Text("플", color = PloCareColor.AquaTeal, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            Column(Modifier.padding(start = 12.dp).weight(1f)) {
                Text("플로케어 데모 사용자", color = PloCareColor.TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(3.dp))
                Text("계정 정보 미연동", color = PloCareColor.TextSecondary, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun NotificationCard() {
    var expiration by remember { mutableStateOf(true) }
    var bundle by remember { mutableStateOf(true) }
    var visit by remember { mutableStateOf(true) }
    ConsumerCard(modifier = Modifier.fillMaxWidth()) {
        NotificationRow("필터 교체 알림", "교체 임박 및 초과 안내", expiration) { expiration = it }
        HorizontalDivider(color = PloCareColor.SurfaceBorder)
        NotificationRow("묶음 교체 추천", "14일 이내 예상 필터 추천", bundle) { bundle = it }
        HorizontalDivider(color = PloCareColor.SurfaceBorder)
        NotificationRow("방문 일정 알림", "예약 확정 및 방문 전 안내", visit) { visit = it }
    }
}

@Composable
private fun NotificationRow(title: String, description: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, color = PloCareColor.TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Text(description, color = PloCareColor.TextTertiary, fontSize = 10.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = PloCareColor.TextPrimary,
                checkedTrackColor = PloCareColor.AquaTeal,
                uncheckedTrackColor = PloCareColor.SurfaceDark,
            ),
        )
    }
}

@Composable
private fun DeviceCard(snapshot: DashboardSnapshot, onOpenSettings: () -> Unit, onRenameDevice: (String) -> Unit) {
    val device = snapshot.device
    var editing by remember(device.id) { mutableStateOf(false) }
    ConsumerCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(device.nickname, color = PloCareColor.TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(3.dp))
                Text("${device.filters.size}단계 · 가구 ${device.householdSize}인", color = PloCareColor.TextSecondary, fontSize = 12.sp)
            }
            Text("닉네임 편집", color = PloCareColor.AquaTeal, fontSize = 11.sp,
                modifier = Modifier.clickable { editing = true })
        }
        Spacer(Modifier.height(12.dp))
        HorizontalDivider(color = PloCareColor.SurfaceBorder)
        Spacer(Modifier.height(12.dp))
        InfoRow("기기 ID", device.id)
        InfoRow("센서 설치일", device.installedOn.toString())
        OutlinedButton(onClick = onOpenSettings, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
            Text("정수기 설정 열기  ›", color = PloCareColor.AquaTeal, fontSize = 12.sp)
        }
    }
    if (editing) {
        DeviceNicknameDialog(device.nickname, onDismiss = { editing = false }) {
            onRenameDevice(it)
            editing = false
        }
    }
}

@Composable
private fun HardwareStatusCard(snapshot: DashboardSnapshot, now: Instant) {
    val elapsed = now - snapshot.device.lastTelemetryAt
    val minutes = elapsed.inWholeMinutes
    val hours = elapsed.inWholeHours
    val syncText = when {
        snapshot.sensorStale || hours >= 24L -> "24시간 이상 미수신"
        minutes < 1L -> "방금 전 동기화"
        minutes < 60L -> "${minutes}분 전 동기화"
        else -> "${hours}시간 전 동기화"
    }
    ConsumerCard(modifier = Modifier.fillMaxWidth()) {
        Text("하드웨어 상태", color = PloCareColor.TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(12.dp))
        StatusRow("센서 전원", if (snapshot.sensorStale) "확인 필요" else "정상", !snapshot.sensorStale)
        StatusRow("통신", if (snapshot.sensorStale) "미수신" else "연결됨", !snapshot.sensorStale)
        StatusRow("마지막 데이터", syncText, !snapshot.sensorStale)
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = PloCareColor.TextTertiary, fontSize = 11.sp)
        Text(value, color = PloCareColor.TextSecondary, fontSize = 11.sp)
    }
}

@Composable
private fun StatusRow(label: String, value: String, good: Boolean) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(7.dp).background(if (good) PloCareColor.StatusGood else PloCareColor.StatusWarn, CircleShape))
        Text(label, modifier = Modifier.padding(start = 8.dp).weight(1f), color = PloCareColor.TextSecondary, fontSize = 12.sp)
        Text(value, color = PloCareColor.TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun SupportCard() {
    ConsumerCard(modifier = Modifier.fillMaxWidth()) {
        SupportRow("이용약관")
        HorizontalDivider(color = PloCareColor.SurfaceBorder)
        SupportRow("개인정보 처리방침")
        HorizontalDivider(color = PloCareColor.SurfaceBorder)
        SupportRow("오픈소스 라이선스")
        HorizontalDivider(color = PloCareColor.SurfaceBorder)
        SupportRow("고객센터")
    }
}

@Composable
private fun SupportRow(label: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 13.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.weight(1f), color = PloCareColor.TextPrimary, fontSize = 13.sp)
        Text("›", color = PloCareColor.TextTertiary, fontSize = 18.sp)
    }
}
