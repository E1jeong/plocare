package com.senplo.plocare.ui.consumer.filtercare

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.senplo.plocare.domain.filter.DashboardSnapshot
import com.senplo.plocare.domain.filter.FilterSnapshot
import com.senplo.plocare.domain.filter.PricingPolicy
import com.senplo.plocare.domain.filter.SamplePricingPolicy
import com.senplo.plocare.ui.components.PloCareButton
import com.senplo.plocare.ui.components.PloCareButtonSize
import com.senplo.plocare.ui.components.PloCareButtonVariant
import com.senplo.plocare.ui.consumer.dashboard.HeaderChip
import com.senplo.plocare.ui.consumer.dashboard.chipTextStyle
import com.senplo.plocare.ui.consumer.dashboard.usagePercentLabel
import com.senplo.plocare.ui.theme.PloCareColor
import com.senplo.plocare.ui.theme.PloCareTheme
import org.jetbrains.compose.resources.painterResource
import plocare.shared.generated.resources.Res
import plocare.shared.generated.resources.ic_chevron_forward
import plocare.shared.generated.resources.ic_tune

// Figma 02 필터 관리 (74:721)
@Composable
fun FilterCareScreen(
    snapshot: DashboardSnapshot,
    preselectedFilterIds: List<String>,
    onBookVisit: (List<String>) -> Unit = {},
    onOpenPurchase: (List<String>) -> Unit = {},
    onOpenSettings: () -> Unit = {},
    // The replacement-method section is built but hidden at first (user decision 2026-11-19, UI-06).
    showReplacementMethods: Boolean = false,
    pricingPolicy: PricingPolicy = SamplePricingPolicy(),
) {
    val colors = PloCareColor
    val typography = PloCareTheme.typography
    val targetIds = preselectedFilterIds.ifEmpty {
        snapshot.filters.filter { it.showReplacementRequest }.map { it.id }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Header (74:851)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "필터 관리",
                style = typography.titleLarge,
                color = colors.TextMain,
                modifier = Modifier.semantics { heading() },
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 20.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp),
        ) {
            DeviceSettingsSection(
                deviceName = snapshot.device.nickname,
                filterCount = snapshot.filters.size,
                onOpenSettings = onOpenSettings,
            )
            if (showReplacementMethods) {
                ReplacementMethodSection(
                    pricingPolicy = pricingPolicy,
                    onOpenPurchase = { onOpenPurchase(targetIds) },
                    onBookVisit = { onBookVisit(targetIds) },
                )
            }
            FilterStatusSection(snapshot = snapshot)
        }
    }
}

@Composable
private fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = PloCareTheme.typography.titleMid,
        color = PloCareColor.TextMain,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier.semantics { heading() },
    )
}

@Composable
private fun DeviceSettingsSection(
    deviceName: String,
    filterCount: Int,
    onOpenSettings: () -> Unit,
) {
    val colors = PloCareColor
    val typography = PloCareTheme.typography
    val cardShape = RoundedCornerShape(12.dp)

    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SectionTitle(text = deviceName, modifier = Modifier.weight(1f, fill = false))
            HeaderChip(text = "${filterCount}개 필터", color = colors.Primary)
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(cardShape)
                .background(colors.White)
                .clickable(role = Role.Button, onClick = onOpenSettings)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(colors.Black0Surface, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_tune),
                    contentDescription = null,
                    tint = colors.TextMain,
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "정수기 설정 • 1분 보정", style = typography.bodyLarge, color = colors.TextMain)
                Text(text = "모델 • 센서 • 통신 다시 설정", style = typography.labelMidMedium, color = colors.Black50Muted)
            }
            Icon(
                painter = painterResource(Res.drawable.ic_chevron_forward),
                contentDescription = null,
                tint = colors.Black50Muted,
            )
        }
    }
}

@Composable
private fun ReplacementMethodSection(
    pricingPolicy: PricingPolicy,
    onOpenPurchase: () -> Unit,
    onBookVisit: () -> Unit,
) {
    val colors = PloCareColor
    val typography = PloCareTheme.typography
    val bannerShape = RoundedCornerShape(8.dp)

    Column(
        modifier = Modifier.padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SectionTitle(text = "교체 방법")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ReplacementMethodCard(
                title = "직접 교체",
                detail = "정품 필터 • 무료 배송",
                price = wonFrom(pricingPolicy.defaultFilterUnitPriceKrw),
                actionLabel = "구매 • 구독",
                recommended = true,
                onAction = onOpenPurchase,
                modifier = Modifier.weight(1f),
            )
            ReplacementMethodCard(
                title = "방문 케어",
                detail = "교체 • 살균 • 점검",
                price = wonFrom(pricingPolicy.defaultFilterUnitPriceKrw + pricingPolicy.visitServiceBaseFeeKrw),
                actionLabel = "방문예약",
                recommended = false,
                onAction = onBookVisit,
                modifier = Modifier.weight(1f),
            )
        }
        // Subscription line (77:1571)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.PrimaryContainerLight, bannerShape)
                .border(1.dp, colors.GradientSecondary, bannerShape)
                .padding(12.dp),
        ) {
            Text(
                text = "정기 구독 • 교체 5일 전 자동 배송 • ${pricingPolicy.subscriptionDiscountPercent}% 할인 • 회수 키트 무료",
                style = typography.labelMidMedium,
                color = colors.Primary,
            )
        }
    }
}

@Composable
private fun ReplacementMethodCard(
    title: String,
    detail: String,
    price: String,
    actionLabel: String,
    recommended: Boolean,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = PloCareColor
    val typography = PloCareTheme.typography
    val shape = RoundedCornerShape(12.dp)

    Column(
        modifier = modifier
            .background(if (recommended) colors.PrimaryContainerLight else colors.White, shape)
            .then(if (recommended) Modifier.border(1.dp, colors.GradientSecondary, shape) else Modifier)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SectionTitle(text = title, modifier = Modifier.weight(1f))
                if (recommended) {
                    Box(
                        modifier = Modifier
                            .heightIn(min = 22.dp)
                            .background(colors.PrimaryTint, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(text = "추천", style = chipTextStyle(), color = colors.Primary)
                    }
                }
            }
            Text(text = detail, style = typography.labelSmall, color = colors.Black50Muted)
            Text(text = price, style = typography.labelLarge, color = colors.TextMain)
        }
        PloCareButton(
            text = actionLabel,
            onClick = onAction,
            size = PloCareButtonSize.Small,
            variant = if (recommended) PloCareButtonVariant.Primary else PloCareButtonVariant.Secondary,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun FilterStatusSection(snapshot: DashboardSnapshot) {
    val colors = PloCareColor

    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SectionTitle(text = "필터 상태")
            Text(text = "교체가 가까운 순", style = chipTextStyle(), color = colors.Black50Muted)
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.White, RoundedCornerShape(12.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            sortedByReplacement(snapshot.filters).forEach { filter ->
                FilterStatusRow(filter = filter, snapshot = snapshot)
            }
        }
    }
}

@Composable
private fun FilterStatusRow(filter: FilterSnapshot, snapshot: DashboardSnapshot) {
    val colors = PloCareColor
    val typography = PloCareTheme.typography
    val filterColor = colors.filterTypeColor(filter.filterType)
    val fraction = usageFraction(filter)
    val trackShape = RoundedCornerShape(100.dp)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "${filter.stage}단 ${filter.name}",
                    style = typography.labelLarge,
                    color = colors.TextMain,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                Box(
                    modifier = Modifier
                        .heightIn(min = 22.dp)
                        .background(colors.Black0Surface, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = dDayBadge(filter.effectiveRemainingDays),
                        style = chipTextStyle(),
                        color = colors.Black60Body,
                    )
                }
            }
            Text(
                text = usagePercentLabel(filter),
                style = typography.labelMidMedium,
                color = colors.TextMain,
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(filterColor.copy(alpha = 0.2f), trackShape)
                .clearAndSetSemantics { progressBarRangeInfo = ProgressBarRangeInfo(fraction, 0f..1f) },
        ) {
            if (fraction > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction)
                        .height(8.dp)
                        .background(filterColor, trackShape),
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = filterStatusDateLabel(filter, snapshot.today),
                style = chipTextStyle(),
                color = colors.Black60Body,
                modifier = Modifier.weight(1f),
            )
            usageOverRatedSpaced(filter)?.let {
                Text(text = it, style = typography.labelMidMedium, color = colors.Black50Muted)
            }
        }
    }
}
