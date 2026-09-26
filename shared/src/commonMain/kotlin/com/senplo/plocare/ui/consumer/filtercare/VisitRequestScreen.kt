package com.senplo.plocare.ui.consumer.filtercare

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
import com.senplo.plocare.ui.consumer.ConsumerCard
import com.senplo.plocare.ui.consumer.ConsumerSectionTitle
import com.senplo.plocare.ui.theme.PloCareColor
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

@Composable
internal fun VisitRequestScreen(
    snapshot: DashboardSnapshot,
    preselectedFilterIds: List<String>,
    onBack: () -> Unit,
    onRequested: (VisitRequestDraft) -> Unit,
    onSubmitted: () -> Unit,
) {
    val timeZone = remember { TimeZone.currentSystemDefault() }
    val today = remember { Clock.System.now().toLocalDateTime(timeZone).date }
    val dateOptions = remember(today) { visitDateOptions(today) }
    val initialSelected = remember(preselectedFilterIds, snapshot.filters) {
        when {
            preselectedFilterIds.isNotEmpty() -> preselectedFilterIds.filter { id ->
                snapshot.filters.any { it.id == id }
            }.toSet()
            else -> snapshot.filters.filter { it.showReplacementRequest }.map { it.id }.toSet()
        }
    }

    var contact by remember { mutableStateOf("") }
    var address by remember { mutableStateOf(DEFAULT_VISIT_ADDRESS) }
    var selectedFilterIds by remember { mutableStateOf(initialSelected) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(dateOptions.firstOrNull()) }
    var selectedWindow by remember { mutableStateOf<VisitTimeWindow?>(null) }
    var notes by remember { mutableStateOf("") }
    var formError by remember { mutableStateOf<String?>(null) }
    var submitted by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PloCareColor.BrandNavy),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .imePadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = "← 필터 관리",
                color = PloCareColor.AquaTeal,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .clickable(onClick = onBack)
                    .padding(vertical = 8.dp),
            )
            Text(
                text = "방문 케어 예약",
                color = PloCareColor.TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "${snapshot.device.nickname} · 희망 일정과 교체 필터를 선택하세요.",
                color = PloCareColor.TextSecondary,
                fontSize = 13.sp,
            )
            Spacer(Modifier.height(18.dp))

            if (submitted) {
                ConsumerCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "방문 요청을 앱 세션에 저장했습니다.",
                        color = PloCareColor.TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "로컬 미리보기입니다. 파트너에게 전달되거나 서버에 저장되지 않습니다.",
                        color = PloCareColor.TextSecondary,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = onSubmitted,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PloCareColor.AquaTeal),
                    ) {
                        Text("대시보드로", color = PloCareColor.BgDeep, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                ConsumerSectionTitle(title = "연락처 · 주소")
                Spacer(Modifier.height(10.dp))
                VisitField(
                    value = contact,
                    onValueChange = { contact = it },
                    label = "연락처",
                    keyboardType = KeyboardType.Phone,
                )
                Spacer(Modifier.height(10.dp))
                VisitField(
                    value = address,
                    onValueChange = { address = it },
                    label = "방문 주소",
                    keyboardType = KeyboardType.Text,
                )
                Spacer(Modifier.height(22.dp))
                ConsumerSectionTitle(title = "교체 필터", caption = "묶음 교체가 필요한 필터는 미리 선택되어 있습니다.")
                Spacer(Modifier.height(10.dp))
                snapshot.filters.forEach { filter ->
                    FilterSelectRow(
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
                Spacer(Modifier.height(12.dp))
                ConsumerSectionTitle(title = "희망 일정", caption = "영업일 기준 이틀 이후부터 예약할 수 있습니다.")
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    dateOptions.forEach { date ->
                        ChoiceChip(
                            label = visitDateChipLabel(date),
                            selected = selectedDate == date,
                            onClick = { selectedDate = date },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    VisitTimeWindow.entries.forEach { window ->
                        ChoiceChip(
                            label = window.label,
                            selected = selectedWindow == window,
                            onClick = { selectedWindow = window },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                Spacer(Modifier.height(22.dp))
                ConsumerSectionTitle(title = "요청 사항")
                Spacer(Modifier.height(10.dp))
                VisitField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = "반려동물, 주차, 싱크대 잠금 등",
                    keyboardType = KeyboardType.Text,
                    singleLine = false,
                )
                formError?.let {
                    Spacer(Modifier.height(12.dp))
                    Text(it, color = PloCareColor.StatusAlert, fontSize = 13.sp)
                }
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {
                        val draft = VisitRequestDraft(
                                contact = contact,
                                address = address,
                                selectedFilterIds = selectedFilterIds,
                                date = selectedDate,
                                window = selectedWindow,
                                notes = notes,
                            )
                        val error = validateVisitRequest(draft, today)
                        if (error == null) {
                            formError = null
                            onRequested(draft)
                            submitted = true
                        } else {
                            formError = error
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PloCareColor.AquaTeal),
                ) {
                    Text("방문 요청하기", color = PloCareColor.BgDeep, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
internal fun FilterSelectRow(
    filter: FilterSnapshot,
    selected: Boolean,
    onToggle: () -> Unit,
) {
    ConsumerCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle),
        borderColor = if (selected) PloCareColor.AquaTeal else PloCareColor.SurfaceBorder,
    ) {
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
                    "권장 통수량의 ${filter.exhaustionPercent.toInt()}% 사용",
                    color = PloCareColor.TextSecondary,
                    fontSize = 11.sp,
                )
            }
        }
    }
}

@Composable
internal fun ChoiceChip(
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
internal fun VisitField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType,
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
            keyboardType = keyboardType,
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
