package com.senplo.plocare.ui.partner

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.senplo.plocare.domain.filter.DashboardSnapshot
import com.senplo.plocare.domain.filter.FilterSnapshot
import com.senplo.plocare.ui.theme.PloCareColor
import kotlin.math.roundToInt

private enum class WorkspaceAction(
    val title: String,
    val caption: String,
) {
    WORK(
        title = "교체 작업 기록",
        caption = "실제 장착한 필터와 완료 확인",
    ),
    SETTINGS(
        title = "설정 및 유량 보정",
        caption = "BLE 페어링 · 1분 유량 테스트",
    ),
    DIAGNOSTICS(
        title = "센서·하드웨어 점검",
        caption = "클램프, 누수, 통신 상태 확인",
    ),
}

@Composable
internal fun CustomerWorkspaceScreen(
    customerId: String,
    visit: PartnerVisitFixture?,
    snapshot: DashboardSnapshot?,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit = {},
    onOpenWork: () -> Unit = {},
    onOpenDiagnostics: () -> Unit = {},
) {
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
            text = "← 뒤로",
            color = PloCareColor.AquaTeal,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .clickable(onClick = onBack)
                .padding(vertical = 8.dp),
        )
        if (visit == null) {
            Text("고객을 찾을 수 없습니다", color = PloCareColor.TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text("고객 번호 ‘$customerId’를 다시 확인해 주세요.", color = PloCareColor.TextSecondary, fontSize = 13.sp)
        } else {
            Text(visit.customerId, color = PloCareColor.AquaTeal, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(visit.maskedName, color = PloCareColor.TextPrimary, fontSize = 23.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text(visit.address, color = PloCareColor.TextSecondary, fontSize = 13.sp)
            Spacer(Modifier.height(4.dp))
            val householdUrgent = snapshot != null && householdIsUrgent(snapshot) && !visit.completed
            Text(
                text = when {
                    visit.completed -> "방문 완료"
                    householdUrgent -> "오늘 ${visit.time} · 긴급"
                    else -> "오늘 ${visit.time} · 예정"
                },
                color = if (householdUrgent) PloCareColor.StatusAlert else PloCareColor.VividCyan,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
            )
            if (snapshot?.device != null) {
                Spacer(Modifier.height(4.dp))
                Text(snapshot.device.nickname, color = PloCareColor.TextTertiary, fontSize = 12.sp)
            }
            Spacer(Modifier.height(18.dp))
            Text("필터 소진 상태", color = PloCareColor.TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            WorkspaceCard {
                if (snapshot != null) {
                    snapshot.filters.forEach { filter ->
                        FilterSnapshotRow(snapshot, filter)
                    }
                } else {
                    visit.filterStatuses.forEach { filter ->
                        FilterStatusRow(filter)
                    }
                }
            }
            Spacer(Modifier.height(18.dp))
            Text("현장 작업", color = PloCareColor.TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            WorkspaceAction.entries.forEach { action ->
                WorkspaceCard(onClick = {
                    when (action) {
                        WorkspaceAction.SETTINGS -> onOpenSettings()
                        WorkspaceAction.WORK -> onOpenWork()
                        WorkspaceAction.DIAGNOSTICS -> onOpenDiagnostics()
                    }
                }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(action.title, color = PloCareColor.TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(3.dp))
                            Text(action.caption, color = PloCareColor.TextSecondary, fontSize = 12.sp)
                        }
                        Text("›", color = PloCareColor.AquaTeal, fontSize = 22.sp)
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun FilterSnapshotRow(snapshot: DashboardSnapshot, filter: FilterSnapshot) {
    val percent = filter.exhaustionPercent.roundToInt()
    val label = partnerFilterLabel(snapshot, filter)
    val accent = when {
        percent >= 100 -> PloCareColor.StatusAlert
        filter.showReplacementRequest -> PloCareColor.StatusWarn
        else -> PloCareColor.StatusGood
    }
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text("${filter.stage}단 ${filter.name}", color = PloCareColor.TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text(label, color = accent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Text("$percent%", color = accent, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun FilterStatusRow(filter: PartnerFilterStatus) {
    val accent = when {
        filter.exhaustionPercent >= 100 -> PloCareColor.StatusAlert
        filter.exhaustionPercent >= 90 -> PloCareColor.StatusWarn
        else -> PloCareColor.StatusGood
    }
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text("${filter.stage}단 ${filter.name}", color = PloCareColor.TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text(filter.label, color = accent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Text("${filter.exhaustionPercent}%", color = accent, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun WorkspaceCard(
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = if (onClick == null) {
            Modifier.fillMaxWidth()
        } else {
            Modifier.fillMaxWidth().clickable(onClick = onClick)
        },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PloCareColor.SurfaceCard),
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp), content = content)
    }
}
