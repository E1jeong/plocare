package com.senplo.plocare.ui.partner

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.senplo.plocare.domain.filter.DashboardSnapshot
import com.senplo.plocare.domain.filter.FilterSnapshot
import com.senplo.plocare.ui.theme.PloCareColor
import kotlin.math.roundToInt

@Composable
internal fun WorkInformationScreen(
    visit: PartnerVisitFixture?,
    snapshot: DashboardSnapshot?,
    onBack: () -> Unit,
    onConfirmed: (Set<String>) -> Unit,
) {
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PloCareColor.BrandNavy)
            .statusBarsPadding()
            .imePadding()
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
        Text("교체 작업 기록", color = PloCareColor.TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        Text(
            text = visit?.let { "${it.displayName} · ${device?.nickname ?: it.deviceId}" } ?: "고객을 찾을 수 없습니다",
            color = PloCareColor.TextSecondary,
            fontSize = 13.sp,
        )
        Spacer(Modifier.height(18.dp))

        if (visit == null || snapshot == null || device == null) {
            WorkCard {
                Text("방문과 연결된 정수기를 찾을 수 없습니다.", color = PloCareColor.TextSecondary, fontSize = 13.sp)
            }
        } else if (visit.completed) {
            WorkCard {
                Text("이 방문은 이미 완료되었습니다.", color = PloCareColor.TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("기준점은 다시 갱신하지 않습니다.", color = PloCareColor.TextSecondary, fontSize = 13.sp)
            }
        } else if (confirming) {
            val preview = baselineSafetyPreview(device, selectedFilterIds)
            WorkCard {
                Text("교체 완료 확인", color = PloCareColor.TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Text(visit.displayName, color = PloCareColor.AquaTeal, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text(device.nickname, color = PloCareColor.TextSecondary, fontSize = 13.sp)
                Spacer(Modifier.height(12.dp))
                Text("기준점 갱신", color = PloCareColor.TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                preview.updated.forEach { (label, liters) ->
                    Text(
                        "$label: 기준점 → ${formatCumulativeL(liters)}",
                        color = PloCareColor.TextSecondary,
                        fontSize = 13.sp,
                    )
                }
                Spacer(Modifier.height(10.dp))
                Text("유지되는 값", color = PloCareColor.TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                if (preview.unchangedLabels.isNotEmpty()) {
                    Text(
                        preview.unchangedLabels.joinToString(" · ") + " 기준점 유지",
                        color = PloCareColor.TextSecondary,
                        fontSize = 13.sp,
                    )
                }
                Text(
                    "총 누적 통수량 ${formatCumulativeL(preview.totalCumulativeL)} 유지",
                    color = PloCareColor.TextSecondary,
                    fontSize = 13.sp,
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    workConfirmationDetail(),
                    color = PloCareColor.TextTertiary,
                    fontSize = 12.sp,
                )
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { confirming = false },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PloCareColor.SurfaceDark),
                    ) {
                        Text("취소", color = PloCareColor.TextPrimary, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { onConfirmed(selectedFilterIds) },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PloCareColor.AquaTeal),
                    ) {
                        Text("확인 및 저장", color = PloCareColor.BgDeep, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            Text("교체한 필터", color = PloCareColor.TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text("방문 대상 필터가 미리 선택되어 있습니다.", color = PloCareColor.TextTertiary, fontSize = 11.sp)
            Spacer(Modifier.height(10.dp))
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
                Spacer(Modifier.height(8.dp))
            }
            Spacer(Modifier.height(10.dp))
            Text("교체 유형", color = PloCareColor.TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ReplacementType.entries.forEach { type ->
                    WorkChip(
                        label = type.label,
                        selected = selectedType == type,
                        onClick = { selectedType = type },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            Spacer(Modifier.height(18.dp))
            Text("필터 바코드 / 시리얼", color = PloCareColor.TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            WorkField(value = barcode, onValueChange = { barcode = it }, label = "선택 입력")
            Spacer(Modifier.height(18.dp))
            Text("기술자 메모", color = PloCareColor.TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            WorkField(
                value = note,
                onValueChange = { note = it },
                label = "수압, 배관, 싱크대 상태 등",
                singleLine = false,
            )
            Spacer(Modifier.height(18.dp))
            Text("하드웨어 점검", color = PloCareColor.TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            CheckRow("센서가 파이프에 단단히 고정됨", sensorClamped) { sensorClamped = it }
            Spacer(Modifier.height(8.dp))
            CheckRow("배관 누수 없음", noLeak) { noLeak = it }
            Spacer(Modifier.height(8.dp))
            CheckRow("통신 상태 확인됨", commsVerified) { commsVerified = it }
            formError?.let {
                Spacer(Modifier.height(12.dp))
                Text(it, color = PloCareColor.StatusAlert, fontSize = 13.sp)
            }
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    val error = validateWorkInformation(
                        WorkInformationDraft(
                            selectedFilterIds = selectedFilterIds,
                            type = selectedType,
                            barcode = barcode,
                            note = note,
                            sensorClamped = sensorClamped,
                            noLeak = noLeak,
                            commsVerified = commsVerified,
                        ),
                    )
                    if (error == null) {
                        formError = null
                        confirming = true
                    } else {
                        formError = error
                    }
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PloCareColor.AquaTeal),
            ) {
                Text("완료 확인으로", color = PloCareColor.BgDeep, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun FilterChoiceRow(
    filter: FilterSnapshot,
    selected: Boolean,
    onToggle: () -> Unit,
) {
    WorkCard(onClick = onToggle, selected = selected) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = if (selected) "✓" else "○",
                color = if (selected) PloCareColor.AquaTeal else PloCareColor.TextTertiary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            )
            Column(Modifier.padding(start = 10.dp).weight(1f)) {
                Text(
                    "${filter.stage}단 ${filter.name}",
                    color = PloCareColor.TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    "권장 통수량의 ${filter.exhaustionPercent.roundToInt()}% 사용",
                    color = PloCareColor.TextSecondary,
                    fontSize = 11.sp,
                )
            }
        }
    }
}

@Composable
private fun CheckRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    WorkCard(onClick = { onCheckedChange(!checked) }, selected = checked) {
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

@Composable
private fun WorkChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(40.dp)
            .border(
                width = 1.dp,
                color = if (selected) PloCareColor.AquaTeal else PloCareColor.SurfaceBorder,
                shape = RoundedCornerShape(10.dp),
            )
            .background(
                color = if (selected) PloCareColor.AquaTeal.copy(alpha = 0.16f) else PloCareColor.SurfaceCard,
                shape = RoundedCornerShape(10.dp),
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = if (selected) PloCareColor.AquaTeal else PloCareColor.TextSecondary,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun WorkField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    singleLine: Boolean = true,
) {
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = singleLine,
        minLines = if (singleLine) 1 else 3,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = if (singleLine) ImeAction.Next else ImeAction.Default,
        ),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = PloCareColor.TextPrimary,
            unfocusedTextColor = PloCareColor.TextPrimary,
            focusedBorderColor = PloCareColor.AquaTeal,
            unfocusedBorderColor = PloCareColor.SurfaceBorder,
            focusedLabelColor = PloCareColor.AquaTeal,
            unfocusedLabelColor = PloCareColor.TextSecondary,
            cursorColor = PloCareColor.AquaTeal,
            focusedContainerColor = PloCareColor.SurfaceCard,
            unfocusedContainerColor = PloCareColor.SurfaceCard,
        ),
    )
}

@Composable
private fun WorkCard(
    onClick: (() -> Unit)? = null,
    selected: Boolean = false,
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
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (selected) PloCareColor.AquaTeal else PloCareColor.SurfaceBorder,
        ),
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp), content = content)
    }
}
