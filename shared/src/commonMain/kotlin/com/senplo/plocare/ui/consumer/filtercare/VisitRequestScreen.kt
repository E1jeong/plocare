package com.senplo.plocare.ui.consumer.filtercare

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.senplo.plocare.domain.filter.DashboardSnapshot
import com.senplo.plocare.ui.components.PloCareButton
import com.senplo.plocare.ui.components.PloCareButtonSize
import com.senplo.plocare.ui.components.PloCareCheckMark
import com.senplo.plocare.ui.components.PloCareDateCell
import com.senplo.plocare.ui.components.PloCareDateCellState
import com.senplo.plocare.ui.theme.PloCareColor
import com.senplo.plocare.ui.theme.PloCareTheme
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.painterResource
import plocare.shared.generated.resources.Res
import plocare.shared.generated.resources.ic_back
import plocare.shared.generated.resources.ic_check
import plocare.shared.generated.resources.ic_chevron_forward
import plocare.shared.generated.resources.ic_verified_user
import kotlin.time.Clock

private val WEEKDAY_HEADERS = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

// Figma 08 방문 케어 예약 (114:591)
@Composable
internal fun VisitRequestScreen(
    snapshot: DashboardSnapshot,
    preselectedFilterIds: List<String>,
    onBack: () -> Unit,
    onRequested: (VisitRequestDraft) -> Unit,
    onSubmitted: () -> Unit,
    onOpenMyPage: () -> Unit = {},
) {
    val colors = PloCareColor
    val typography = PloCareTheme.typography
    val today = remember { Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date }
    val calendarDays = remember(today) { visitCalendarDays(today) }
    val initialSelected = remember(preselectedFilterIds, snapshot.filters) {
        preselectedFilterIds.filter { id -> snapshot.filters.any { it.id == id } }
            .ifEmpty { snapshot.filters.filter { it.showReplacementRequest }.map { it.id } }
    }

    var selectedFilterIds by rememberSaveable { mutableStateOf(initialSelected) }
    var preferredDates by remember { mutableStateOf(listOf<LocalDate>()) }
    var windows by remember { mutableStateOf(mapOf<LocalDate, VisitTimeWindow>()) }
    var submitted by rememberSaveable { mutableStateOf(false) }
    val canSubmit = selectedFilterIds.isNotEmpty() && preferredDates.size == MAX_VISIT_PREFERENCES

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.Background)
            .statusBarsPadding(),
    ) {
        // Header (114:594)
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
                Icon(painter = painterResource(Res.drawable.ic_back), contentDescription = "뒤로", tint = colors.TextMain)
                Text(
                    text = "방문 케어 예약",
                    style = typography.titleLarge,
                    color = colors.TextMain,
                    modifier = Modifier.semantics { heading() },
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(top = 12.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            AddressCard(onOpenMyPage = onOpenMyPage)
            FilterChipsField(
                snapshot = snapshot,
                selectedIds = selectedFilterIds,
                onToggle = { id ->
                    selectedFilterIds = if (id in selectedFilterIds) selectedFilterIds - id else selectedFilterIds + id
                },
            )
            VisitDateCard(
                today = today,
                days = calendarDays,
                preferredDates = preferredDates,
                windows = windows,
                onToggleDate = { date -> preferredDates = toggleVisitDate(preferredDates, date) },
                onWindowChange = { date, window -> windows = windows + (date to window) },
            )
        }

        // Bottom action (114:1310)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 12.dp, vertical = 16.dp),
        ) {
            PloCareButton(
                text = "방문 요청 보내기",
                onClick = {
                    onRequested(
                        VisitRequestDraft(
                            contact = DEFAULT_VISIT_CONTACT_MASKED,
                            address = "$DEFAULT_VISIT_ADDRESS $DEFAULT_VISIT_ADDRESS_DETAIL",
                            selectedFilterIds = selectedFilterIds.toSet(),
                            preferences = preferredDates.map { date ->
                                VisitPreference(date, windows[date] ?: VisitTimeWindow.MORNING)
                            },
                        ),
                    )
                    submitted = true
                },
                size = PloCareButtonSize.Large,
                enabled = canSubmit,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    if (submitted) {
        VisitSubmittedDialog(
            onConfirm = {
                submitted = false
                onSubmitted()
            },
        )
    }
}

@Composable
private fun AddressCard(onOpenMyPage: () -> Unit) {
    val colors = PloCareColor
    val typography = PloCareTheme.typography
    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .background(colors.White, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = DEFAULT_VISIT_ADDRESS, style = typography.labelMidMedium, color = colors.TextMain)
                Text(
                    text = "$DEFAULT_VISIT_ADDRESS_DETAIL • $DEFAULT_VISIT_CONTACT_MASKED",
                    style = typography.labelSmall,
                    color = colors.Black50Muted,
                )
            }
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(role = Role.Button, onClick = onOpenMyPage)
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "마이페이지",
                    style = typography.gnbLabel.copy(fontWeight = FontWeight.Bold, lineHeight = 22.sp, letterSpacing = (-0.39).sp),
                    color = colors.Primary,
                )
                Icon(
                    painter = painterResource(Res.drawable.ic_chevron_forward),
                    contentDescription = null,
                    tint = colors.Primary,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
        // Safe-number notice (114:1334)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.Black0Surface, RoundedCornerShape(8.dp))
                .padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(painter = painterResource(Res.drawable.ic_verified_user), contentDescription = null, tint = colors.Primary)
            Text(text = "기사님께는 안심번호로만 전달돼요", style = typography.labelMidMedium, color = colors.Primary)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FilterChipsField(
    snapshot: DashboardSnapshot,
    selectedIds: List<String>,
    onToggle: (String) -> Unit,
) {
    val colors = PloCareColor
    val typography = PloCareTheme.typography
    val chipShape = RoundedCornerShape(8.dp)
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(text = "교체할 필터", style = typography.labelMid, color = colors.TextMain)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            snapshot.filters.forEach { filter ->
                val selected = filter.id in selectedIds
                Row(
                    modifier = Modifier
                        .clip(chipShape)
                        .then(
                            if (selected) {
                                Modifier.background(colors.White, chipShape).border(1.dp, colors.Black10, chipShape)
                            } else {
                                Modifier
                            },
                        )
                        .toggleable(value = selected, role = Role.Checkbox, onValueChange = { onToggle(filter.id) })
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (selected) {
                        Box(modifier = Modifier.size(18.dp), contentAlignment = Alignment.Center) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_check),
                                contentDescription = null,
                                tint = colors.GradientMain,
                            )
                        }
                    }
                    Text(
                        text = "${filter.stage}단 ${filter.name}",
                        style = typography.labelMidMedium,
                        color = if (selected) colors.TextMain else colors.Black40Ghost,
                    )
                }
            }
        }
    }
}

@Composable
private fun VisitDateCard(
    today: LocalDate,
    days: List<LocalDate>,
    preferredDates: List<LocalDate>,
    windows: Map<LocalDate, VisitTimeWindow>,
    onToggleDate: (LocalDate) -> Unit,
    onWindowChange: (LocalDate, VisitTimeWindow) -> Unit,
) {
    val colors = PloCareColor
    val typography = PloCareTheme.typography

    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .background(colors.White, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "방문 날짜 선택",
                style = typography.titleMid.copy(fontWeight = FontWeight.Medium),
                color = colors.TextMain,
                modifier = Modifier.semantics { heading() },
            )
            Text(
                text = "${preferredDates.size}/$MAX_VISIT_PREFERENCES",
                style = typography.labelMidMedium,
                color = colors.Primary,
            )
        }
        Column {
            Row(modifier = Modifier.fillMaxWidth()) {
                WEEKDAY_HEADERS.forEach { label ->
                    Box(
                        modifier = Modifier.weight(1f).height(41.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(text = label, style = typography.bodySmall, color = colors.TextMain)
                    }
                }
            }
            days.chunked(7).forEach { week ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    week.forEach { date ->
                        PloCareDateCell(
                            dayText = date.day.toString(),
                            state = when {
                                date in preferredDates -> PloCareDateCellState.Selected
                                isVisitDateSelectable(date, today) -> PloCareDateCellState.Active
                                else -> PloCareDateCellState.Disabled
                            },
                            onClick = { onToggleDate(date) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
        HorizontalDivider(thickness = 1.dp, color = colors.Black10)
        Column(
            modifier = Modifier.padding(top = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            preferredDates.forEach { date ->
                PreferenceRow(
                    date = date,
                    window = windows[date] ?: VisitTimeWindow.MORNING,
                    onRemove = { onToggleDate(date) },
                    onWindowChange = { onWindowChange(date, it) },
                )
            }
        }
    }
}

@Composable
private fun PreferenceRow(
    date: LocalDate,
    window: VisitTimeWindow,
    onRemove: () -> Unit,
    onWindowChange: (VisitTimeWindow) -> Unit,
) {
    val colors = PloCareColor
    val typography = PloCareTheme.typography
    val label = visitPreferenceLabel(date)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 34.dp)
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .toggleable(value = true, role = Role.Checkbox, onValueChange = { onRemove() }),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PloCareCheckMark(checked = true)
            Text(text = label, style = typography.labelMidMedium, color = colors.TextMain)
        }
        // AM/PM mini tab (114:1404)
        Row(
            modifier = Modifier
                .background(colors.Black0Surface, RoundedCornerShape(8.dp))
                .padding(4.dp)
                .selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            VisitTimeWindow.entries.forEach { option ->
                val selected = option == window
                val shape = RoundedCornerShape(8.dp)
                Box(
                    modifier = Modifier
                        .clip(shape)
                        .then(
                            if (selected) {
                                Modifier
                                    .background(colors.White, shape)
                                    .border(1.dp, colors.White.copy(alpha = 0.4f), shape)
                            } else {
                                Modifier
                            },
                        )
                        .selectable(selected = selected, role = Role.RadioButton) { onWindowChange(option) }
                        .padding(horizontal = 8.dp, vertical = 5.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = option.label,
                        style = typography.labelSmall,
                        color = if (selected) colors.Primary else colors.Black50Muted,
                    )
                }
            }
        }
    }
}

@Composable
private fun VisitSubmittedDialog(onConfirm: () -> Unit) {
    val colors = PloCareColor
    val typography = PloCareTheme.typography
    val shape = RoundedCornerShape(16.dp)
    // No Figma frame yet: the local preview reuses the modal styling of 360:3990.
    Dialog(onDismissRequest = onConfirm) {
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
                    text = "방문 요청을 앱 세션에 저장했습니다.",
                    style = typography.titleLarge,
                    color = colors.TextMain,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.semantics { heading() },
                )
                Text(
                    text = "로컬 미리보기입니다. 파트너에게 전달되거나 서버에 저장되지 않습니다.",
                    style = typography.labelMidMedium,
                    color = colors.GrayChateau,
                    textAlign = TextAlign.Center,
                )
            }
            PloCareButton(text = "확인", onClick = onConfirm, modifier = Modifier.fillMaxWidth())
        }
    }
}
