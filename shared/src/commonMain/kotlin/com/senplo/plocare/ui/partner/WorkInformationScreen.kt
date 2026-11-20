package com.senplo.plocare.ui.partner

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.senplo.plocare.domain.filter.DashboardSnapshot
import com.senplo.plocare.domain.filter.FilterSnapshot
import com.senplo.plocare.domain.filter.buildWaterReportHistory
import com.senplo.plocare.ui.components.PloCareButton
import com.senplo.plocare.ui.components.PloCareButtonVariant
import com.senplo.plocare.ui.components.PloCareCheckMark
import com.senplo.plocare.ui.components.PloCareCheckboxStyle
import com.senplo.plocare.ui.components.PloCareChip
import com.senplo.plocare.ui.components.PloCareTextField
import com.senplo.plocare.ui.consumer.support.SupportHeader
import com.senplo.plocare.ui.consumer.waterreport.HistoryCard
import com.senplo.plocare.ui.theme.PloCareColor
import com.senplo.plocare.ui.theme.PloCareTheme
import kotlin.math.roundToInt

/**
 * Work record under 17 고객 현장 (no Figma frame; built from the design system, 2026-11-19 decision): the
 * purifier's replacement log in the Water Report history style, then the completion form.
 */
@Composable
internal fun WorkInformationScreen(
    visit: PartnerVisitFixture?,
    snapshot: DashboardSnapshot?,
    completedReport: WorkInformationDraft?,
    onBack: () -> Unit,
    onConfirmed: (WorkInformationDraft) -> Unit,
) {
    val colors = PloCareColor
    val typography = PloCareTheme.typography
    val device = snapshot?.device
    val initialSelected = remember(visit, snapshot) {
        visit?.targetFilterIds?.filter { id -> snapshot?.filters?.any { it.id == id } == true }?.toSet()
            ?: snapshot?.filters?.filter { it.showReplacementRequest }?.map { it.id }?.toSet()
            ?: emptySet()
    }
    var selectedFilterIds by remember { mutableStateOf(initialSelected) }
    var selectedType by remember { mutableStateOf<ReplacementType?>(null) }
    var barcode by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var sensorClamped by remember { mutableStateOf(false) }
    var noLeak by remember { mutableStateOf(false) }
    var commsVerified by remember { mutableStateOf(false) }
    var formError by remember { mutableStateOf<String?>(null) }
    var confirming by remember { mutableStateOf(false) }
    var pendingDraft by remember { mutableStateOf<WorkInformationDraft?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.White)
            .statusBarsPadding()
            .imePadding()
            .navigationBarsPadding(),
    ) {
        SupportHeader(title = "교체 작업 기록", onBack = onBack)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Text(
                text = visit?.let { listOf(visitTitle(it.maskedName, it.customerId), device?.nickname ?: it.deviceId).joinToString(" • ") }
                    ?: "고객을 찾을 수 없습니다",
                style = typography.labelMidMedium,
                color = colors.Black50Muted,
            )

            if (visit == null || snapshot == null || device == null) {
                WorkCard {
                    Text("방문과 연결된 정수기를 찾을 수 없습니다.", style = typography.labelMidMedium, color = colors.Black50Muted)
                }
                return@Column
            }

            // Replacement log, same rows as Water Report history (94:4584)
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                PartnerSectionHeading("교체 이력")
                HistoryCard(
                    history = buildWaterReportHistory(device),
                    modifier = Modifier.border(1.dp, colors.Black10, RoundedCornerShape(12.dp)),
                )
            }

            when {
                visit.completed -> WorkCard {
                    PartnerSectionHeading("이 방문은 이미 완료되었습니다.")
                    WorkBody("기준점은 다시 갱신하지 않습니다.")
                    completedReport?.let { report ->
                        WorkBody("교체 유형: ${report.type?.label.orEmpty()}")
                        val names = snapshot.filters.filter { it.id in report.selectedFilterIds }
                            .joinToString(" • ") { "${it.stage}단 ${it.name}" }
                        WorkBody("교체 필터: $names")
                        if (report.barcode.isNotBlank()) WorkBody("바코드: ${report.barcode}")
                        if (report.note.isNotBlank()) WorkBody("메모: ${report.note}")
                        WorkBody("센서 고정 • 누수 • 통신 확인 완료")
                    }
                }

                confirming -> {
                    val preview = baselineSafetyPreview(device, selectedFilterIds)
                    WorkCard {
                        PartnerSectionHeading("교체 완료 확인")
                        Text(visitTitle(visit.maskedName, visit.customerId), style = typography.labelMidMedium, color = colors.Primary)
                        WorkBody(device.nickname)
                        WorkLabel("기준점 갱신")
                        preview.updated.forEach { (label, liters) ->
                            WorkBody("$label: 기준점 → ${formatCumulativeL(liters)}")
                        }
                        WorkLabel("유지되는 값")
                        if (preview.unchangedLabels.isNotEmpty()) {
                            WorkBody(preview.unchangedLabels.joinToString(" • ") + " 기준점 유지")
                        }
                        WorkBody("총 누적 통수량 ${formatCumulativeL(preview.totalCumulativeL)} 유지")
                        Text(workConfirmationDetail(), style = typography.bodySmall, color = colors.Black50Muted)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            PloCareButton(
                                text = "취소",
                                onClick = { confirming = false },
                                variant = PloCareButtonVariant.Secondary,
                                modifier = Modifier.weight(1f),
                            )
                            PloCareButton(
                                text = "확인 및 저장",
                                onClick = { pendingDraft?.let(onConfirmed) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }

                else -> {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        PartnerSectionHeading("교체한 필터")
                        Text("방문 대상 필터가 미리 선택되어 있습니다.", style = typography.labelMidMedium, color = colors.Black50Muted)
                        snapshot.filters.forEach { filter ->
                            FilterChoiceRow(
                                filter = filter,
                                selected = filter.id in selectedFilterIds,
                                onToggle = {
                                    selectedFilterIds = if (filter.id in selectedFilterIds) {
                                        selectedFilterIds - filter.id
                                    } else {
                                        selectedFilterIds + filter.id
                                    }
                                },
                            )
                        }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        PartnerSectionHeading("교체 유형")
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ReplacementType.entries.forEach { type ->
                                PloCareChip(
                                    text = type.label,
                                    selected = selectedType == type,
                                    onClick = { selectedType = type },
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        PartnerSectionHeading("필터 바코드 / 시리얼")
                        PloCareTextField(
                            value = barcode,
                            onValueChange = { barcode = it },
                            placeholder = "선택 입력",
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        PartnerSectionHeading("기술자 메모")
                        PloCareTextField(
                            value = note,
                            onValueChange = { note = it },
                            placeholder = "수압, 배관, 싱크대 상태 등",
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = false,
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        PartnerSectionHeading("하드웨어 점검")
                        CheckRow("센서가 파이프에 단단히 고정됨", sensorClamped) { sensorClamped = it }
                        CheckRow("배관 누수 없음", noLeak) { noLeak = it }
                        CheckRow("통신 상태 확인됨", commsVerified) { commsVerified = it }
                    }
                    formError?.let {
                        Text(it, style = typography.labelMidMedium, color = colors.StatusAlert)
                    }
                    PloCareButton(
                        text = "완료 확인으로",
                        onClick = {
                            val draft = WorkInformationDraft(
                                selectedFilterIds = selectedFilterIds,
                                type = selectedType,
                                barcode = barcode,
                                note = note,
                                sensorClamped = sensorClamped,
                                noLeak = noLeak,
                                commsVerified = commsVerified,
                            )
                            val error = validateWorkInformation(draft)
                            if (error == null) {
                                formError = null
                                pendingDraft = draft
                                confirming = true
                            } else {
                                formError = error
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Composable
private fun WorkBody(text: String) {
    Text(text, style = PloCareTheme.typography.labelMidMedium, color = PloCareColor.Black70)
}

@Composable
private fun WorkLabel(text: String) {
    Text(text, style = PloCareTheme.typography.bodyLarge, color = PloCareColor.TextMain)
}

@Composable
private fun FilterChoiceRow(
    filter: FilterSnapshot,
    selected: Boolean,
    onToggle: () -> Unit,
) {
    SelectableRow(selected = selected, onToggle = onToggle) {
        Column(Modifier.weight(1f)) {
            Text("${filter.stage}단 ${filter.name}", style = PloCareTheme.typography.labelLarge, color = PloCareColor.TextMain)
            Text(
                "권장 통수량의 ${filter.exhaustionPercent.roundToInt()}% 사용",
                style = PloCareTheme.typography.labelMidMedium,
                color = PloCareColor.Black50Muted,
            )
        }
    }
}

@Composable
private fun CheckRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    SelectableRow(selected = checked, onToggle = { onCheckedChange(!checked) }) {
        Text(label, style = PloCareTheme.typography.labelLarge, color = PloCareColor.TextMain, modifier = Modifier.weight(1f))
    }
}

/** White bordered row with the `circle/` check mark; the selected row takes the chip's selected colors. */
@Composable
private fun SelectableRow(
    selected: Boolean,
    onToggle: () -> Unit,
    content: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit,
) {
    val colors = PloCareColor
    val shape = RoundedCornerShape(12.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (selected) colors.PrimaryContainerLight else colors.White, shape)
            .border(1.dp, if (selected) colors.GradientSecondary else colors.Black10, shape)
            .toggleable(value = selected, role = Role.Checkbox, onValueChange = { onToggle() })
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PloCareCheckMark(checked = selected, uncheckedStyle = PloCareCheckboxStyle.OutlineEmpty)
        content()
    }
}

@Composable
private fun WorkCard(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(PloCareColor.Black0Surface, RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        content = content,
    )
}
