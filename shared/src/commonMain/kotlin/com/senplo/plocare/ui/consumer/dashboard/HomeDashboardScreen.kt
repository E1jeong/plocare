package com.senplo.plocare.ui.consumer.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.senplo.plocare.domain.filter.DashboardSnapshot
import com.senplo.plocare.domain.filter.FilterSnapshot
import com.senplo.plocare.domain.filter.PurifierDevice
import com.senplo.plocare.ui.theme.PloCareColor
import com.senplo.plocare.ui.theme.PloCareTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import plocare.shared.generated.resources.Res
import plocare.shared.generated.resources.ic_error
import plocare.shared.generated.resources.ic_notifications_active
import plocare.shared.generated.resources.ic_unfold_more
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.senplo.plocare.domain.filter.MakerGroup
import com.senplo.plocare.ui.components.PloCareBottomSheet
import com.senplo.plocare.ui.components.PloCareButton
import com.senplo.plocare.ui.components.PloCareButtonVariant
import com.senplo.plocare.ui.components.PloCareCheckMark
import com.senplo.plocare.ui.components.PloCareCheckboxStyle
import com.senplo.plocare.ui.components.PloCareChip
import com.senplo.plocare.ui.components.PloCareTextField
import plocare.shared.generated.resources.ic_add
import plocare.shared.generated.resources.ic_chevron_down
import plocare.shared.generated.resources.ic_close
import plocare.shared.generated.resources.ic_delete
import kotlin.time.Instant

// Figma 01 대시보드 list layout (349:1675), switch sheet (349:1893), banner (380:2209)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun HomeDashboardScreen(
    devices: List<PurifierDevice>,
    snapshot: DashboardSnapshot,
    now: Instant,
    onSelectDevice: (String) -> Unit,
    onRefreshTelemetry: () -> Unit,
    onRequestReplacement: (filterIds: List<String>) -> Unit,
    onOpenDeviceSettings: () -> Unit,
    onRemoveDevice: (String) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val selected = snapshot.device
    var isRefreshing by remember { mutableStateOf(false) }
    var switcherOpen by rememberSaveable { mutableStateOf(false) }
    var dismissedBannerDeviceIds by rememberSaveable { mutableStateOf(listOf<String>()) }
    var pendingRemoveId by rememberSaveable { mutableStateOf<String?>(null) }
    var addSheetOpen by rememberSaveable { mutableStateOf(false) }
    var removedToastVisible by remember { mutableStateOf(false) }

    val banner = remember(snapshot) { replacementBanner(snapshot) }
        ?.takeIf { selected.id !in dismissedBannerDeviceIds }
    // Keeps the last banner on screen while its exit animation runs.
    var lastBanner by remember { mutableStateOf(banner) }
    if (banner != null) lastBanner = banner

    BackHandler(enabled = switcherOpen) { switcherOpen = false }

    // Screen-flow spec 4.1: the banner pushes the content down, then closes.
    val bannerFilterId = banner?.filterId
    LaunchedEffect(selected.id, bannerFilterId) {
        if (bannerFilterId != null) {
            delay(BANNER_VISIBLE_MILLIS)
            dismissedBannerDeviceIds = dismissedBannerDeviceIds + selected.id
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                scope.launch {
                    isRefreshing = true
                    delay(350)
                    onRefreshTelemetry()
                    isRefreshing = false
                }
            },
            modifier = Modifier
                .fillMaxSize()
                // Hide the dashboard from screen readers while the switch sheet covers it.
                .then(if (switcherOpen) Modifier.clearAndSetSemantics {} else Modifier),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
            ) {
                AnimatedVisibility(
                    visible = banner != null,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut(),
                ) {
                    lastBanner?.let { copy ->
                        ReplacementBanner(
                            copy = copy,
                            // Tapping leads to the purchase/replacement choice for that filter.
                            onOpen = {
                                dismissedBannerDeviceIds = dismissedBannerDeviceIds + selected.id
                                onRequestReplacement(listOf(copy.filterId))
                            },
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
                        )
                    }
                }

                DashboardHeader(
                    device = selected,
                    filterCount = snapshot.filters.size,
                    syncText = syncLabel(now - selected.lastTelemetryAt),
                    onOpenSwitcher = { switcherOpen = true },
                    onOpenDeviceSettings = onOpenDeviceSettings,
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp, top = 12.dp, end = 12.dp, bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    snapshot.filters.forEach { filter ->
                        FilterGaugeCard(
                            filter = filter,
                            onReplace = { onRequestReplacement(listOf(filter.id)) },
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = switcherOpen,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.2f))
                    .clickable(
                        interactionSource = null,
                        indication = null,
                        onClickLabel = "닫기",
                        onClick = { switcherOpen = false },
                    ),
            )
        }
        AnimatedVisibility(
            visible = switcherOpen,
            enter = slideInVertically { -it },
            exit = slideOutVertically { -it },
            modifier = Modifier.align(Alignment.TopCenter),
        ) {
            PurifierSwitchSheet(
                devices = devices,
                selectedId = selected.id,
                onSelect = { id ->
                    onSelectDevice(id)
                    switcherOpen = false
                },
                onRemove = { id -> pendingRemoveId = id },
                onAdd = {
                    switcherOpen = false
                    addSheetOpen = true
                },
            )
        }

        AnimatedVisibility(
            visible = removedToastVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 30.dp),
        ) {
            DashboardToast(text = "정수기를 제거했습니다")
        }
    }

    LaunchedEffect(removedToastVisible) {
        if (removedToastVisible) {
            delay(TOAST_VISIBLE_MILLIS)
            removedToastVisible = false
        }
    }

    pendingRemoveId?.let { id ->
        RemovePurifierDialog(
            onDismiss = { pendingRemoveId = null },
            onConfirm = {
                pendingRemoveId = null
                switcherOpen = false
                onRemoveDevice(id)
                removedToastVisible = true
            },
        )
    }

    if (addSheetOpen) {
        AddPurifierSheet(onDismiss = { addSheetOpen = false })
    }
}

private const val TOAST_VISIBLE_MILLIS = 2_000L

@Composable
private fun ReplacementBanner(
    copy: ReplacementBannerCopy,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = PloCareColor
    val typography = PloCareTheme.typography
    val shape = RoundedCornerShape(12.dp)
    // Figma 380:2209 gradient start #437DFF is outside the palette; it is used only by this banner.
    val brush = Brush.horizontalGradient(listOf(Color(0xFF437DFF), colors.GradientMain))
    val lineStyle = typography.labelMidMedium.copy(fontSize = 13.sp, lineHeight = 16.sp, letterSpacing = 0.sp)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .dropShadow(shape = shape, shadow = Shadow(radius = 24.dp, color = Color.Black.copy(alpha = 0.05f)))
            .clip(shape)
            .background(brush, shape)
            .clickable(role = Role.Button, onClick = onOpen)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            modifier = Modifier
                .size(width = 46.dp, height = 44.dp)
                .background(colors.White.copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_notifications_active),
                contentDescription = null,
                tint = colors.White,
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Text(
                    text = copy.filterLabel,
                    style = typography.gnbLabel.copy(fontWeight = FontWeight.Normal, lineHeight = 22.sp, letterSpacing = (-0.39).sp),
                    color = colors.PrimaryTint,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Box(
                    modifier = Modifier
                        .background(colors.Black0Surface.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp),
                ) {
                    Text(
                        text = "교체 예정 알림",
                        style = typography.gnbLabel.copy(fontWeight = FontWeight.Normal, lineHeight = 22.sp, letterSpacing = (-0.39).sp),
                        color = colors.White.copy(alpha = 0.9f),
                    )
                }
            }
            Text(
                text = copy.headline,
                style = typography.labelLarge,
                color = colors.White,
                modifier = Modifier.semantics { heading() },
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                copy.remainingLabel?.let { Text(text = it, style = lineStyle, color = colors.White) }
                Text(text = copy.purchaseHint, style = lineStyle, color = colors.White)
            }
        }
    }
}

@Composable
private fun DashboardHeader(
    device: PurifierDevice,
    filterCount: Int,
    syncText: String,
    onOpenSwitcher: () -> Unit,
    onOpenDeviceSettings: () -> Unit,
) {
    val colors = PloCareColor
    val typography = PloCareTheme.typography
    val pillShape = RoundedCornerShape(100.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 18.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(role = Role.Button, onClickLabel = "정수기 전환", onClick = onOpenSwitcher),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = device.nickname,
                    style = typography.headSmall,
                    color = colors.TextMain,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .semantics { heading() },
                )
                Icon(
                    painter = painterResource(Res.drawable.ic_unfold_more),
                    contentDescription = null,
                    tint = colors.TextMain,
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Figma shows only `연결됨` (349:1886). A not-connected default screen will come from the
                // designer; until then the badge stays fixed (2026-11-20 user decision).
                HeaderChip(text = "연결됨", color = colors.Primary, showDot = true)
                HeaderChip(text = "${filterCount}개 필터", color = colors.Primary)
                Text(
                    text = syncText,
                    style = chipTextStyle(),
                    color = colors.Black50Muted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Box(
            modifier = Modifier
                .dropShadow(
                    shape = pillShape,
                    shadow = Shadow(radius = 24.dp, color = Color.Black.copy(alpha = 0.04f), offset = DpOffset(0.dp, 4.dp)),
                )
                .clip(pillShape)
                .background(colors.White, pillShape)
                .clickable(role = Role.Button, onClick = onOpenDeviceSettings)
                .padding(horizontal = 24.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "설정",
                style = typography.bodyLarge.copy(fontWeight = FontWeight.Normal),
                color = colors.Black60Body,
            )
        }
    }
}

@Composable
internal fun chipTextStyle() = PloCareTheme.typography.gnbLabel.copy(
    fontWeight = FontWeight.Normal,
    lineHeight = 22.sp,
    letterSpacing = (-0.39).sp,
)

@Composable
internal fun HeaderChip(
    text: String,
    color: Color,
    showDot: Boolean = false,
) {
    Row(
        modifier = Modifier
            .heightIn(min = 22.dp)
            .background(PloCareColor.Black10, RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (showDot) {
            Box(modifier = Modifier.size(6.dp).background(color, CircleShape))
        }
        Text(text = text, style = chipTextStyle(), color = color, maxLines = 1)
    }
}

@Composable
private fun FilterGaugeCard(
    filter: FilterSnapshot,
    onReplace: () -> Unit,
) {
    val colors = PloCareColor
    val typography = PloCareTheme.typography
    val urgent = filter.showReplacementRequest
    val title = filterTitle(filter)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.White, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().heightIn(min = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = title,
                        style = typography.labelMidMedium,
                        color = colors.TextMain,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    if (urgent) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_error),
                            contentDescription = "교체 임박",
                            tint = colors.Primary,
                        )
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = usageOverRated(filter),
                        style = typography.labelSmall,
                        color = colors.Black50Muted,
                        maxLines = 1,
                    )
                    Box(
                        modifier = Modifier
                            .heightIn(min = 20.dp)
                            .background(
                                if (urgent) colors.StatusAlertContainer else colors.Black10,
                                RoundedCornerShape(4.dp),
                            )
                            .padding(horizontal = 6.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = usagePercentLabel(filter),
                            style = typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (urgent) colors.StatusAlert else colors.Primary,
                        )
                    }
                }
            }
            FilterGauge(filter = filter)
        }
        ReplaceButton(title = title, urgent = urgent, onClick = onReplace)
    }
}

@Composable
private fun FilterGauge(filter: FilterSnapshot) {
    val colors = PloCareColor
    val filled = gaugeFilledCells(filter.exhaustionPercent)
    val filterColor = colors.filterTypeColor(filter.filterType)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(26.dp)
            .clearAndSetSemantics {
                progressBarRangeInfo = ProgressBarRangeInfo(filled / GAUGE_CELL_COUNT.toFloat(), 0f..1f)
            },
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        repeat(GAUGE_CELL_COUNT) { index ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .background(
                        if (index < filled) filterColor.copy(alpha = gaugeCellAlpha(index)) else colors.Black0Surface,
                        RoundedCornerShape(4.dp),
                    ),
            )
        }
    }
}

@Composable
private fun ReplaceButton(
    title: String,
    urgent: Boolean,
    onClick: () -> Unit,
) {
    val colors = PloCareColor
    Box(
        modifier = Modifier
            .minimumInteractiveComponentSize()
            .size(45.dp)
            .dropShadow(
                shape = CircleShape,
                shadow = Shadow(radius = 24.dp, color = Color.Black.copy(alpha = 0.03f), offset = DpOffset(0.dp, 4.dp)),
            )
            .clip(CircleShape)
            .background(if (urgent) colors.Primary else colors.Black0Surface, CircleShape)
            .then(if (urgent) Modifier else Modifier.border(1.dp, colors.Black10, CircleShape))
            .semantics { contentDescription = "$title 교체" }
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "교체",
            style = PloCareTheme.typography.labelSmall,
            color = if (urgent) colors.White else colors.Black60Body,
            modifier = Modifier.clearAndSetSemantics {},
        )
    }
}

@Composable
private fun PurifierSwitchSheet(
    devices: List<PurifierDevice>,
    selectedId: String,
    onSelect: (String) -> Unit,
    onRemove: (String) -> Unit,
    onAdd: () -> Unit,
) {
    val colors = PloCareColor
    val typography = PloCareTheme.typography
    val shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
    val rowShape = RoundedCornerShape(12.dp)
    // The last purifier cannot be removed until the zero-purifier screens exist.
    val canRemove = devices.size > 1

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.White, shape)
            .border(1.dp, colors.Black10, shape)
            // Absorb taps on the sheet's padding so they do not reach the scrim behind it.
            .pointerInput(Unit) { detectTapGestures() }
            .padding(start = 20.dp, top = 24.dp, end = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(
            modifier = Modifier.selectableGroup(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            devices.forEach { device ->
                val isSelected = device.id == selectedId
                val model = device.modelCode
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(rowShape)
                        .background(colors.Black0Surface),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .selectable(selected = isSelected, role = Role.RadioButton) { onSelect(device.id) }
                            .padding(start = 18.dp, top = 16.dp, bottom = 16.dp, end = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        PloCareCheckMark(checked = isSelected, uncheckedStyle = PloCareCheckboxStyle.OutlineEmpty)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = device.nickname,
                                style = typography.bodyLarge,
                                color = colors.TextMain,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            if (model.isNotEmpty()) {
                                Text(
                                    text = model,
                                    style = typography.labelMidMedium,
                                    color = colors.Black50Muted,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                    if (canRemove) {
                        Box(
                            modifier = Modifier
                                .padding(end = 4.dp)
                                .size(48.dp)
                                .clip(CircleShape)
                                .clickable(role = Role.Button, onClick = { onRemove(device.id) }),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_delete),
                                contentDescription = "${device.nickname} 제거",
                                tint = colors.Black60Body,
                            )
                        }
                    }
                }
            }
        }
        // Add row (358:3194)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(rowShape)
                .background(colors.Black0Surface)
                .border(1.dp, colors.Black10, rowShape)
                .clickable(role = Role.Button, onClick = onAdd)
                .padding(horizontal = 18.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_add),
                contentDescription = null,
                tint = colors.Black60Body,
            )
            Text(text = "정수기 추가", style = typography.bodyLarge, color = colors.Black60Body)
        }
        // Handle (349:1894): 58x5 bar 8dp into a 34dp frame
        Box(
            modifier = Modifier.fillMaxWidth().height(34.dp),
            contentAlignment = Alignment.TopCenter,
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .width(58.dp)
                    .height(5.dp)
                    .background(colors.Black10, RoundedCornerShape(100.dp)),
            )
        }
    }
}

// Remove confirmation modal (360:3990)
@Composable
private fun RemovePurifierDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    val colors = PloCareColor
    val typography = PloCareTheme.typography
    val shape = RoundedCornerShape(16.dp)
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .width(320.dp)
                .background(colors.White, shape)
                .border(1.dp, colors.Black10, shape)
                .padding(start = 20.dp, top = 24.dp, end = 24.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = "정수기를 제거합니다",
                    style = typography.titleLarge,
                    color = colors.TextMain,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.semantics { heading() },
                )
                Text(
                    text = "한 번 제거한 정수기 데이터는 되돌릴 수 없어요.\n그래도 삭제하시겠습니까?",
                    style = typography.labelMidMedium,
                    color = colors.GrayChateau,
                    textAlign = TextAlign.Center,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PloCareButton(
                    text = "취소",
                    onClick = onDismiss,
                    variant = PloCareButtonVariant.Secondary,
                    modifier = Modifier.weight(1f),
                )
                PloCareButton(
                    text = "삭제",
                    onClick = onConfirm,
                    variant = PloCareButtonVariant.Danger,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

/**
 * Replacement-forecast modal (74:298). UI only: when it appears and what `앱 나가기` does are
 * feature-phase decisions, so no screen opens it yet.
 */
@Composable
internal fun ReplacementForecastDialog(
    copy: ReplacementForecastCopy,
    onDismiss: () -> Unit,
    onExitApp: () -> Unit,
    onReplace: () -> Unit,
) {
    // The platform default dialog width is narrower than the Figma 340dp card.
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        ReplacementForecastCard(
            copy = copy,
            onDismiss = onDismiss,
            onExitApp = onExitApp,
            onReplace = onReplace,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
    }
}

@Composable
internal fun ReplacementForecastCard(
    copy: ReplacementForecastCopy,
    onDismiss: () -> Unit,
    onExitApp: () -> Unit,
    onReplace: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = PloCareColor
    val typography = PloCareTheme.typography
    val lines = listOfNotNull(copy.purchaseLine, copy.companionLine)

    Box(
        modifier = modifier
            .widthIn(max = 340.dp)
            .fillMaxWidth()
            .background(colors.White, RoundedCornerShape(16.dp)),
    ) {
        Column(
            modifier = Modifier.padding(start = 20.dp, top = 32.dp, end = 24.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = copy.headline,
                    style = typography.titleLarge,
                    color = colors.TextMain,
                    // Keep the headline clear of the close button.
                    modifier = Modifier.padding(end = 32.dp).semantics { heading() },
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    copy.chips.forEach { chip ->
                        Text(
                            text = chip,
                            style = typography.labelMidMedium,
                            color = colors.Black60Body,
                            modifier = Modifier
                                .background(colors.Black0Surface, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 4.dp),
                        )
                    }
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.Black0Surface, RoundedCornerShape(12.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                lines.forEach { line ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(modifier = Modifier.size(8.dp), contentAlignment = Alignment.Center) {
                            Box(modifier = Modifier.size(4.dp).background(colors.Primary, CircleShape))
                        }
                        Text(
                            text = line,
                            style = typography.labelMidMedium,
                            color = colors.TextMain,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PloCareButton(
                    text = "앱 나가기",
                    onClick = onExitApp,
                    variant = PloCareButtonVariant.Secondary,
                    modifier = Modifier.weight(1f),
                )
                PloCareButton(
                    text = "교체하기",
                    onClick = onReplace,
                    modifier = Modifier.weight(1f),
                )
            }
        }
        // Figma places the 24dp close icon 21dp from the top and 20dp from the end; the 48dp touch
        // target is centered on it.
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 9.dp, end = 8.dp)
                .size(48.dp)
                .clip(CircleShape)
                .clickable(role = Role.Button, onClick = onDismiss),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_close),
                contentDescription = "닫기",
                tint = colors.TextMain,
            )
        }
    }
}

// Toast (358:3673)
@Composable
private fun DashboardToast(text: String) {
    Box(
        modifier = Modifier
            .background(PloCareColor.TextMain, RoundedCornerShape(8.dp))
            .semantics { liveRegion = LiveRegionMode.Polite }
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Text(text = text, style = PloCareTheme.typography.labelMidMedium, color = PloCareColor.White)
    }
}

// Add purifier sheet (247:6104). Maker and model lists are not wired yet (no catalog), and
// confirming only closes the sheet until the add flow is built.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AddPurifierSheet(onDismiss: () -> Unit) {
    val colors = PloCareColor
    val typography = PloCareTheme.typography
    var name by rememberSaveable { mutableStateOf("") }
    var makerGroup by rememberSaveable { mutableStateOf(MakerGroup.MAJOR) }

    PloCareBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, top = 24.dp, end = 24.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "정수기를 추가합니다",
                    style = typography.titleLarge,
                    color = colors.TextMain,
                    modifier = Modifier.semantics { heading() },
                )
                Text(text = "모델 및 필터를 확인해주세요", style = typography.labelMidMedium, color = colors.GrayChateau)
            }
            Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                SheetField(label = "정수기 이름") {
                    PloCareTextField(value = name, onValueChange = { name = it }, placeholder = "")
                }
                SheetField(label = "제조사 구분") {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MakerGroup.entries.forEach { group ->
                            PloCareChip(
                                text = group.label,
                                selected = group == makerGroup,
                                onClick = { makerGroup = group },
                            )
                        }
                    }
                }
                SheetField(label = "제조사") {
                    SelectField(label = "제조사")
                }
                SheetField(label = "모델명") {
                    SelectField(label = "모델명")
                    Text(
                        text = "정수기 옆면•뒷면 모델 라벨의 QR•바코드를 찍으면 자동 선택돼요",
                        style = typography.labelMid,
                        color = colors.GradientMain,
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PloCareButton(
                    text = "취소",
                    onClick = onDismiss,
                    variant = PloCareButtonVariant.Secondary,
                    modifier = Modifier.weight(1f),
                )
                PloCareButton(
                    text = "정수기 추가",
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun SheetField(
    label: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = label, style = PloCareTheme.typography.labelMid, color = PloCareColor.TextMain)
        content()
    }
}

/** Figma select box (247:6159): 48dp surface with a trailing chevron. Options are not wired yet. */
@Composable
private fun SelectField(label: String, value: String = "") {
    val colors = PloCareColor
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(colors.Black0Surface)
            .clickable(role = Role.DropdownList, onClickLabel = "$label 선택", onClick = {})
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = value,
            style = PloCareTheme.typography.labelLarge,
            color = colors.TextMain,
            modifier = Modifier.weight(1f),
        )
        Icon(
            painter = painterResource(Res.drawable.ic_chevron_down),
            contentDescription = null,
            tint = colors.TextMain,
        )
    }
}

