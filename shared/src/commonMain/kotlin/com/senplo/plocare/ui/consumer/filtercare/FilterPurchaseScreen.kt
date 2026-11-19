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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.senplo.plocare.domain.filter.DashboardSnapshot
import com.senplo.plocare.domain.filter.FilterSnapshot
import com.senplo.plocare.ui.components.PloCareButton
import com.senplo.plocare.ui.components.PloCareButtonSize
import com.senplo.plocare.ui.components.PloCareButtonVariant
import com.senplo.plocare.ui.components.PloCareCheckMark
import com.senplo.plocare.ui.components.PloCareSegmentedTab
import com.senplo.plocare.ui.consumer.dashboard.usagePercentLabel
import com.senplo.plocare.ui.theme.PloCareColor
import com.senplo.plocare.ui.theme.PloCareTheme
import org.jetbrains.compose.resources.painterResource
import plocare.shared.generated.resources.Res
import plocare.shared.generated.resources.ic_back
import plocare.shared.generated.resources.ic_dishwasher

// Figma 07 정품·호환 필터 구매·구독 (98:5287). Contact and delivery address come from the
// contact/address screen (Figma 11); payment and supplier ordering are not connected.
@Composable
fun FilterPurchaseScreen(
    snapshot: DashboardSnapshot,
    preselectedFilterIds: List<String>,
    onBack: () -> Unit,
    onConfirmed: () -> Unit,
    onChangeModel: () -> Unit = {},
) {
    val colors = PloCareColor
    val typography = PloCareTheme.typography
    val initialSelected = remember(preselectedFilterIds, snapshot.filters) {
        preselectedFilterIds.ifEmpty { snapshot.filters.filter { it.showReplacementRequest }.map { it.id } }
    }
    var selectedFilterIds by rememberSaveable { mutableStateOf(initialSelected) }
    var mode by rememberSaveable { mutableStateOf(FilterSupplyMode.SUBSCRIPTION) }
    var confirmed by rememberSaveable { mutableStateOf(false) }
    val total = purchaseSubtotalKrw(selectedFilterIds.size, mode)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.Background)
            .statusBarsPadding(),
    ) {
        PurchaseHeader(onBack = onBack)
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(top = 20.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp),
        ) {
            ModelSection(snapshot = snapshot, onChangeModel = onChangeModel)
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    PurchaseSectionTitle(text = "필터 선택")
                    Text(
                        text = "교체가 필요한 필터는 미리 골라 뒀어요",
                        style = typography.labelMidMedium,
                        color = colors.Black50Muted,
                    )
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.White, RoundedCornerShape(12.dp))
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    snapshot.filters.forEach { filter ->
                        val selected = filter.id in selectedFilterIds
                        PurchaseFilterRow(
                            filter = filter,
                            selected = selected,
                            price = formatKrw(defaultPricingPolicy.filterUnitPriceKrw(filter.filterType)),
                            onToggle = {
                                selectedFilterIds = if (selected) {
                                    selectedFilterIds - filter.id
                                } else {
                                    selectedFilterIds + filter.id
                                }
                            },
                        )
                    }
                }
                // Purchase method card (98:5866)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.White, RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    PurchaseSectionTitle(text = "구매 방식")
                    PloCareSegmentedTab(
                        items = FilterSupplyMode.entries,
                        selectedItem = mode,
                        onItemSelected = { mode = it },
                        label = { supplyModeTabLabel(it) },
                    )
                    Text(
                        text = "협력 업체가 직접 보내 드려요 • 교체 5일 전 배송",
                        style = typography.labelMidMedium,
                        color = colors.Black50Muted,
                    )
                }
            }
        }
        PurchaseBottomBar(
            mode = mode,
            total = total,
            count = selectedFilterIds.size,
            onSubmit = { confirmed = true },
        )
    }

    if (confirmed) {
        PurchasePreviewDialog(
            mode = mode,
            summary = "${mode.label} · ${selectedFilterIds.size}개 · ${formatKrw(total)}",
            onConfirm = {
                confirmed = false
                onConfirmed()
            },
            onDismiss = { confirmed = false },
        )
    }
}

@Composable
private fun PurchaseHeader(onBack: () -> Unit) {
    // Header (98:5290)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable(role = Role.Button, onClick = onBack)
                .padding(vertical = 4.dp, horizontal = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_back),
                contentDescription = "뒤로",
                tint = PloCareColor.TextMain,
            )
            Text(
                text = "정품•호환 필터 구매•구독",
                style = PloCareTheme.typography.titleLarge,
                color = PloCareColor.TextMain,
                modifier = Modifier.semantics { heading() },
            )
        }
    }
}

@Composable
private fun PurchaseSectionTitle(text: String) {
    Text(
        text = text,
        style = PloCareTheme.typography.titleMid,
        color = PloCareColor.TextMain,
        modifier = Modifier.semantics { heading() },
    )
}

@Composable
private fun ModelSection(snapshot: DashboardSnapshot, onChangeModel: () -> Unit) {
    val colors = PloCareColor
    val typography = PloCareTheme.typography
    val device = snapshot.device
    val model = listOf(device.makerName, device.modelCode).filter { it.isNotBlank() }.joinToString(" • ")

    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        PurchaseSectionTitle(text = "내 정수기에 맞는 필터를\n협력 업체와 연결해 드려요")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.White, RoundedCornerShape(12.dp))
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(48.dp).background(colors.Black0Surface, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_dishwasher),
                    contentDescription = null,
                    tint = colors.TextMain,
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "제조사 • 모델명", style = typography.labelMidMedium, color = colors.Black50Muted)
                Text(
                    text = model,
                    style = typography.titleMid,
                    color = colors.TextMain,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            PloCareButton(
                text = "변경",
                onClick = onChangeModel,
                size = PloCareButtonSize.Small,
                variant = PloCareButtonVariant.Secondary,
            )
        }
        Text(
            text = "기사님이 이 정보로 필터 호환 여부를 한 번 더 확인해요",
            style = typography.labelMidMedium,
            color = colors.Black50Muted,
        )
    }
}

@Composable
private fun PurchaseFilterRow(
    filter: FilterSnapshot,
    selected: Boolean,
    price: String,
    onToggle: () -> Unit,
) {
    val colors = PloCareColor
    val typography = PloCareTheme.typography
    val shape = RoundedCornerShape(8.dp)
    val usage = "${usagePercentLabel(filter)} 사용 • ${dDayBadge(filter.effectiveRemainingDays)}"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .then(if (selected) Modifier.background(colors.Black0Surface) else Modifier)
            .toggleable(value = selected, role = Role.Checkbox, onValueChange = { onToggle() })
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PloCareCheckMark(checked = selected)
        Column(
            modifier = Modifier.weight(1f).padding(start = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(colors.filterTypeColor(filter.filterType), RoundedCornerShape(4.dp)),
                )
                Text(
                    text = "${filter.stage}단 ${filter.name}",
                    style = typography.labelMidMedium,
                    color = colors.TextMain,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                // Compatibility badge (98:5575); manufacturer-only filters (98:5628) need catalog data.
                Box(
                    modifier = Modifier
                        .heightIn(min = 20.dp)
                        .background(colors.Primary.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = "호환", style = typography.labelSmall, color = colors.Primary)
                }
            }
            Text(text = usage, style = typography.labelSmall, color = colors.Black50Muted)
        }
        Text(text = price, style = typography.labelMidMedium, color = colors.TextMain)
    }
}

@Composable
private fun PurchaseBottomBar(
    mode: FilterSupplyMode,
    total: Int,
    count: Int,
    onSubmit: () -> Unit,
) {
    val colors = PloCareColor
    val typography = PloCareTheme.typography

    // Bottom bar (98:5897)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.White)
            .border(width = 1.dp, color = colors.Black10)
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            if (mode == FilterSupplyMode.SUBSCRIPTION) {
                Text(text = "구독 할인 적용", style = typography.labelMidMedium, color = colors.GradientMain)
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = formatKrw(total), style = typography.titleLarge, color = colors.TextMain)
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(colors.GradientMain.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = "$count", style = typography.labelMidMedium, color = colors.GradientMain)
                }
            }
        }
        PloCareButton(
            text = purchaseCtaLabel(mode),
            onClick = onSubmit,
            enabled = count > 0,
            modifier = Modifier.width(188.dp),
        )
    }
}

@Composable
private fun PurchasePreviewDialog(
    mode: FilterSupplyMode,
    summary: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = PloCareColor
    val typography = PloCareTheme.typography
    val shape = RoundedCornerShape(16.dp)
    // No Figma frame yet: the local preview reuses the modal styling of 360:3990.
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .width(320.dp)
                .background(colors.White, shape)
                .border(1.dp, colors.Black10, shape)
                .padding(start = 20.dp, top = 24.dp, end = 24.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = purchaseConfirmationTitle(mode),
                    style = typography.titleLarge,
                    color = colors.TextMain,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.semantics { heading() },
                )
                Text(
                    text = purchaseConfirmationDetail(mode),
                    style = typography.labelMidMedium,
                    color = colors.GrayChateau,
                    textAlign = TextAlign.Center,
                )
            }
            Text(text = summary, style = typography.labelMidMedium, color = colors.TextMain, textAlign = TextAlign.Center)
            PloCareButton(text = "확인", onClick = onConfirm, modifier = Modifier.fillMaxWidth())
        }
    }
}
