package com.senplo.plocare.ui.consumer.filtercare

import androidx.compose.foundation.background
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.senplo.plocare.domain.filter.DashboardSnapshot
import com.senplo.plocare.ui.consumer.ConsumerCard
import com.senplo.plocare.ui.consumer.ConsumerSectionTitle
import com.senplo.plocare.ui.theme.PloCareColor

@Composable
fun FilterPurchaseScreen(
    snapshot: DashboardSnapshot,
    preselectedFilterIds: List<String>,
    onBack: () -> Unit,
    onConfirmed: () -> Unit,
) {
    val initialSelected = remember(preselectedFilterIds, snapshot.filters) {
        when {
            preselectedFilterIds.isNotEmpty() -> preselectedFilterIds.toSet()
            else -> snapshot.filters.filter { it.showReplacementRequest }.map { it.id }.toSet()
        }
    }

    var contact by remember { mutableStateOf("") }
    var address by remember { mutableStateOf(DEFAULT_VISIT_ADDRESS) }
    var selectedFilterIds by remember { mutableStateOf(initialSelected) }
    var selectedMode by remember { mutableStateOf<FilterSupplyMode?>(null) }
    var formError by remember { mutableStateOf<String?>(null) }
    var confirmed by remember { mutableStateOf(false) }
    val mode = selectedMode
    val localTotal = mode?.let { purchaseSubtotalKrw(selectedFilterIds.size, it) }

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
                text = "정품 필터 구매·구독",
                color = PloCareColor.TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "${snapshot.device.nickname} · 로컬 가격 미리보기입니다.",
                color = PloCareColor.TextSecondary,
                fontSize = 13.sp,
            )
            Spacer(Modifier.height(18.dp))

            if (confirmed && mode != null && localTotal != null) {
                ConsumerCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = purchaseConfirmationTitle(mode),
                        color = PloCareColor.TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = purchaseConfirmationDetail(mode),
                        color = PloCareColor.TextSecondary,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "${mode.label} · ${selectedFilterIds.size}개 · ${formatKrw(localTotal)}",
                        color = PloCareColor.TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = onConfirmed,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PloCareColor.AquaTeal),
                    ) {
                        Text("필터 관리로", color = PloCareColor.BgDeep, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                ConsumerSectionTitle(title = "연락처 · 배송지")
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
                    label = "배송 주소",
                    keyboardType = KeyboardType.Text,
                )
                Spacer(Modifier.height(22.dp))
                ConsumerSectionTitle(title = "구매 필터", caption = "관리가 필요한 필터는 미리 선택되어 있습니다.")
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
                ConsumerSectionTitle(
                    title = "구매 방식",
                    caption = "구독은 로컬 10% 할인만 반영합니다. 자동 결제는 없습니다.",
                )
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterSupplyMode.entries.forEach { option ->
                        ChoiceChip(
                            label = option.label,
                            selected = selectedMode == option,
                            onClick = { selectedMode = option },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                ConsumerCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "로컬 예상 금액",
                        color = PloCareColor.TextSecondary,
                        fontSize = 11.sp,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = localTotal?.let { formatKrw(it) } ?: "구매 또는 구독을 선택해 주세요.",
                        color = PloCareColor.TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "단가 ${formatKrw(FILTER_UNIT_PRICE_KRW)} · 백엔드 가격 아님",
                        color = PloCareColor.TextTertiary,
                        fontSize = 11.sp,
                    )
                }
                formError?.let {
                    Spacer(Modifier.height(12.dp))
                    Text(it, color = PloCareColor.StatusAlert, fontSize = 13.sp)
                }
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {
                        val error = validateFilterPurchase(
                            FilterPurchaseDraft(
                                contact = contact,
                                address = address,
                                selectedFilterIds = selectedFilterIds,
                                mode = selectedMode,
                            ),
                        )
                        if (error == null) {
                            formError = null
                            confirmed = true
                        } else {
                            formError = error
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PloCareColor.AquaTeal),
                ) {
                    Text("미리보기 확인", color = PloCareColor.BgDeep, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
