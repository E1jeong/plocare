package com.senplo.plocare.ui.partner

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.senplo.plocare.domain.filter.DashboardSnapshot
import com.senplo.plocare.ui.consumer.support.SupportHeader
import com.senplo.plocare.ui.theme.PloCareColor
import com.senplo.plocare.ui.theme.PloCareTheme
import org.jetbrains.compose.resources.painterResource
import plocare.shared.generated.resources.Res
import plocare.shared.generated.resources.ic_chevron_forward
import plocare.shared.generated.resources.ic_info

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
        caption = "블루투스 • 1분 유량 테스트",
    ),
    DIAGNOSTICS(
        title = "센서•하드웨어 점검",
        caption = "클램프, 누수, 통신 상태 확인",
    ),
}

// Figma 17 고객 현장 화면 (146:2746)
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
    val colors = PloCareColor
    val typography = PloCareTheme.typography
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.White)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        SupportHeader(title = "고객 정보", onBack = onBack)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            if (visit == null) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    PartnerSectionHeading("고객을 찾을 수 없습니다")
                    Text(
                        text = "고객 번호 ‘$customerId’를 다시 확인해 주세요.",
                        style = typography.labelMidMedium,
                        color = colors.Black50Muted,
                    )
                }
            } else {
                CustomerCard(visit, workspaceVisitBadge(snapshot, visit))
                FilterWearSection(workspaceFilterRows(snapshot, visit))
                SiteWorkSection(
                    onAction = { action ->
                        when (action) {
                            WorkspaceAction.WORK -> onOpenWork()
                            WorkspaceAction.SETTINGS -> onOpenSettings()
                            WorkspaceAction.DIAGNOSTICS -> onOpenDiagnostics()
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun CustomerCard(visit: PartnerVisitFixture, badge: VisitBadgeKind) {
    val colors = PloCareColor
    val typography = PloCareTheme.typography
    val (accent, bannerFill) = when (badge) {
        VisitBadgeKind.URGENT -> colors.StatusAlert to colors.StatusAlert.copy(alpha = 0.1f)
        VisitBadgeKind.NORMAL -> colors.Primary to colors.PrimaryTint.copy(alpha = 0.6f)
        VisitBadgeKind.DONE -> colors.Black40Ghost to colors.White
    }
    // Card (146:2916)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.Black0Surface, RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            PartnerSectionHeading("${visit.maskedName} •")
            Text(
                text = visit.customerId,
                style = typography.bodyLarge,
                color = colors.Primary,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(1f),
            )
        }
        Text(text = visit.address, style = typography.labelMidMedium, color = colors.Black70)
        // Visit banner (146:2939): red for urgent as in Figma; the other states reuse the primary banner.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(bannerFill, RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_info),
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = "오늘 ${visit.time}",
                style = typography.labelMidMedium,
                color = accent,
                modifier = Modifier.weight(1f),
            )
            Text(text = badge.label, style = typography.labelMidMedium, color = accent)
        }
    }
}

@Composable
private fun FilterWearSection(rows: List<WorkspaceFilterRow>) {
    val colors = PloCareColor
    val typography = PloCareTheme.typography
    val shape = RoundedCornerShape(12.dp)
    // 필터 소진 상태 (146:2962)
    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
        PartnerSectionHeading("필터 소진 상태")
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.White, shape)
                .border(1.dp, colors.Black10, shape)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            rows.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(text = row.label, style = typography.labelLarge, color = colors.TextMain)
                        if (row.exhausted) {
                            PartnerBadge(row.badge, colors.StatusAlert, colors.StatusAlert.copy(alpha = 0.1f))
                        } else {
                            PartnerBadge(row.badge, colors.Primary, colors.Primary.copy(alpha = 0.1f))
                        }
                    }
                    Text(
                        text = "${row.percent}%",
                        style = typography.labelLarge,
                        color = if (row.exhausted) colors.StatusAlert else colors.TextMain,
                    )
                }
            }
        }
    }
}

@Composable
private fun SiteWorkSection(onAction: (WorkspaceAction) -> Unit) {
    val colors = PloCareColor
    val typography = PloCareTheme.typography
    val shape = RoundedCornerShape(12.dp)
    // 현장 작업 (146:3015)
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        PartnerSectionHeading("현장 작업")
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.White, shape)
                .border(1.dp, colors.Black10, shape)
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            WorkspaceAction.entries.forEachIndexed { index, action ->
                if (index > 0) {
                    Box(Modifier.fillMaxWidth().height(1.dp).background(colors.Black10))
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .clickable(role = Role.Button) { onAction(action) },
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = action.title, style = typography.bodyLarge, color = colors.TextMain)
                        Text(text = action.caption, style = typography.labelMidMedium, color = colors.Black50Muted)
                    }
                    Icon(
                        painter = painterResource(Res.drawable.ic_chevron_forward),
                        contentDescription = null,
                        tint = colors.Black50Muted,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
        }
    }
}
