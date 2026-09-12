package com.senplo.plocare.ui.consumer.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.senplo.plocare.domain.filter.DashboardFixtures
import com.senplo.plocare.domain.filter.DashboardSnapshot
import com.senplo.plocare.domain.filter.FilterSnapshot
import com.senplo.plocare.domain.filter.ForecastStage
import com.senplo.plocare.domain.filter.PurifierDevice
import com.senplo.plocare.domain.filter.buildDashboardSnapshot
import com.senplo.plocare.domain.filter.withSelfReplaced
import com.senplo.plocare.domain.filter.withTelemetryAt
import com.senplo.plocare.ui.theme.PloCareColor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlin.time.Clock

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeDashboardScreen(
    onRequestReplacement: (filterIds: List<String>) -> Unit,
    onOpenDeviceSettings: () -> Unit,
) {
    val timeZone = remember { TimeZone.currentSystemDefault() }
    val scope = rememberCoroutineScope()
    var now by remember { mutableStateOf(Clock.System.now()) }
    var devices by remember { mutableStateOf(DashboardFixtures.devices(now, timeZone)) }
    var selectedId by remember { mutableStateOf(devices.first().id) }
    var isRefreshing by remember { mutableStateOf(false) }
    var pendingSelfReplace by remember { mutableStateOf<FilterSnapshot?>(null) }

    val selected = devices.first { it.id == selectedId }
    val snapshot = remember(selected, now) {
        buildDashboardSnapshot(selected, now, timeZone)
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            scope.launch {
                isRefreshing = true
                delay(350)
                val refreshedAt = Clock.System.now()
                now = refreshedAt
                devices = devices.map { device ->
                    if (device.id == selectedId) device.withTelemetryAt(refreshedAt) else device
                }
                isRefreshing = false
            }
        },
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            DashboardHeader(
                devices = devices,
                selected = selected,
                syncText = syncLabel(now - selected.lastTelemetryAt),
                onSelectDevice = { selectedId = it },
                onOpenNotifications = {},
                onOpenDeviceSettings = onOpenDeviceSettings,
            )

            if (snapshot.sensorStale) {
                Spacer(modifier = Modifier.height(12.dp))
                AlertBanner(
                    accent = PloCareColor.StatusWarn,
                    title = "정수기 센서 Wi-Fi 연결을 확인해 주세요 (24시간 미수신)",
                    actionLabel = "연결 가이드",
                    onAction = onOpenDeviceSettings,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            AiBriefingBanner(snapshot = snapshot)

            Spacer(modifier = Modifier.height(12.dp))
            FilterUsageDonut(
                filters = snapshot.filters,
                onSelfReplace = { pendingSelfReplace = it },
                onRequestReplacement = { onRequestReplacement(listOf(it.id)) },
            )

            snapshot.overCapacityFilters.firstOrNull()?.let { exhausted ->
                Spacer(modifier = Modifier.height(12.dp))
                AlertBanner(
                    accent = PloCareColor.StatusAlert,
                    title = overCapacityMessage(exhausted),
                    actionLabel = "긴급 방문 교체 요청",
                    onAction = { onRequestReplacement(listOf(exhausted.id)) },
                )
            }

            bundleMessage(snapshot)?.let { message ->
                Spacer(modifier = Modifier.height(12.dp))
                AlertBanner(
                    accent = PloCareColor.VividCyan,
                    title = message,
                    actionLabel = "원클릭 묶음 교체 신청하기",
                    onAction = { onRequestReplacement(snapshot.bundle?.filterIds.orEmpty()) },
                )
            }

            selected.visitTicket?.let { ticket ->
                Spacer(modifier = Modifier.height(12.dp))
                VisitTicketCard(
                    whenLabel = visitWhen(ticket),
                    technician = ticket.technicianMaskedName,
                    targets = visitTargets(ticket, snapshot.filters),
                    onReview = { onRequestReplacement(ticket.targetFilterIds) },
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }

    pendingSelfReplace?.let { filter ->
        AlertDialog(
            onDismissRequest = { pendingSelfReplace = null },
            containerColor = PloCareColor.SurfaceCard,
            title = {
                Text(
                    text = "자가 교체 확인",
                    color = PloCareColor.TextPrimary,
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                Text(
                    text = selfReplaceSummary(filter, selected.totalCumulativeL),
                    color = PloCareColor.TextSecondary,
                    fontSize = 14.sp,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        devices = devices.map { device ->
                            if (device.id == selectedId) device.withSelfReplaced(filter.id) else device
                        }
                        pendingSelfReplace = null
                    },
                ) {
                    Text("확인", color = PloCareColor.AquaTeal, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingSelfReplace = null }) {
                    Text("취소", color = PloCareColor.TextSecondary)
                }
            },
        )
    }
}

@Composable
private fun DashboardHeader(
    devices: List<PurifierDevice>,
    selected: PurifierDevice,
    syncText: String,
    onSelectDevice: (String) -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenDeviceSettings: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Box {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { expanded = true }
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = selected.nickname,
                        color = PloCareColor.TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "  ▾",
                        color = PloCareColor.TextSecondary,
                        fontSize = 16.sp,
                    )
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                ) {
                    devices.forEach { device ->
                        DropdownMenuItem(
                            text = { Text(device.nickname) },
                            onClick = {
                                onSelectDevice(device.id)
                                expanded = false
                            },
                        )
                    }
                }
            }
            Text(
                text = syncText,
                color = PloCareColor.TextTertiary,
                fontSize = 12.sp,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            HeaderIconButton(label = "알림", glyph = "🔔", onClick = onOpenNotifications)
            HeaderIconButton(label = "설정", glyph = "⚙", onClick = onOpenDeviceSettings)
        }
    }
}

@Composable
private fun HeaderIconButton(
    label: String,
    glyph: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(PloCareColor.SurfaceCard)
            .border(1.dp, PloCareColor.SurfaceBorder, CircleShape)
            .semantics { contentDescription = label }
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = glyph, color = PloCareColor.TextPrimary, fontSize = 14.sp)
    }
}

@Composable
private fun AiBriefingBanner(snapshot: DashboardSnapshot) {
    val icon = if (snapshot.forecastStage == ForecastStage.COLD_START) "📊" else "🤖"
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(PloCareColor.SurfaceCard)
            .border(1.dp, PloCareColor.SurfaceBorder, RoundedCornerShape(14.dp))
            .padding(14.dp),
    ) {
        Text(
            text = "$icon ${briefingTitle(snapshot)}",
            color = PloCareColor.TextPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = briefingCaption(snapshot),
            color = PloCareColor.TextTertiary,
            fontSize = 11.sp,
        )
    }
}

@Composable
private fun AlertBanner(
    accent: Color,
    title: String,
    actionLabel: String,
    onAction: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(PloCareColor.SurfaceCard)
            .border(1.dp, accent.copy(alpha = 0.55f), RoundedCornerShape(14.dp))
            .padding(14.dp),
    ) {
        Text(
            text = title,
            color = PloCareColor.TextPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = onAction,
            modifier = Modifier.fillMaxWidth().height(40.dp),
            colors = ButtonDefaults.buttonColors(containerColor = accent),
            shape = RoundedCornerShape(10.dp),
        ) {
            Text(
                text = actionLabel,
                color = PloCareColor.BgDeep,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun VisitTicketCard(
    whenLabel: String,
    technician: String?,
    targets: String,
    onReview: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(PloCareColor.SurfaceCard)
            .border(1.dp, PloCareColor.SurfaceBorder, RoundedCornerShape(14.dp))
            .padding(14.dp),
    ) {
        Text(
            text = "진행 중인 방문 케어",
            color = PloCareColor.TextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = whenLabel + technician?.let { " ($it 테크니션 방문 예정)" }.orEmpty(),
            color = PloCareColor.TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = targets,
            color = PloCareColor.TextSecondary,
            fontSize = 12.sp,
        )
        Spacer(modifier = Modifier.height(10.dp))
        Button(
            onClick = onReview,
            modifier = Modifier.fillMaxWidth().height(40.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PloCareColor.AquaTeal),
            shape = RoundedCornerShape(10.dp),
        ) {
            Text(
                text = "예약 확인 / 변경",
                color = PloCareColor.BgDeep,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}
